package com.g5.bolsines.servicio;

/** Falla de la API de un proveedor de GPS. Pertenece a los adaptees: no conoce el patrón Adapter. */
public class ServicioGPSException extends RuntimeException {

    public ServicioGPSException(String mensaje) {
        super(mensaje);
    }

    public ServicioGPSException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
