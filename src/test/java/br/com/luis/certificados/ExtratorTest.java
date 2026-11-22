package br.com.luis.certificados;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ExtratorTest {
    final ObjectMapper json=new ObjectMapper();
    @Test void extratorLocalNaoCompletaCampoAusente() {
        var e=new Extrator(json,"","modelo","http://localhost");
        var d=e.local("Participante: Ana Costa\nCarga horaria: 2,5 horas");
        assertEquals(2.5,d.horas());assertNull(d.emissor());assertNull(d.emissao());
    }
    @Test void rotulosDuplicadosSaoAmbiguos() {
        var d=new Extrator(json,"","modelo","http://localhost").local("Participante: Ana Costa\nParticipante: Bruno Lima");
        assertNull(d.participante());
    }
    @Test void integraContratoIaEDescartaNomeInventado() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        List<String> pedidos=new ArrayList<>();
        server.createContext("/",exchange->{
            pedidos.add(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            String conteudo="{\"participante\":\"Pessoa Inventada\",\"atividade\":\"Oficina de Java\",\"emissor\":\"Escola Exemplo\",\"horas\":12,\"inicio\":\"2026-10-01\",\"termino\":\"2026-10-02\",\"emissao\":\"2026-10-03\",\"codigo\":\"JAVA-2026-001\"}";
            byte[] body=json.writeValueAsBytes(Map.of("choices",List.of(Map.of("message",Map.of("content",conteudo)))));
            exchange.sendResponseHeaders(200,body.length);exchange.getResponseBody().write(body);exchange.close();
        });server.start();
        try {
            var e=new Extrator(json,"teste-local","modelo","http://127.0.0.1:"+server.getAddress().getPort()+"/");
            var resultado=e.extrair(Files.readString(Path.of("exemplos/valido.txt")),true);
            assertNull(resultado.dados().participante());assertEquals("2026-10-03",resultado.dados().emissao());
            assertFalse(resultado.avisos().isEmpty());assertTrue(pedidos.getFirst().contains("json_object"));
        } finally {server.stop(0);}
    }
    @Test void jsonInvalidoDaIaRetornaErro() throws Exception {
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/",exchange->{byte[] b="{}".getBytes();exchange.sendResponseHeaders(200,b.length);exchange.getResponseBody().write(b);exchange.close();});server.start();
        try {
            var e=new Extrator(json,"teste","modelo","http://127.0.0.1:"+server.getAddress().getPort()+"/");
            assertEquals(502,assertThrows(ResponseStatusException.class,()->e.extrair("certificado",true)).getStatusCode().value());
        } finally {server.stop(0);}
    }
}
