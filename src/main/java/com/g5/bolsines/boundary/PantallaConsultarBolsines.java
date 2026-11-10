package com.g5.bolsines.boundary;

import com.g5.bolsines.control.GestorConsultarBolsines;
import com.g5.bolsines.entity.CM;
import com.g5.bolsines.entity.Sesion;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/consultar-bolsines")
public class PantallaConsultarBolsines {

    private final GestorConsultarBolsines gestor;
    private final String claveGoogleMaps;

    public PantallaConsultarBolsines(GestorConsultarBolsines gestor,
                                     @Value("${google.maps.api-key}") String claveGoogleMaps) {
        this.gestor = gestor;
        this.claveGoogleMaps = claveGoogleMaps;
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public String registrarOpcConsultasUbBolsines(HttpSession httpSession, Model model) {
        Sesion sesion = (Sesion) httpSession.getAttribute("sesion");
        if (sesion == null) {
            return "redirect:/inicio";
        }
        habilitarVentana(model);

        Object[] resultado = gestor.consultarUbBolsines(sesion);
        List<Map<String, Object>> ubicaciones = (List<Map<String, Object>>) resultado[1];

        mostrarCM((CM) resultado[0], model);
        mostrarMapaConUbicaciones(ubicaciones, model);
        if (!ubicaciones.isEmpty()) {
            solicitarSelecBolsin(model);
        }
        return "consultarBolsines";
    } // CU36 paso 1

    private void habilitarVentana(Model model) {
        model.addAttribute("claveGoogleMaps", claveGoogleMaps);
    } // CU36 paso 1

    private void mostrarCM(CM cm, Model model) {
        model.addAttribute("lblCMUsuario", cm.getNombre());
    } // CU36 paso 2

    private void mostrarMapaConUbicaciones(List<Map<String, Object>> ubicaciones, Model model) {
        model.addAttribute("ubicaciones", ubicaciones);
    } // CU36 paso 6

    private void solicitarSelecBolsin(Model model) {
        model.addAttribute("seleccionHabilitada", true);
    } // CU36 paso 6

    @PostMapping("/seleccion")
    @ResponseBody
    public Map<String, String> tomarSelecBolsinMapa(@RequestParam int nroBolsin) {
        gestor.tomarSelecBolsinMapa(nroBolsin);
        return solicitarEnvioCorreo();
    } // CU36 paso 7

    private Map<String, String> solicitarEnvioCorreo() {
        return Map.of("abrir", "modalConfirmacionCorreo");
    } // CU36 paso 8

    @PostMapping("/confirmacion")
    public String tomarConfirmEnvioCorreo(RedirectAttributes redirect) {
        if (gestor.tomarConfirmEnvioCorreo()) {
            informarEjecucionExitosa(redirect);
        } else {
            redirect.addFlashAttribute("mensajeError", "No se pudo enviar el correo. Volvé a consultar los bolsines.");
        }
        return "redirect:/inicio";
    } // CU36 paso 9

    private void informarEjecucionExitosa(RedirectAttributes redirect) {
        redirect.addFlashAttribute("mensajeExito", "Se notificó la ubicación del bolsín al GCM de la CM destino.");
    } // CU36 paso 11

    @PostMapping("/cancelar")
    public String cancelar() {
        gestor.finCU();
        return "redirect:/inicio";
    } // CU36 Obs. 1
}
