package com.g5.bolsines.boundary;

import com.g5.bolsines.adapter.AdaptadorUbicacionGeoPulseMTR900;
import com.g5.bolsines.adapter.AdaptadorUbicacionNavTrackQX7A;
import com.g5.bolsines.adapter.AdaptadorUbicacionXTR4500L;
import com.g5.bolsines.adapter.IAdaptadorUbicacionBolsin;
import com.g5.bolsines.adapter.UbicacionNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Boundary con el GPS Tracker y Client del patrón Adapter. */
@Component
public class InterfazGPSTracker {

    // Índices paralelos: 0 = XTR-4500L, 1 = NavTrack QX-7A, 2 = GeoPulse MTR-900
    private final String[] clavesAPI;
    private final String[] urlsServicios;

    public InterfazGPSTracker(@Value("${gps.clave.xtr4500l}") String claveXTR,
                              @Value("${gps.clave.navtrack}") String claveNavTrack,
                              @Value("${gps.clave.geopulse}") String claveGeoPulse,
                              @Value("${gps.url.xtr4500l}") String urlXTR,
                              @Value("${gps.url.navtrack}") String urlNavTrack,
                              @Value("${gps.url.geopulse}") String urlGeoPulse) {
        this.clavesAPI = new String[]{claveXTR, claveNavTrack, claveGeoPulse};
        this.urlsServicios = new String[]{urlXTR, urlNavTrack, urlGeoPulse};
    }

    public Object[] buscarUbicacionBolsin(int nroBolsin, String codCMOrigen, String codCMDestino, String modeloGPS) {
        IAdaptadorUbicacionBolsin adaptador = switch (modeloGPS) {
            case "XTR-4500L" -> new AdaptadorUbicacionXTR4500L(clavesAPI[0], urlsServicios[0]);
            case "NavTrack QX-7A" -> new AdaptadorUbicacionNavTrackQX7A(clavesAPI[1], urlsServicios[1]);
            case "GeoPulse MTR-900" -> new AdaptadorUbicacionGeoPulseMTR900(clavesAPI[2], urlsServicios[2]);
            default -> null;
        };
        if (adaptador == null) {
            return null;
        }
        try {
            return adaptador.recibirUbicacionBolsin(nroBolsin, codCMOrigen, codCMDestino);
        } catch (UbicacionNoDisponibleException e) {
            return null;
        }
    } // CU36 pasos 4 y 5
}
