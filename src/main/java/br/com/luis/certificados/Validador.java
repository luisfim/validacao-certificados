package br.com.luis.certificados;

import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static br.com.luis.certificados.Modelos.*;

@Component
public class Validador {
    public Decisao validar(Dados d, Aluno aluno, LocalDate hoje, boolean duplicado, boolean parecido,
                           List<String> avisos) {
        var pendencias = new ArrayList<>(avisos);
        var erros = new ArrayList<String>();
        if (Texto.vazio(d.participante())) pendencias.add("Nome do participante ausente ou não reconhecido.");
        else if (!Texto.normalizar(d.participante()).equals(Texto.normalizar(aluno.nome())))
            pendencias.add("O nome não corresponde ao aluno selecionado; conferir manualmente.");
        if (Texto.vazio(d.atividade())) pendencias.add("Título da atividade ausente.");
        if (Texto.vazio(d.emissor())) pendencias.add("Instituição emissora ausente.");
        if (d.horas() == null || !Double.isFinite(d.horas())) pendencias.add("Carga horária não reconhecida.");
        else if (d.horas() <= 0) erros.add("A carga horária deve ser maior que zero.");

        var inicio = Texto.data(d.inicio());
        var termino = Texto.data(d.termino());
        var emissao = Texto.data(d.emissao());
        if (inicio == null) pendencias.add("Data de início ausente ou inválida.");
        if (termino == null) pendencias.add("Data de término ausente ou inválida.");
        if (emissao == null) pendencias.add("Data de emissão ausente ou inválida.");
        if (inicio != null && inicio.isBefore(aluno.ingresso())) erros.add("A atividade começou antes do ingresso do aluno.");
        if (inicio != null && termino != null && inicio.isAfter(termino)) erros.add("O início é posterior ao término.");
        if (termino != null && emissao != null && emissao.isBefore(termino)) erros.add("A emissão é anterior ao término.");
        if (emissao != null && emissao.isAfter(hoje)) erros.add("A data de emissão está no futuro.");
        if (duplicado) erros.add("Certificado já aprovado na triagem para este aluno (duplicidade).");
        if (parecido && !duplicado) pendencias.add("Há uma atividade semelhante no histórico; conferir duplicidade.");
        if (!erros.isEmpty()) {
            erros.addAll(pendencias);
            return new Decisao("REJEITADO", erros);
        }
        if (!pendencias.isEmpty()) return new Decisao("PENDENTE", pendencias);
        return new Decisao("APROVADO_NA_TRIAGEM", List.of("Campos e regras atendidos. O aproveitamento das horas depende da coordenação."));
    }
}
