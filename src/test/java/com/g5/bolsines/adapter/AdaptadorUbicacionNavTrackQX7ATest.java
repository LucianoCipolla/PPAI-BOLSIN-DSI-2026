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
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "1003,-32.95,-60.66,08/11/2026 10:15")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionNavTrackQX7A("clave", proveedor.getUrl());

            Object[] ubicacion = adaptador.recibirUbicacionBolsin(1003, "CBA", "CMC");

            assertArrayEquals(new Object[]{1003, -32.95, -60.66, LocalDateTime.of(2026, 11, 8, 10, 15)}, ubicacion);
            assertEquals("/retrieveTrackingData?apiKey=clave&nroBolsin=1003&codCMDestino=CMC", proveedor.getConsultaRecibida());
        }
    }

    @Test
    void traduceLaFallaDelServicio() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(503, "")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionNavTrackQX7A("clave", proveedor.getUrl());

            UbicacionNoDisponibleException excepcion = assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(1003, "CBA", "CMC"));

            assertInstanceOf(ServicioGPSException.class, excepcion.getCause());
        }
    }

    @Test
    void rechazaUnaRespuestaInvalida() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "1003,-32.95,-60.66")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionNavTrackQX7A("clave", proveedor.getUrl());

            assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(1003, "CBA", "CMC"));
        }
    }
}
