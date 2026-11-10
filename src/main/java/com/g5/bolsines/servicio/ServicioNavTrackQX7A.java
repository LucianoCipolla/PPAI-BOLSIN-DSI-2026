package com.g5.bolsines.servicio;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Adaptee: expone la API del GPS NavTrack QX-7A tal como la define el proveedor. */
public class ServicioNavTrackQX7A {

    private final RestClient restClient;

    public ServicioNavTrackQX7A(String urlServicio) {
        SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(2000);
        timeouts.setReadTimeout(5000);
        this.restClient = RestClient.builder().baseUrl(urlServicio).requestFactory(timeouts).build();
    }

    public String retrieveTrackingData(String claveAPI, int nroBolsin, String codCMDestino) {
        String respuesta;
        try {
            respuesta = restClient.get()
                    .uri(uri -> uri.path("/retrieveTrackingData")
                            .queryParam("apiKey", claveAPI)
                            .queryParam("nroBolsin", nroBolsin)
                            .queryParam("codCMDestino", codCMDestino)
                            .build())
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new ServicioGPSException("NavTrack QX-7A no informó el bolsín " + nroBolsin, e);
        }
        if (respuesta == null) {
            throw new ServicioGPSException("NavTrack QX-7A respondió vacío para el bolsín " + nroBolsin);
        }
        return respuesta;
    } // CU36 pasos 4 y 5
}
