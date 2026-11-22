package br.com.luis.certificados;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import static br.com.luis.certificados.Modelos.*;

@Repository
public class Banco {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    public Banco(JdbcTemplate jdbc, ObjectMapper json) { this.jdbc=jdbc; this.json=json; }
    public void salvar(Analise a, String hash) {
        try {
            jdbc.update("INSERT INTO analises (id,aluno_id,hash_documento,status,criado_em,resposta) VALUES (?,?,?,?,?,?)",
                a.id(), a.aluno().id(), hash, a.status(), a.criadoEm(), json.writeValueAsString(a));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException(e); }
    }
    private Analise ler(String s) {
        try { return json.readValue(s, Analise.class); }
        catch (Exception e) { throw new IllegalStateException("Registro inválido no banco", e); }
    }
    public List<Analise> listar() {
        return jdbc.query("SELECT resposta FROM analises ORDER BY criado_em DESC LIMIT 100", (r,i)->ler(r.getString(1)));
    }
    public Analise buscar(String id) {
        var itens=jdbc.query("SELECT resposta FROM analises WHERE id=?", (r,i)->ler(r.getString(1)), id);
        return itens.isEmpty() ? null : itens.getFirst();
    }
    public boolean hashAprovado(String aluno, String hash) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM analises WHERE aluno_id=? AND hash_documento=? AND status='APROVADO_NA_TRIAGEM'",
                Integer.class, aluno, hash) > 0;
    }
    public List<Analise> aprovados(String aluno) {
        return jdbc.query("SELECT resposta FROM analises WHERE aluno_id=? AND status='APROVADO_NA_TRIAGEM'",
                (r,i)->ler(r.getString(1)), aluno);
    }
}
