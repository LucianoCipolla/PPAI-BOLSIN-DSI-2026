package com.g5.bolsines.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** CU 30 incluido – no es alcance del grupo */
@Service
public class GestorNotificarUbicacionBolsin {

    private static final Logger log = LoggerFactory.getLogger(GestorNotificarUbicacionBolsin.class);

    public boolean notificarUbicacion(String mailGCM, int nroBolsin, LocalDateTime fechaHora,
                                      double latitud, double longitud) {
        log.info("[CU 30 simulado] Mail a {}: bolsín {} en ({}, {}) a las {}",
                mailGCM, nroBolsin, latitud, longitud, fechaHora);
        return true;
    } // CU36 paso 11
}
