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
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200,
                "[[103,-32.8895,-68.8458,\"2026-03-07T12:35:04\"]]")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionGeoPulseMTR900("clave", proveedor.getUrl());

            Object[] ubicacion = adaptador.recibirUbicacionBolsin(103, "CM-MDZ-03", "CM-BA-01");

            assertArrayEquals(new Object[]{103, -32.8895, -68.8458, LocalDateTime.of(2026, 3, 7, 12, 35, 4)}, ubicacion);
            assertEquals("/fetchCargoPositions?numeroBolsin=103", proveedor.getConsultaRecibida());
            assertEquals("clave", proveedor.getClaveRecibida());
        }
    }

    @Test
    void traduceElBolsinQueElGPSNoConoce() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(404,
                "{\"error\":\"BOLSIN_NO_ENCONTRADO\",\"status\":404}")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionGeoPulseMTR900("clave", proveedor.getUrl());

            UbicacionNoDisponibleException excepcion = assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(111, "CM-CBA-01", "CM-ROS-02"));

            assertInstanceOf(ServicioGPSException.class, excepcion.getCause());
        }
    }

    @Test
    void rechazaUnaRespuestaVacia() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "[]")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionGeoPulseMTR900("clave", proveedor.getUrl());

            assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(103, "CM-MDZ-03", "CM-BA-01"));
        }
    }

    @Test
    void rechazaUnaCeldaConTipoInesperado() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200,
                "[[103,\"-32.8895\",-68.8458,\"2026-03-07T12:35:04\"]]")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionGeoPulseMTR900("clave", proveedor.getUrl());

            assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(103, "CM-MDZ-03", "CM-BA-01"));
        }
    }
}
