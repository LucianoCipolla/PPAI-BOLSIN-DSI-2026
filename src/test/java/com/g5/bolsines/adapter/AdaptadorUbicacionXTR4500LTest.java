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
                "{\"items\":[{\"numeroBolsin\":101,\"latitud\":-31.4201,\"longitud\":-64.1888,"
                        + "\"fechaHoraUltimaActualizacion\":\"2026-03-07T12:30:00\"}]}")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionXTR4500L("clave", proveedor.getUrl());

            Object[] ubicacion = adaptador.recibirUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02");

            assertArrayEquals(new Object[]{101, -31.4201, -64.1888, LocalDateTime.of(2026, 3, 7, 12, 30)}, ubicacion);
            assertEquals("/getBolsinLocation?numeroBolsin=101&codigoComisionMedicaOrigen=CM-CBA-01",
                    proveedor.getConsultaRecibida());
            assertEquals("clave", proveedor.getClaveRecibida());
        }
    }

    @Test
    void traduceLaFallaDelServicio() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(404,
                "{\"error\":\"CODIGO_COMISION_NO_COINCIDE\",\"status\":404}")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionXTR4500L("clave", proveedor.getUrl());

            UbicacionNoDisponibleException excepcion = assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(101, "CM-XXX-99", "CM-ROS-02"));

            assertInstanceOf(ServicioGPSException.class, excepcion.getCause());
        }
    }

    @Test
    void rechazaUnaRespuestaSinItems() throws IOException {
        try (ProveedorGPSFalso proveedor = new ProveedorGPSFalso(200, "{\"items\":[]}")) {
            IAdaptadorUbicacionBolsin adaptador = new AdaptadorUbicacionXTR4500L("clave", proveedor.getUrl());

            assertThrows(UbicacionNoDisponibleException.class,
                    () -> adaptador.recibirUbicacionBolsin(101, "CM-CBA-01", "CM-ROS-02"));
        }
    }
}
