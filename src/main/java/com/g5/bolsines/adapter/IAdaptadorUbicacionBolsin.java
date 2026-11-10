package com.g5.bolsines.adapter;

/** Target del patrón Adapter: lo que InterfazGPSTracker (Client) espera de cualquier modelo de GPS. */
public interface IAdaptadorUbicacionBolsin {

    Object[] recibirUbicacionBolsin(int nroBolsin, String codCMOrigen, String codCMDestino); // CU36 pasos 4 y 5

    void solicitarUbicacionBolsin(int nroBolsin, String codCMOrigen, String codCMDestino); // CU36 pasos 4 y 5

    void adaptarDatosUbicacion(); // CU36 pasos 4 y 5
}
