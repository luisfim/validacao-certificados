package br.com.luis.certificados;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import static br.com.luis.certificados.Modelos.*;

@Service
public class Certificados {
    private final Banco banco;
    private final Extrator extrator;
    private final Validador validador;
    public Certificados(Banco banco, Extrator extrator, Validador validador) {
        this.banco=banco; this.extrator=extrator; this.validador=validador;
    }
    public List<Aluno> alunos() {
        return List.of(new Aluno("1", "Ana Costa", LocalDate.of(2025,1,1)),
                       new Aluno("2", "Bruno Lima", LocalDate.of(2025,1,1)));
    }
    public Aluno aluno(String id) {
        return alunos().stream().filter(a->a.id().equals(id)).findFirst()
            .orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Selecione um aluno válido."));
    }
    // Evita aprovar dois envios iguais ao mesmo tempo nesta única instância local.
    public synchronized Analise analisar(String alunoId, String texto, String arquivo, String origem, byte[] original, boolean usarIa) {
        Aluno aluno=aluno(alunoId);
        var extracao=extrator.extrair(texto, usarIa);
        Dados d=extracao.dados();
        String hash=hash(original);
        boolean duplicado=banco.hashAprovado(alunoId,hash), parecido=false;
        for (var anterior:banco.aprovados(alunoId)) {
            Dados outro=anterior.dados();
            if (!Texto.vazio(d.codigo()) && !Texto.vazio(d.emissor()) &&
                Texto.normalizar(d.codigo()).equals(Texto.normalizar(outro.codigo())) &&
                Texto.normalizar(d.emissor()).equals(Texto.normalizar(outro.emissor()))) duplicado=true;
            if ((Texto.vazio(d.codigo()) || Texto.vazio(outro.codigo())) &&
                !Texto.vazio(d.atividade()) && !Texto.vazio(d.emissor()) && Texto.data(d.inicio())!=null && Texto.data(d.termino())!=null &&
                Texto.normalizar(d.atividade()).equals(Texto.normalizar(outro.atividade())) &&
                Texto.normalizar(d.emissor()).equals(Texto.normalizar(outro.emissor())) &&
                Objects.equals(Texto.data(d.inicio()),Texto.data(outro.inicio())) &&
                Objects.equals(Texto.data(d.termino()),Texto.data(outro.termino()))) parecido=true;
        }
        var decisao=validador.validar(d, aluno, LocalDate.now(ZoneId.of("America/Fortaleza")),duplicado,parecido,extracao.avisos());
        var etapas = new ArrayList<String>();
        if (origem.equals("ARQUIVO")) etapas.add("Arquivo convertido em imagem, tratado com OpenCV e lido pelo Tesseract.");
        else etapas.add("Texto informado diretamente; OCR e OpenCV não foram usados.");
        etapas.add("Extração: " + extracao.modo());
        etapas.add("Regras aplicadas em Java; resultado persistido em H2.");
        var a=new Analise(UUID.randomUUID().toString(), Instant.now().toString(),aluno,arquivo,origem,
                extracao.modo(),texto,d,decisao.status(),decisao.motivos(),etapas);
        banco.salvar(a,hash);
        return a;
    }
    private String hash(byte[] bytes) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
}
