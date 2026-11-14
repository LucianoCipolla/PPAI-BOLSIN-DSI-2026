package com.g5.bolsines.boundary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Recorre Interfaz → Adaptador → Servicio por HTTP real, contra un doble de la API de la cátedra. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:gps")
class InterfazGPSTrackerTest {

    private static final ApiCatedraFalsa API_CATEDRA = new ApiCatedraFalsa();

    @DynamicPropertySource
    static void apuntarALaApiDeLaCatedra(DynamicPropertyRegistry propiedades) {
        propiedades.add("gps.base-url", API_CATEDRA::getUrl);
    }

    @Autowired
    private InterfazGPSTracker interfazGPSTracker;

    @ParameterizedTest
    @CsvSource({
            "101, CM-CBA-01, CM-ROS-02, XTR-4500L, -31.4201, -64.1888, 2026-03-07T12:30",
            "102, CM-ROS-02, CM-MDZ-03, NavTrack QX-7A, -32.9468, -60.6393, 2026-03-07T12:32:15",
            "103, CM-MDZ-03, CM-BA-01, GeoPulse MTR-900, -32.8895, -68.8458, 2026-03-07T12:35:04"})
    void cadaModeloDevuelveElMismoFormato(int nroBolsin, String codCMOrigen, String codCMDestino, String modeloGPS,
                                          double latitud, double longitud, LocalDateTime fecha) {
        Object[] ubicacion = interfazGPSTracker.buscarUbicacionBolsin(nroBolsin, codCMOrigen, codCMDestino, modeloGPS);

        assertNotNull(ubicacion);
        assertEquals(nroBolsin, ubicacion[0]);
        assertEquals(latitud, ubicacion[1]);
        assertEquals(longitud, ubicacion[2]);
        assertEquals(fecha, ubicacion[3]);
    }

    @Test
    void elBolsinQueElGPSNoConoceDevuelveNullPorFlujoA3() {
        assertNull(interfazGPSTracker.buscarUbicacionBolsin(111, "CM-CBA-01", "CM-ROS-02", "GeoPulse MTR-900"));
    }

    @Test
    void unCodigoDeCMQueNoCoincideDevuelveNull() {
        assertNull(interfazGPSTracker.buscarUbicacionBolsin(101, "CM-XXX-99", "CM-ROS-02", "XTR-4500L"));
    }

    @Test
    void unModeloDesconocidoDevuelveNull() {
        assertNull(interfazGPSTracker.buscarUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02", "Otro"));
    }
}