package com.g5.bolsines.config;

import com.g5.bolsines.entity.Sesion;
import com.g5.bolsines.entity.Usuario;
import com.g5.bolsines.persistencia.RepositorioSesion;
import com.g5.bolsines.persistencia.RepositorioUsuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

/** Inicio con sesión simulada: iniciar sesión es el CU 5, fuera del alcance del grupo. */
@Controller
public class PantallaInicio {

    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioSesion repositorioSesion;

    public PantallaInicio(RepositorioUsuario repositorioUsuario, RepositorioSesion repositorioSesion) {
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioSesion = repositorioSesion;
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/inicio";
    }

    @GetMapping("/inicio")
    public String mostrarInicio(HttpSession httpSession, Model model) {
        model.addAttribute("usuarios", repositorioUsuario.findAll(Sort.by("usuario")));
        Sesion sesion = (Sesion) httpSession.getAttribute("sesion");
        if (sesion != null) {
            model.addAttribute("usuarioActual", sesion.getUserLog().getUsuario());
            model.addAttribute("nombreEmpleado", sesion.getUserLog().getEmp().getNombreCompleto());
        }
        return "inicio";
    }

    @PostMapping("/ingresar")
    public String ingresarComo(@RequestParam String usuario, HttpServletRequest request, RedirectAttributes redirect) {
        Optional<Usuario> encontrado = repositorioUsuario.findByUsuario(usuario);
        if (encontrado.isEmpty()) {
            redirect.addFlashAttribute("mensajeError", "El usuario elegido no existe.");
            return "redirect:/inicio";
        }
        // Una HttpSession nueva descarta el gestor (@SessionScope) del usuario anterior
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        Sesion sesion = repositorioSesion.save(new Sesion(LocalDateTime.now(), encontrado.get()));
        request.getSession().setAttribute("sesion", sesion);
        return "redirect:/inicio";
    }

    @PostMapping("/logout")
    public String cerrarSesion(HttpSession httpSession) {
        httpSession.invalidate();
        return "redirect:/inicio";
    }
}
