package com.g5.bolsines.adapter;

import com.g5.bolsines.servicio.ServicioGPSException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdaptadorUbicacionNavTrackQX7ATest {
    @Test
    void adaptaElTextoDelProveedorYReenviaLaCMDestino() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "102,-32.9468,-60.6393,2026-03-07T12:32:15")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionNavTrackQX7A("clave", proveedor.getUrl());

            Object[] ubicacion = adaptador.recibirUbicacionBolsin(102, "CM-ROS-02", "CM-MDZ-03");

            assertArrayEquals(new Object[]{102, -32.9468, -60.6393, LocalDateTime.of(2026, 3, 7, 12, 32, 15)}, ubicacion);
            assertEquals("/retrieveTrackingData?numeroBolsin=102&codigoComisionMedicaDestino=CM-MDZ-03",
                    proveedor.getConsultaRecibida());
            assertEquals("clave", proveedor.getClaveRecibida());
        }
    }

    @Test
    void aceptaLaFechaSinSegundos() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "101,-31.4201,-64.1888,2026-03-07T12:30")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionNavTrackQX7A("clave", proveedor.getUrl());

            Object[] ubicacion = adaptador.recibirUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02");

            assertEquals(LocalDateTime.of(2026, 3, 7, 12, 30), ubicacion[3]);
        }
    }

    @Test
    void traduceLaFallaDelServicio() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(404,
                "{\"error\":\"CODIGO_COMISION_NO_COINCIDE\",\"status\":404}")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionNavTrackQX7A("clave", proveedor.getUrl());

            UbicacionNoDisponibleException excepcion = assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(102, "CM-ROS-02", "CM-XXX-99"));

            assertInstanceOf(ServicioGPSException.class, excepcion.getCause());
        }
    }

    @Test
    void rechazaUnaRespuestaInvalida() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "102,-32.9468,-60.6393")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionNavTrackQX7A("clave", proveedor.getUrl());

            assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(102, "CM-ROS-02", "CM-MDZ-03"));
        }
    }
}
