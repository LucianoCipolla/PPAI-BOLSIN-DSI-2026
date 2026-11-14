package com.g5.bolsines.adapter;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Proveedor de GPS falso para los tests: como cada adaptador crea su propio servicio,
 * se lo apunta a este servidor local, que responde lo que indique el test y registra la consulta.
 */
class ProveedorGPSFalso implements AutoCloseable {

    private final HttpServer servidor;
    private String consultaRecibida;
    private String claveRecibida;
    ProveedorGPSFalso(int estado, String cuerpo) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/", intercambio -> {
            consultaRecibida = intercambio.getRequestURI().toString();
            claveRecibida = intercambio.getRequestHeaders().getFirst("X-API-Key");
            byte[] respuesta = cuerpo.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().set("Content-Type", "application/json");
            intercambio.sendResponseHeaders(estado, respuesta.length);
            intercambio.getResponseBody().write(respuesta);
            intercambio.close();
        });
        servidor.start();
    }

    String getUrl() {
        return "http://localhost:" + servidor.getAddress().getPort();
    }

    String getConsultaRecibida() {
        return consultaRecibida;
    }
    String getClaveRecibida() {
        return claveRecibida;
    }
    @Override
    public void close() {
        servidor.stop(0);
    }
}
