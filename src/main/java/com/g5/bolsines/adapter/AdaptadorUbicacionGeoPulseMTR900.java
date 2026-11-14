package com.g5.bolsines.adapter;

import com.g5.bolsines.servicio.ServicioGPSException;
import com.g5.bolsines.servicio.ServicioGeoPulseMTR900;

import java.time.DateTimeException;
import java.time.LocalDateTime;

public class AdaptadorUbicacionGeoPulseMTR900 implements IAdaptadorUbicacionBolsin {

    private final String claveAPI;
    private final ServicioGeoPulseMTR900 servicio;
    private Object[][] datosUbicacionBolsin;
    private Object[] datosUbicacionAdaptados;

    public AdaptadorUbicacionGeoPulseMTR900(String claveAPI, String urlServicio) {
        this.claveAPI = claveAPI;
        this.servicio = new ServicioGeoPulseMTR900(urlServicio);
    }

    @Override
    public Object[] recibirUbicacionBolsin(int nroBolsin, String codCMOrigen, String codCMDestino) {
        solicitarUbicacionBolsin(nroBolsin, codCMOrigen, codCMDestino);
        adaptarDatosUbicacion();
        return datosUbicacionAdaptados;
    } // CU36 pasos 4 y 5

    @Override
    public void solicitarUbicacionBolsin(int nroBolsin, String codCMOrigen, String codCMDestino) {
        try {
            datosUbicacionBolsin = servicio.fetchCargoPositions(claveAPI, nroBolsin);
        } catch (ServicioGPSException e) {
            throw new UbicacionNoDisponibleException("GeoPulse MTR-900 no informó el bolsín " + nroBolsin, e);
        }
    } // CU36 pasos 4 y 5

    @Override
    public void adaptarDatosUbicacion() {
        if (datosUbicacionBolsin.length == 0 || datosUbicacionBolsin[0].length != 4) {
            throw new UbicacionNoDisponibleException("Respuesta inválida de GeoPulse MTR-900");
        }
        Object[] fila = datosUbicacionBolsin[0];
        try {
            datosUbicacionAdaptados = new Object[]{
                    ((Number) fila[0]).intValue(),
                    ((Number) fila[1]).doubleValue(),
                    ((Number) fila[2]).doubleValue(),
                    LocalDateTime.parse((String) fila[3])
            };
        } catch (ClassCastException | NullPointerException | DateTimeException e) {
            throw new UbicacionNoDisponibleException("Respuesta inválida de GeoPulse MTR-900", e);
        }
    } // CU36 pasos 4 y 5
}