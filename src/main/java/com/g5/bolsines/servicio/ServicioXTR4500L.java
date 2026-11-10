package com.g5.bolsines.servicio;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Adaptee: expone la API del GPS XTR-4500L tal como la define el proveedor. */
public class ServicioXTR4500L {

    private final RestClient restClient;

    public ServicioXTR4500L(String urlServicio) {
        SimpleClientHttpRequestFactory timeouts = new SimpleClientHttpRequestFactory();
        timeouts.setConnectTimeout(2000);
        timeouts.setReadTimeout(5000);
        this.restClient = RestClient.builder().baseUrl(urlServicio).requestFactory(timeouts).build();
    }

    public String getBolsinLocation(String claveAPI, int nroBolsin, String codCMOrigen) {
        String respuesta;
        try {
            respuesta = restClient.get()
                    .uri(uri -> uri.path("/getBolsinLocation")
                            .queryParam("apiKey", claveAPI)
                            .queryParam("nroBolsin", nroBolsin)
                            .queryParam("codCMOrigen", codCMOrigen)
                            .build())
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            throw new ServicioGPSException("XTR-4500L no informó el bolsín " + nroBolsin, e);
        }
        if (respuesta == null) {
            throw new ServicioGPSException("XTR-4500L respondió vacío para el bolsín " + nroBolsin);
        }
        return respuesta;
    } // CU36 pasos 4 y 5
}
