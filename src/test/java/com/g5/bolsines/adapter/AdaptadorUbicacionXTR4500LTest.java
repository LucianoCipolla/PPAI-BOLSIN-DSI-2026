package com.g5.bolsines.adapter;

import com.g5.bolsines.servicio.ServicioGPSException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdaptadorUbicacionXTR4500LTest {

    @Test
    void adaptaElJsonDelProveedorYReenviaLaCMOrigen() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200,
                "{\"bolsinId\":1001,\"lat\":-31.65,\"lon\":-63.9,\"lastUpdate\":\"2026-11-08T10:15:00\"}")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionXTR4500L("clave", proveedor.getUrl());

            Object[] ubicacion = adaptador.recibirUbicacionBolsin(1001, "CBA", "ROS");

            assertArrayEquals(new Object[]{1001, -31.65, -63.9, LocalDateTime.of(2026, 11, 8, 10, 15)}, ubicacion);
            assertEquals("/getBolsinLocation?apiKey=clave&nroBolsin=1001&codCMOrigen=CBA", proveedor.getConsultaRecibida());
        }
    }

    @Test
    void traduceLaFallaDelServicio() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(503, "")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionXTR4500L("clave", proveedor.getUrl());

            UbicacionNoDisponibleException excepcion = assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(1001, "CBA", "ROS"));

            assertInstanceOf(ServicioGPSException.class, excepcion.getCause());
        }
    }

    @Test
    void rechazaUnaRespuestaInvalida() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "{\"bolsinId\":1001}")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionXTR4500L("clave", proveedor.getUrl());

            assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(1001, "CBA", "ROS"));
        }
    }
}
