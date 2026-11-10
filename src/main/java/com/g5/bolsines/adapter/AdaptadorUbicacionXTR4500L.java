package com.g5.bolsines.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.g5.bolsines.servicio.ServicioGPSException;
import com.g5.bolsines.servicio.ServicioXTR4500L;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class AdaptadorUbicacionXTR4500L implements IAdaptadorUbicacionBolsin {

    private final String claveAPI;
    private final ServicioXTR4500L servicio;
    private String datosUbicacionBolsin;
    private Object[] datosUbicacionAdaptados;

    public AdaptadorUbicacionXTR4500L(String claveAPI, String urlServicio) {
        this.claveAPI = claveAPI;
        this.servicio = new ServicioXTR4500L(urlServicio);
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
            datosUbicacionBolsin = servicio.getBolsinLocation(claveAPI, nroBolsin, codCMOrigen);
        } catch (ServicioGPSException e) {
            throw new UbicacionNoDisponibleException("XTR-4500L no informó el bolsín " + nroBolsin, e);
        }
    } // CU36 pasos 4 y 5

    @Override
    public void adaptarDatosUbicacion() {
        try {
            JsonNode json = new ObjectMapper().readTree(datosUbicacionBolsin);
            datosUbicacionAdaptados = new Object[]{
                    json.required("bolsinId").asInt(),
                    json.required("lat").asDouble(),
                    json.required("lon").asDouble(),
                    LocalDateTime.parse(json.required("lastUpdate").asText())
            };
        } catch (JsonProcessingException | IllegalArgumentException | DateTimeParseException e) {
            throw new UbicacionNoDisponibleException("Respuesta inválida de XTR-4500L: " + datosUbicacionBolsin, e);
        }
    } // CU36 pasos 4 y 5
}
