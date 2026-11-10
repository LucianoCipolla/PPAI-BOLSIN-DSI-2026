package com.g5.bolsines.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Recorre la cadena completa contra el simulador por HTTP real, con su propio puerto y BD en memoria. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
        "server.port=18081",
        "gps.base-url=http://localhost:18081/simulador-gps",
        "spring.datasource.url=jdbc:h2:mem:gps"})
class InterfazGPSTrackerTest {

    @Autowired
    private InterfazGPSTracker interfazGPSTracker;

    @Autowired
    private TestRestTemplate rest;

    @ParameterizedTest
    @CsvSource({
            "1001, XTR-4500L",
            "1002, XTR-4500L",
            "1003, NavTrack QX-7A",
            "1004, NavTrack QX-7A",
            "1005, GeoPulse MTR-900"})
    void cadaModeloDevuelveElMismoFormato(int nroBolsin, String modeloGPS) {
        Object[] ubicacion = interfazGPSTracker.buscarUbicacionBolsin(nroBolsin, "CBA", "ROS", modeloGPS);

        assertNotNull(ubicacion);
        assertEquals(4, ubicacion.length);
        assertEquals(nroBolsin, ubicacion[0]);
        assertInstanceOf(Double.class, ubicacion[1]);
        assertInstanceOf(Double.class, ubicacion[2]);
        assertInstanceOf(LocalDateTime.class, ubicacion[3]);
    }

    @Test
    void elBolsin1007DevuelveNullPorFlujoA3() {
        assertNull(interfazGPSTracker.buscarUbicacionBolsin(1007, "CBA", "ROS", "GeoPulse MTR-900"));
    }

    @Test
    void unModeloDesconocidoDevuelveNull() {
        assertNull(interfazGPSTracker.buscarUbicacionBolsin(1001, "CBA", "ROS", "Otro"));
    }

    @Test
    void elSimuladorRechazaUnaClaveIncorrecta() {
        var respuesta = rest.getForEntity(
                "/simulador-gps/xtr4500l/getBolsinLocation?apiKey=mala&nroBolsin=1001&codCMOrigen=CBA", String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
    }

    @Test
    void elSimuladorDevuelve503ParaElBolsin1007() {
        var respuesta = rest.getForEntity(
                "/simulador-gps/geopulse/fetchCargoPositions?apiKey=clave-geopulse-g5&nroBolsin=1007", String.class);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, respuesta.getStatusCode());
    }
}
