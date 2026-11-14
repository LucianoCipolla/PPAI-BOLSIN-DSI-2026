package com.g5.bolsines.boundary;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Doble de prueba de la API de la cátedra (repo dsi-api-bolsines): mismas rutas, keys, validaciones,
 * formatos y bolsines. Permite correr los tests de integración sin tener levantada la API real.
 */
class ApiCatedraFalsa {

    private static final String CLAVE_XTR = "sk_xtr_9F3a7C21bE6d4A10f2c8e5b7d913";
    private static final String CLAVE_NAVTRACK = "sk_nav_7b61d9e4c3a2485fae10c77d5b2f";
    private static final String CLAVE_GEOPULSE = "sk_geo_f2d84a1c9e7345b8a6d1c3f0972e";

    private record Bolsin(String cmOrigen, String cmDestino, double latitud, double longitud, LocalDateTime fecha) {
    }

    private record Respuesta(int estado, String tipo, String cuerpo) {
    }

    /** Copia de InMemoryBolsinRepository de la cátedra. */
    private static final Map<Integer, Bolsin> BOLSINES = Map.of(
            101, new Bolsin("CM-CBA-01", "CM-ROS-02", -31.4201, -64.1888, LocalDateTime.of(2026, 3, 7, 12, 30, 0)),
            102, new Bolsin("CM-ROS-02", "CM-MDZ-03", -32.9468, -60.6393, LocalDateTime.of(2026, 3, 7, 12, 32, 15)),
            103, new Bolsin("CM-MDZ-03", "CM-BA-01", -32.8895, -68.8458, LocalDateTime.of(2026, 3, 7, 12, 35, 4)),
            104, new Bolsin("CM-BA-01", "CM-CBA-01", -34.6037, -58.3816, LocalDateTime.of(2026, 3, 7, 12, 36, 20)),
            105, new Bolsin("CM-TUC-01", "CM-SLA-01", -26.8083, -65.2176, LocalDateTime.of(2026, 3, 7, 12, 40, 31)),
            106, new Bolsin("CM-SLA-01", "CM-JUJ-01", -24.7829, -65.4232, LocalDateTime.of(2026, 3, 7, 12, 43, 18)),
            107, new Bolsin("CM-NQN-01", "CM-MDQ-01", -38.9516, -68.0591, LocalDateTime.of(2026, 3, 7, 12, 45, 46)),
            108, new Bolsin("CM-MDQ-01", "CM-BA-01", -38.0055, -57.5426, LocalDateTime.of(2026, 3, 7, 12, 47, 2)),
            109, new Bolsin("CM-LPL-01", "CM-CBA-01", -34.9215, -57.9545, LocalDateTime.of(2026, 3, 7, 12, 49, 27)),
            110, new Bolsin("CM-CBA-02", "CM-ROS-02", -31.4167, -64.1833, LocalDateTime.of(2026, 3, 7, 12, 52, 9)));

    private static final Respuesta NO_AUTORIZADO = error(401, "API_KEY_INVALIDA");
    private static final Respuesta NO_ENCONTRADO = error(404, "BOLSIN_NO_ENCONTRADO");

    private final HttpServer servidor;

    ApiCatedraFalsa() {
        try {
            servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        servidor.createContext("/api", this::atender);
        servidor.start();
    }

    String getUrl() {
        return "http://localhost:" + servidor.getAddress().getPort() + "/api";
    }

    private void atender(HttpExchange intercambio) throws IOException {
        String clave = intercambio.getRequestHeaders().getFirst("X-API-Key");
        Map<String, String> parametros = leerParametros(intercambio.getRequestURI().getRawQuery());
        Respuesta respuesta = switch (intercambio.getRequestURI().getPath()) {
            case "/api/xtr-4500l/getBolsinLocation" -> xtr(clave, parametros);
            case "/api/navtrack-qx-7a/retrieveTrackingData" -> navTrack(clave, parametros);
            case "/api/geopulse-mtr-900/fetchCargoPositions" -> geoPulse(clave, parametros);
            default -> NO_ENCONTRADO;
        };
        byte[] cuerpo = respuesta.cuerpo().getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", respuesta.tipo());
        intercambio.sendResponseHeaders(respuesta.estado(), cuerpo.length);
        intercambio.getResponseBody().write(cuerpo);
        intercambio.close();
    }

    private Respuesta xtr(String clave, Map<String, String> parametros) {
        if (!CLAVE_XTR.equals(clave)) {
            return NO_AUTORIZADO;
        }
        int numero = numeroBolsin(parametros);
        Bolsin bolsin = BOLSINES.get(numero);
        if (bolsin == null || !bolsin.cmOrigen().equalsIgnoreCase(parametros.get("codigoComisionMedicaOrigen"))) {
            return NO_ENCONTRADO;
        }
        return json("{\"items\":[{\"numeroBolsin\":" + numero + ",\"latitud\":" + bolsin.latitud()
                + ",\"longitud\":" + bolsin.longitud() + ",\"fechaHoraUltimaActualizacion\":\""
                + bolsin.fecha().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\"}]}");
    }

    private Respuesta navTrack(String clave, Map<String, String> parametros) {
        if (!CLAVE_NAVTRACK.equals(clave)) {
            return NO_AUTORIZADO;
        }
        int numero = numeroBolsin(parametros);
        Bolsin bolsin = BOLSINES.get(numero);
        if (bolsin == null || !bolsin.cmDestino().equalsIgnoreCase(parametros.get("codigoComisionMedicaDestino"))) {
            return NO_ENCONTRADO;
        }
        return new Respuesta(200, "text/plain",
                numero + "," + bolsin.latitud() + "," + bolsin.longitud() + "," + bolsin.fecha());
    }

    private Respuesta geoPulse(String clave, Map<String, String> parametros) {
        if (!CLAVE_GEOPULSE.equals(clave)) {
            return NO_AUTORIZADO;
        }
        int numero = numeroBolsin(parametros);
        Bolsin bolsin = BOLSINES.get(numero);
        if (bolsin == null) {
            return NO_ENCONTRADO;
        }
        return json("[[" + numero + "," + bolsin.latitud() + "," + bolsin.longitud() + ",\"" + bolsin.fecha() + "\"]]");
    }

    private int numeroBolsin(Map<String, String> parametros) {
        try {
            return Integer.parseInt(parametros.get("numeroBolsin"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Map<String, String> leerParametros(String query) {
        Map<String, String> parametros = new HashMap<>();
        if (query != null) {
            for (String par : query.split("&")) {
                String[] claveValor = par.split("=", 2);
                if (claveValor.length == 2) {
                    parametros.put(claveValor[0], URLDecoder.decode(claveValor[1], StandardCharsets.UTF_8));
                }
            }
        }
        return parametros;
    }

    private static Respuesta json(String cuerpo) {
        return new Respuesta(200, "application/json", cuerpo);
    }

    private static Respuesta error(int estado, String codigo) {
        return new Respuesta(estado, "application/json", "{\"error\":\"" + codigo + "\",\"status\":" + estado + "}");
    }
}