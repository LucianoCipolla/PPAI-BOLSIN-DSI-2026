package com.g5.bolsines.adapter;

import com.g5.bolsines.servicio.ServicioGPSException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdaptadorUbicacionGeoPulseMTR900Test {

    @Test
    void adaptaLaMatrizDelProveedorYReenviaSoloElNumero() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "[[\"1005\",\"-33.12\",\"-64.35\",\"1794143700\"]]")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionGeoPulseMTR900("clave", proveedor.getUrl());

            Object[] ubicacion = adaptador.recibirUbicacionBolsin(1005, "CBA", "PNA");

            assertArrayEquals(new Object[]{1005, -33.12, -64.35, LocalDateTime.of(2026, 11, 8, 10, 15)}, ubicacion);
            assertEquals("/fetchCargoPositions?apiKey=clave&nroBolsin=1005", proveedor.getConsultaRecibida());
        }
    }

    @Test
    void traduceLaFallaDelServicio() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(503, "")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionGeoPulseMTR900("clave", proveedor.getUrl());

            UbicacionNoDisponibleException excepcion = assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(1005, "CBA", "PNA"));

            assertInstanceOf(ServicioGPSException.class, excepcion.getCause());
        }
    }

    @Test
    void rechazaUnaRespuestaInvalida() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "[]")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionGeoPulseMTR900("clave", proveedor.getUrl());

            assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(1005, "CBA", "PNA"));
        }
    }
}
