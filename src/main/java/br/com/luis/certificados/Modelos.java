package br.com.luis.certificados;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

// Records agrupados aqui para manter o protótipo pequeno.
public class Modelos {
    public record Aluno(String id, String nome, LocalDate ingresso) {}
    public record Dados(String participante, String atividade, String emissor, Double horas,
                        String inicio, String termino, String emissao, String codigo) {}
    public record PedidoTexto(@NotBlank String alunoId,
                              @NotBlank @Size(max=20000) String texto, boolean usarIa) {}
    public record Decisao(String status, List<String> motivos) {}
    public record Extracao(Dados dados, String modo, List<String> avisos) {}
    public record Analise(String id, String criadoEm, Aluno aluno, String arquivo, String origem,
                          String modoExtracao, String textoExtraido, Dados dados,
                          String status, List<String> motivos, List<String> etapas) {}
}
