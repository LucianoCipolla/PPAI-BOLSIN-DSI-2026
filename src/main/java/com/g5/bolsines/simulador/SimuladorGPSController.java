package com.g5.bolsines.simulador;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * API GPS falsa: hace de sistema de cada proveedor hasta tener la API de la cátedra.
 * Cada modelo responde en su propio formato, para que cada adaptador tenga trabajo real.
 */
@RestController
@RequestMapping("/simulador-gps")
@ConditionalOnProperty(name = "gps.simulador.habilitado", havingValue = "true")
public class SimuladorGPSController {

    private static final ZoneId ZONA_ARGENTINA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final DateTimeFormatter FORMATO_NAVTRACK = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int BOLSIN_CON_FALLA = 1007;
    private static final int MINUTOS_POR_VIAJE = 240;

    private static final double[] CORDOBA = {-31.4201, -64.1888};
    private static final double[] ROSARIO = {-32.9468, -60.6393};
    private static final double[] MENDOZA = {-32.8895, -68.8458};
    private static final double[] PARANA = {-31.7319, -60.5297};
    private static final double[] VILLA_MARIA = {-32.4075, -63.2402};
    private static final double[] CABA = {-34.6037, -58.3816};

    /** nroBolsin → {lat/lng origen, lat/lng destino}, como lo guardaría el sistema del proveedor. */
    private static final Map<Integer, double[][]> VIAJES = Map.of(
            1001, new double[][]{CORDOBA, ROSARIO},
            1002, new double[][]{CORDOBA, MENDOZA},
            1003, new double[][]{CORDOBA, CABA},
            1004, new double[][]{CORDOBA, VILLA_MARIA},
            1005, new double[][]{CORDOBA, PARANA},
            1006, new double[][]{CORDOBA, MENDOZA},
            1007, new double[][]{CORDOBA, ROSARIO},
            2001, new double[][]{ROSARIO, CORDOBA});

    private final String claveXTR;
    private final String claveNavTrack;
    private final String claveGeoPulse;

    public SimuladorGPSController(@Value("${gps.clave.xtr4500l}") String claveXTR, @Value("${gps.clave.navtrack}") String claveNavTrack, @Value("${gps.clave.geopulse}") String claveGeoPulse) {
        this.claveXTR = claveXTR;
        this.claveNavTrack = claveNavTrack;
        this.claveGeoPulse = claveGeoPulse;
    }

    @GetMapping("/xtr4500l/getBolsinLocation")
    public Map<String, Object> getBolsinLocation(@RequestParam String apiKey, @RequestParam int nroBolsin, @RequestParam String codCMOrigen) {
        validarConsulta(apiKey, claveXTR, nroBolsin);
        double[] posicion = calcularPosicion(nroBolsin);
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("bolsinId", nroBolsin);
        respuesta.put("lat", posicion[0]);
        respuesta.put("lon", posicion[1]);
        respuesta.put("lastUpdate", calcularUltimaLectura(nroBolsin).toString());
        return respuesta;
    } // CU36 pasos 4 y 5

    @GetMapping("/navtrack/retrieveTrackingData")
    public String retrieveTrackingData(@RequestParam String apiKey, @RequestParam int nroBolsin, @RequestParam String codCMDestino) {
        validarConsulta(apiKey, claveNavTrack, nroBolsin);
        double[] posicion = calcularPosicion(nroBolsin);
        return String.format(Locale.US, "%d,%.4f,%.4f,%s", nroBolsin, posicion[0], posicion[1],
                calcularUltimaLectura(nroBolsin).format(FORMATO_NAVTRACK));
    } // CU36 pasos 4 y 5

    @GetMapping("/geopulse/fetchCargoPositions")
    public String[][] fetchCargoPositions(@RequestParam String apiKey, @RequestParam int nroBolsin) {
        validarConsulta(apiKey, claveGeoPulse, nroBolsin);
        double[] posicion = calcularPosicion(nroBolsin);
        long epoch = calcularUltimaLectura(nroBolsin).atZone(ZONA_ARGENTINA).toEpochSecond();
        return new String[][]{{String.valueOf(nroBolsin), String.format(Locale.US, "%.4f", posicion[0]),
                String.format(Locale.US, "%.4f", posicion[1]), String.valueOf(epoch)}};
    } // CU36 pasos 4 y 5

    private void validarConsulta(String apiKey, String claveEsperada, int nroBolsin) {
        if (!claveEsperada.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (nroBolsin == BOLSIN_CON_FALLA) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (!VIAJES.containsKey(nroBolsin)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    } // CU36 pasos 4 y 5

    /** Posición intermedia entre origen y destino, según el minuto actual dentro del viaje. */
    private double[] calcularPosicion(int nroBolsin) {
        double[][] viaje = VIAJES.get(nroBolsin);
        long minutos = LocalDateTime.now(ZONA_ARGENTINA).toLocalTime().toSecondOfDay() / 60;
        double avance = (minutos + nroBolsin * 37L) % MINUTOS_POR_VIAJE / (double) MINUTOS_POR_VIAJE;
        return new double[]{
                redondear(viaje[0][0] + (viaje[1][0] - viaje[0][0]) * avance),
                redondear(viaje[0][1] + (viaje[1][1] - viaje[0][1]) * avance)};
    } // CU36 pasos 4 y 5

    private double redondear(double coordenada) {
        return Math.round(coordenada * 10000) / 10000.0;
    } // CU36 pasos 4 y 5

    /** Cada bolsín reportó hace unos minutos distintos, para que la lista no muestre todos iguales. */
    private LocalDateTime calcularUltimaLectura(int nroBolsin) {
        return LocalDateTime.now(ZONA_ARGENTINA).minusMinutes(nroBolsin % 20).truncatedTo(ChronoUnit.MINUTES);
    } // CU36 pasos 4 y 5
}
