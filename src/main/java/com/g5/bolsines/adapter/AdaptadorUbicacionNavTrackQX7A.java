package com.g5.bolsines.adapter;

import com.g5.bolsines.servicio.ServicioGPSException;
import com.g5.bolsines.servicio.ServicioNavTrackQX7A;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class AdaptadorUbicacionNavTrackQX7A implements IAdaptadorUbicacionBolsin {
    private final String claveAPI;
    private final ServicioNavTrackQX7A servicio;
    private String datosUbicacionBolsin;
    private Object[] datosUbicacionAdaptados;

    public AdaptadorUbicacionNavTrackQX7A(String claveAPI, String urlServicio) {
        this.claveAPI = claveAPI;
        this.servicio = new ServicioNavTrackQX7A(urlServicio);
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
            datosUbicacionBolsin = servicio.retrieveTrackingData(claveAPI, nroBolsin, codCMDestino);
        } catch (ServicioGPSException e) {
            throw new UbicacionNoDisponibleException("NavTrack QX-7A no informó el bolsín " + nroBolsin, e);
        }
    } // CU36 pasos 4 y 5

    @Override
    public void adaptarDatosUbicacion() {
        String[] campos = datosUbicacionBolsin.split(",");
        if (campos.length != 4) {
            throw new UbicacionNoDisponibleException("Respuesta inválida de NavTrack QX-7A: " + datosUbicacionBolsin);
        }
        try {
            datosUbicacionAdaptados = new Object[]{
                    Integer.valueOf(campos[0]),
                    Double.valueOf(campos[1]),
                    Double.valueOf(campos[2]),
                    LocalDateTime.parse(campos[3])
            };
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new UbicacionNoDisponibleException("Respuesta inválida de NavTrack QX-7A: " + datosUbicacionBolsin, e);
        }
    } // CU36 pasos 4 y 5
}
