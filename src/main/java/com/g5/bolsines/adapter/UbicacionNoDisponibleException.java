package com.g5.bolsines.adapter;

/** El GPS Tracker no pudo informar la ubicación del bolsín (flujo A3). */
public class UbicacionNoDisponibleException extends RuntimeException {

    public UbicacionNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public UbicacionNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
