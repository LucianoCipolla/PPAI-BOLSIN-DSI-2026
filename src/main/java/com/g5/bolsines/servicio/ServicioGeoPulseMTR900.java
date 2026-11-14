package com.g5.bolsines.servicio;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Adaptee: expone la API del GPS GeoPulse MTR-900 tal como la define el proveedor. */
public class ServicioGeoPulseMTR900 {

    private final RestClient restClient;

    public ServicioGeoPulseMTR900(String urlServicio) {
        SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(2000);
        timeouts.setReadTimeout(5000);
        this.restClient = RestClient.builder().baseUrl(urlServicio).requestFactory(timeouts).build();
    }

        public Object[][] fetchCargoPositions(String claveAPI, int nroBolsin) {
        Object[][] respuesta;
        try {
            respuesta = restClient.get()
                    .uri(uri -> uri.path("/fetchCargoPositions")
                            .queryParam("numeroBolsin", nroBolsin)
                            .build())
                    .header("X-API-Key", claveAPI)
                    .retrieve()
                    .body(Object[][].class);
        } catch (RestClientException e) {
            throw new ServicioGPSException("GeoPulse MTR-900 no informó el bolsín " + nroBolsin, e);
        }
        if (respuesta == null) {
            throw new ServicioGPSException("GeoPulse MTR-900 respondió vacío para el bolsín " + nroBolsin);
        }
        return respuesta;
    } // CU36 pasos 4 y 5
}
