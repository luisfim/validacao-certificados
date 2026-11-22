package br.com.luis.certificados;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static br.com.luis.certificados.Modelos.*;

@RestController
@RequestMapping("/api")
public class Api {
    private final Certificados certificados;
    private final Banco banco;
    private final Ocr ocr;
    private final Extrator extrator;
    public Api(Certificados certificados, Banco banco, Ocr ocr, Extrator extrator) {
        this.certificados=certificados;this.banco=banco;this.ocr=ocr;this.extrator=extrator;
    }
    @GetMapping("/alunos") public List<Aluno> alunos() { return certificados.alunos(); }
    @GetMapping("/config") public Map<String,Object> config() {
        return Map.of("iaConfigurada",extrator.iaConfigurada(),"banco","H2 em arquivo", "limiteArquivoMB",5);
    }
    @GetMapping("/analises") public List<Analise> listar() { return banco.listar(); }
    @GetMapping("/analises/{id}") public Analise buscar(@PathVariable String id) {
        Analise a=banco.buscar(id);
        if (a==null) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Análise não encontrada.");
        return a;
    }
    @PostMapping("/analises/texto") public ResponseEntity<Analise> texto(@Valid @RequestBody PedidoTexto p) {
        Analise a=certificados.analisar(p.alunoId(),p.texto(),"Texto informado", "TEXTO",p.texto().getBytes(StandardCharsets.UTF_8),p.usarIa());
        return ResponseEntity.created(URI.create("/api/analises/"+a.id())).body(a);
    }
    @PostMapping(value="/analises/arquivo",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Analise> arquivo(@RequestParam String alunoId, @RequestParam(defaultValue="false") boolean usarIa,
                                           @RequestParam MultipartFile arquivo) throws java.io.IOException {
        certificados.aluno(alunoId);
        if (usarIa && !extrator.iaConfigurada()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"IA sem chave; desmarque a opção de IA.");
        String nome=Objects.toString(arquivo.getOriginalFilename(),"Certificado").replaceAll("[^\\p{L}\\p{N}._ -]", "_");
        if (nome.length()>120) nome=nome.substring(0,120);
        Analise a=certificados.analisar(alunoId,ocr.ler(arquivo),nome,"ARQUIVO",arquivo.getBytes(),usarIa);
        return ResponseEntity.created(URI.create("/api/analises/"+a.id())).body(a);
    }
}
