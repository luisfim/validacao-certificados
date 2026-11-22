package br.com.luis.certificados;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static br.com.luis.certificados.Modelos.*;

class ValidadorTest {
    final Validador validador=new Validador();
    final Aluno aluno=new Aluno("1","Ana Costa",LocalDate.of(2025,1,1));
    final LocalDate hoje=LocalDate.of(2026,11,19);
    Dados dados(String nome, Double horas, String inicio, String fim, String emissao) {
        return new Dados(nome,"Oficina","Escola",horas,inicio,fim,emissao,"001");
    }
    String status(Dados d) { return validador.validar(d,aluno,hoje,false,false,List.of()).status(); }
    @Test void completoEAcentos() {
        assertEquals("APROVADO_NA_TRIAGEM",status(dados(" ÁNA  COSTA ",12.0,"01/10/2026","02/10/2026","03/10/2026")));
    }
    @Test void aceitaDatasNosLimites() {
        assertEquals("APROVADO_NA_TRIAGEM",status(dados("Ana Costa",1.0,"2025-01-01","2026-11-19","2026-11-19")));
    }
    @Test void semHorasPendente() { assertEquals("PENDENTE",status(dados("Ana Costa",null,"2026-10-01","2026-10-02","2026-10-03"))); }
    @Test void horasZeroRejeitadas() { assertEquals("REJEITADO",status(dados("Ana Costa",0.0,"2026-10-01","2026-10-02","2026-10-03"))); }
    @Test void dataImpossivelPendente() { assertEquals("PENDENTE",status(dados("Ana Costa",1.0,"31/02/2026","2026-10-02","2026-10-03"))); }
    @Test void emissaoAnteriorRejeitada() { assertEquals("REJEITADO",status(dados("Ana Costa",1.0,"2026-10-01","2026-10-02","2026-10-01"))); }
    @Test void emissaoFuturaRejeitada() { assertEquals("REJEITADO",status(dados("Ana Costa",1.0,"2026-10-01","2026-10-02","2027-10-01"))); }
    @Test void antesDoIngressoRejeitado() { assertEquals("REJEITADO",status(dados("Ana Costa",1.0,"2024-10-01","2026-10-02","2026-10-03"))); }
    @Test void nomeDiferentePendente() { assertEquals("PENDENTE",status(dados("Bruno Lima",1.0,"2026-10-01","2026-10-02","2026-10-03"))); }
    @Test void inicioDepoisDoFimRejeitado() { assertEquals("REJEITADO",status(dados("Ana Costa",1.0,"2026-10-03","2026-10-02","2026-10-03"))); }
}
