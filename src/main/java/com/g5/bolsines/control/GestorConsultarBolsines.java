package com.g5.bolsines.control;

import com.g5.bolsines.boundary.InterfazGPSTracker;
import com.g5.bolsines.entity.Bolsin;
import com.g5.bolsines.entity.CM;
import com.g5.bolsines.entity.Empleado;
import com.g5.bolsines.entity.Sesion;
import com.g5.bolsines.persistencia.RepositorioBolsin;
import com.g5.bolsines.persistencia.RepositorioEmpleado;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SessionScope
public class GestorConsultarBolsines {

    private final RepositorioBolsin repositorioBolsin;
    private final RepositorioEmpleado repositorioEmpleado;
    private final InterfazGPSTracker interfazGPSTracker;
    private final GestorNotificarUbicacionBolsin gestorNotificarUbicacionBolsin;

    private CM cmUsuarioLogueado;
    private List<Bolsin> bolsinesEnviados = new ArrayList<>();
    private Map<Integer, Object[]> datosLocBolsines = new HashMap<>();
    private Bolsin bolsinSeleccionado;
    private String mailGCM;

    public GestorConsultarBolsines(RepositorioBolsin repositorioBolsin, RepositorioEmpleado repositorioEmpleado, InterfazGPSTracker interfazGPSTracker, GestorNotificarUbicacionBolsin gestorNotificarUbicacionBolsin) {
        this.repositorioBolsin = repositorioBolsin;
        this.repositorioEmpleado = repositorioEmpleado;
        this.interfazGPSTracker = interfazGPSTracker;
        this.gestorNotificarUbicacionBolsin = gestorNotificarUbicacionBolsin;
    }

    public Object[] consultarUbBolsines(Sesion sesion) {
        buscarCMUsuarioLogueado(sesion);
        buscarBolsinesEnviados();
        buscarDatosLocBolsines();

        List<Map<String, Object>> ubicaciones = new ArrayList<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            Object[] datos = datosLocBolsines.get(bolsin.getNumeroBolsin());
            Map<String, Object> ubicacion = new HashMap<>();
            ubicacion.put("nroBolsin", bolsin.getNumeroBolsin());
            ubicacion.put("numeroPrecinto", bolsin.getNumeroPrecinto());
            ubicacion.put("cmDestino", bolsin.getCMDestino().getNombre());
            ubicacion.put("modeloGPS", bolsin.getModeloGPS());
            ubicacion.put("disponible", datos != null);
            if (datos != null) {
                ubicacion.put("latitud", datos[1]);
                ubicacion.put("longitud", datos[2]);
                ubicacion.put("fechaHora", datos[3]);
            }
            ubicaciones.add(ubicacion);
        }

        Object[] resultado = {cmUsuarioLogueado, ubicaciones};
        if (bolsinesEnviados.isEmpty()) {
            finCU();
        }
        return resultado;
    } // CU36 pasos 2 a 6

    private void buscarCMUsuarioLogueado(Sesion sesion) {
        cmUsuarioLogueado = sesion.getUserLog().getEmp().getCM();
    } // CU36 paso 2

    private void buscarBolsinesEnviados() {
        bolsinesEnviados = new ArrayList<>();
        for (Bolsin bolsin : repositorioBolsin.findAll()) {
            if (bolsin.esTuCMOrigen(cmUsuarioLogueado) && bolsin.sosEnviado()) {
                bolsinesEnviados.add(bolsin);
            }
        }
    } // CU36 paso 3

    private void buscarDatosLocBolsines() {
        datosLocBolsines = new HashMap<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            Object[] datos = interfazGPSTracker.buscarUbicacionBolsin(bolsin.getNumeroBolsin(),
                    bolsin.getCMOrigen().getCodigo(), bolsin.getCMDestino().getCodigo(), bolsin.getModeloGPS());
            if (datos != null) {
                datosLocBolsines.put(bolsin.getNumeroBolsin(), datos);
            }
        }
    } // CU36 pasos 4 y 5

    public void tomarSelecBolsinMapa(int nroBolsin) {
        bolsinSeleccionado = null;
        for (Bolsin bolsin : bolsinesEnviados) {
            if (bolsin.getNumeroBolsin() == nroBolsin && datosLocBolsines.containsKey(nroBolsin)) {
                bolsinSeleccionado = bolsin;
            }
        }
    } // CU36 paso 7

    public boolean tomarConfirmEnvioCorreo() {
        if (bolsinSeleccionado == null) {
            return false;
        }
        buscarMailGCM();
        boolean notificado = llamarCUNotificarUbicacionDeBolsines();
        finCU();
        return notificado;
    } // CU36 paso 9

    private void buscarMailGCM() {
        for (Empleado empleado : repositorioEmpleado.findAll()) {
            if (empleado.esTuCM(bolsinSeleccionado.getCMDestino()) && empleado.sosGCM()) {
                mailGCM = empleado.getMail();
            }
        }
    } // CU36 paso 10

    private boolean llamarCUNotificarUbicacionDeBolsines() {
        if (mailGCM == null) {
            return false;
        }
        Object[] datos = datosLocBolsines.get(bolsinSeleccionado.getNumeroBolsin());
        return gestorNotificarUbicacionBolsin.notificarUbicacion(mailGCM, bolsinSeleccionado.getNumeroBolsin(),
                (LocalDateTime) datos[3], (Double) datos[1], (Double) datos[2]);
    } // CU36 paso 11

    public void finCU() {
        cmUsuarioLogueado = null;
        bolsinesEnviados = new ArrayList<>();
        datosLocBolsines = new HashMap<>();
        bolsinSeleccionado = null;
        mailGCM = null;
    } // CU36 fin
}
