package br.com.luis.certificados;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.*;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:testes;DB_CLOSE_DELAY=-1","app.ai-key="})
@AutoConfigureMockMvc
class ApiTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    String texto;
    @BeforeEach void limpar() throws Exception {
        jdbc.update("DELETE FROM analises");texto=Files.readString(Path.of("exemplos/valido.txt"));
    }
    String pedido(String t) throws Exception { return json.writeValueAsString(Map.of("alunoId","1","texto",t,"usarIa",false)); }
    @Test void salvaConsultaEDetectaRepeticao() throws Exception {
        var r=mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(texto)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("APROVADO_NA_TRIAGEM")).andReturn();
        String local=r.getResponse().getHeader("Location");
        mvc.perform(get(local)).andExpect(status().isOk()).andExpect(jsonPath("$.dados.horas").value(12.0));
        mvc.perform(get("/api/analises")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(texto)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REJEITADO"));
    }
    @Test void codigoIgualMesmoComArquivoDiferente() throws Exception {
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(texto))).andExpect(status().isCreated());
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(texto+"\nOutra linha")))
                .andExpect(jsonPath("$.status").value("REJEITADO"));
    }
    @Test void duplicidadeProvavelSemCodigo() throws Exception {
        String semCodigo=texto.replace("Codigo: JAVA-2026-001", "");
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(semCodigo))).andExpect(status().isCreated());
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(semCodigo+"\nSegunda imagem")))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }
    @Test void reenvioCorrigidoPodeSerAprovado() throws Exception {
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(texto.replace("Carga horaria: 12 horas", ""))))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(texto)))
                .andExpect(jsonPath("$.status").value("APROVADO_NA_TRIAGEM"));
    }
    @Test void rejeitaPedidoVazioEAlunoInexistente() throws Exception {
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(" "))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(texto).replace("\"alunoId\":\"1\"","\"alunoId\":\"99\"")))
                .andExpect(status().isBadRequest());
    }
    @Test void iaSemChaveNaoFingeSucesso() throws Exception {
        mvc.perform(post("/api/analises/texto").contentType("application/json").content(pedido(texto).replace("false","true")))
                .andExpect(status().isServiceUnavailable());
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM analises",Integer.class));
    }
    @Test void formatoFalsoNaoPassaPeloOcr() throws Exception {
        var f=new MockMultipartFile("arquivo","fake.png","image/png","isto não é uma imagem".getBytes());
        mvc.perform(multipart("/api/analises/arquivo").file(f).param("alunoId","1")).andExpect(status().isUnsupportedMediaType());
    }
    @Test void paginaInicialEConsultaInexistente() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/api/analises/inexistente")).andExpect(status().isNotFound());
    }
    @Test void pdfComDuasPaginasRejeitado() throws Exception {
        try(var pdf=new org.apache.pdfbox.pdmodel.PDDocument();var out=new java.io.ByteArrayOutputStream()) {
            pdf.addPage(new org.apache.pdfbox.pdmodel.PDPage());pdf.addPage(new org.apache.pdfbox.pdmodel.PDPage());pdf.save(out);
            var f=new MockMultipartFile("arquivo","duas.pdf","application/pdf",out.toByteArray());
            mvc.perform(multipart("/api/analises/arquivo").file(f).param("alunoId","1")).andExpect(status().isBadRequest());
        }
    }
}
