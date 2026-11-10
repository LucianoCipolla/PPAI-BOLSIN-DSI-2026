package com.g5.bolsines.boundary;

import com.g5.bolsines.adapter.AdaptadorUbicacionGeoPulseMTR900;
import com.g5.bolsines.adapter.AdaptadorUbicacionNavTrackQX7A;
import com.g5.bolsines.adapter.AdaptadorUbicacionXTR4500L;
import com.g5.bolsines.adapter.IAdaptadorUbicacionBolsin;
import com.g5.bolsines.adapter.UbicacionNoDisponibleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Boundary con el GPS Tracker y Client del patrón Adapter. */
@Component
public class InterfazGPSTracker {

    private static final String XTR = "XTR-4500L";
    private static final String NAVTRACK = "NavTrack QX-7A";
    private static final String GEOPULSE = "GeoPulse MTR-900";

    private final Map<String, String> clavesAPI;
    private final Map<String, String> urlsServicios;

    public InterfazGPSTracker(@Value("${gps.clave.xtr4500l}") String claveXTR,
                              @Value("${gps.clave.navtrack}") String claveNavTrack,
                              @Value("${gps.clave.geopulse}") String claveGeoPulse,
                              @Value("${gps.url.xtr4500l}") String urlXTR,
                              @Value("${gps.url.navtrack}") String urlNavTrack,
                              @Value("${gps.url.geopulse}") String urlGeoPulse) {
        this.clavesAPI = Map.of(XTR, claveXTR, NAVTRACK, claveNavTrack, GEOPULSE, claveGeoPulse);
        this.urlsServicios = Map.of(XTR, urlXTR, NAVTRACK, urlNavTrack, GEOPULSE, urlGeoPulse);
    }

    public Object[] buscarUbicacionBolsin(int nroBolsin, String codCMOrigen, String codCMDestino, String modeloGPS) {
        String clave = clavesAPI.get(modeloGPS);
        String url = urlsServicios.get(modeloGPS);
        IAdaptadorUbicacionBolsin adaptador = switch (modeloGPS) {
            case XTR -> new AdaptadorUbicacionXTR4500L(clave, url);
            case NAVTRACK -> new AdaptadorUbicacionNavTrackQX7A(clave, url);
            case GEOPULSE -> new AdaptadorUbicacionGeoPulseMTR900(clave, url);
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
