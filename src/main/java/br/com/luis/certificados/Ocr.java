package br.com.luis.certificados;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class Ocr {
    private final String python, tesseract, idioma, script;
    public Ocr(@Value("${app.python}") String python, @Value("${app.tesseract}") String tesseract,
               @Value("${app.ocr-language}") String idioma, @Value("${app.ocr-script}") String script) {
        this.python=python; this.tesseract=tesseract; this.idioma=idioma; this.script=script;
    }
    public String ler(MultipartFile arquivo) {
        Path dir = null;
        try {
            if (arquivo.isEmpty() || arquivo.getSize() > 5 * 1024 * 1024)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo de até 5 MB, não vazio.");
            byte[] bytes=arquivo.getBytes();
            dir=Files.createTempDirectory("certificado-");
            Path entrada=dir.resolve("entrada.png");
            if (bytes.length >= 5 && new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-")) {
                try (var pdf=Loader.loadPDF(bytes)) {
                    if (pdf.getNumberOfPages()!=1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O MVP aceita PDF com uma página.");
                    var pagina=pdf.getPage(0).getCropBox();
                    if (pagina.getWidth()<=0 || pagina.getHeight()<=0 || pagina.getWidth()*pagina.getHeight()*9 > 20000000)
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página grande demais para o protótipo.");
                    ImageIO.write(new PDFRenderer(pdf).renderImageWithDPI(0, 150), "png", entrada.toFile());
                }
            } else {
                try (var in=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                    var leitores=ImageIO.getImageReaders(in);
                    if (!leitores.hasNext()) throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato inválido. Envie PNG, JPG ou PDF.");
                    var leitor=leitores.next();
                    try {
                        leitor.setInput(in);
                        String formato=leitor.getFormatName().toLowerCase(Locale.ROOT);
                        if (!Set.of("png","jpeg","jpg").contains(formato)) throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,"Use PNG, JPG ou PDF.");
                        if ((long)leitor.getWidth(0)*leitor.getHeight(0)>20000000)
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem acima de 20 megapixels.");
                        ImageIO.write(leitor.read(0), "png", entrada.toFile());
                    } finally { leitor.dispose(); }
                }
            }
            Path tratada=dir.resolve("tratada.png");
            executar(List.of(python, script, entrada.toString(), tratada.toString()), dir.resolve("opencv.log"), "OpenCV/Python");
            Path saida=dir.resolve("leitura");
            executar(List.of(tesseract, tratada.toString(), saida.toString(), "-l", idioma, "--psm", "6"), dir.resolve("ocr.log"), "Tesseract/idioma " + idioma);
            String texto=Files.readString(dir.resolve("leitura.txt"));
            if (texto.length()>20000) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Documento contém texto demais para o MVP.");
            return texto;
        } catch (ResponseStatusException e) { throw e;
        } catch (IOException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo corrompido, protegido ou não pôde ser lido.");
        } finally {
            if (dir != null) try (var arquivos=Files.walk(dir)) {
                for (Path p: arquivos.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p);
            } catch (IOException ignored) { }
        }
    }
    private void executar(List<String> comando, Path log, String ferramenta) {
        try {
            var processo=new ProcessBuilder(comando).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!processo.waitFor(30, TimeUnit.SECONDS)) {
                processo.destroyForcibly(); processo.waitFor(5, TimeUnit.SECONDS);
                throw new IOException("Tempo excedido");
            }
            if (processo.exitValue()!=0) throw new IOException("Falha no comando");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Leitura interrompida. Tente novamente.");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Não foi possível executar " + ferramenta + ". Confira a instalação ou use a aba Texto de exemplo.");
        }
    }
}
