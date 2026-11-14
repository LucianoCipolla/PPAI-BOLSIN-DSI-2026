package com.g5.bolsines.boundary;

import com.g5.bolsines.persistencia.RepositorioSesion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo básico, A1 y A3 de punta a punta, con los datos iniciales y un doble de la API de la cátedra. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:flujos")
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class PantallaConsultarBolsinesTest {

    private static final String PANTALLA = "/consultar-bolsines";
    private static final ApiCatedraFalsa API_CATEDRA = new ApiCatedraFalsa();

    @DynamicPropertySource
    static void apuntarALaApiDeLaCatedra(DynamicPropertyRegistry propiedades) {
        propiedades.add("gps.base-url", API_CATEDRA::getUrl);
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private RepositorioSesion repositorioSesion;

    private MockHttpSession ingresarComo(String usuario, MockHttpSession actual) throws Exception {
        MvcResult resultado = mvc.perform(post("/ingresar").param("usuario", usuario).session(actual))
                .andExpect(redirectedUrl("/inicio"))
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private MockHttpSession iniciarSesion(String usuario) throws Exception {
        return ingresarComo(usuario, new MockHttpSession());
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Map<String, Object>> consultar(MockHttpSession sesion) throws Exception {
        MvcResult resultado = mvc.perform(get(PANTALLA).session(sesion))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("lblCMUsuario"))
                .andReturn();
        Map<Integer, Map<String, Object>> porNumero = new HashMap<>();
        for (Map<String, Object> ubicacion : (List<Map<String, Object>>) resultado.getModelAndView().getModel().get("ubicaciones")) {
            porNumero.put((Integer) ubicacion.get("nroBolsin"), ubicacion);
        }
        return porNumero;
    }

    @Test
    void flujoBasicoNotificaAlGCMDeLaCMDestino(CapturedOutput salida) throws Exception {
        MockHttpSession sesion = iniciarSesion("eb.cba-01");

        Map<Integer, Map<String, Object>> ubicaciones = consultar(sesion);
        assertEquals(java.util.Set.of(101, 111), ubicaciones.keySet());
        assertTrue((Boolean) ubicaciones.get(101).get("disponible"));
        assertNotNull(ubicaciones.get(101).get("latitud"));

        mvc.perform(post(PANTALLA + "/seleccion").param("nroBolsin", "101").session(sesion))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"abrir\":\"modalConfirmacionCorreo\"}"));
        mvc.perform(post(PANTALLA + "/confirmacion").session(sesion))
                .andExpect(redirectedUrl("/inicio"))
                .andExpect(flash().attributeExists("mensajeExito"));

        assertTrue(salida.getOut().contains("Mail a gcm.ros-02@bolsines.test: bolsín 101"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void cadaConsultaReemplazaLosDatosDelGestor() throws Exception {
        MockHttpSession sesion = iniciarSesion("eb.cba-01");

        consultar(sesion);
        MvcResult segunda = mvc.perform(get(PANTALLA).session(sesion)).andReturn();

        assertEquals(2, ((List<Map<String, Object>>) segunda.getModelAndView().getModel().get("ubicaciones")).size());
    }

    @Test
    void laPantallaNoMuestraElNombreDelEmpleado() throws Exception {
        MockHttpSession sesion = iniciarSesion("eb.cba-01");

        mvc.perform(get(PANTALLA).session(sesion)).andExpect(model().attributeDoesNotExist("nombreEmpleado"));
    }

    @Test
    void a1SinBolsinesEnviadosMuestraElEstadoVacio() throws Exception {
        MockHttpSession sesion = iniciarSesion("eb.juj-01");

        MvcResult resultado = mvc.perform(get(PANTALLA).session(sesion))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("seleccionHabilitada"))
                .andReturn();

        assertTrue(resultado.getResponse().getContentAsString().contains("No hay bolsines enviados desde CM Jujuy"));
    }

    @Test
    void a3ElBolsinSinUbicacionNoSePuedeNotificar() throws Exception {
        MockHttpSession sesion = iniciarSesion("eb.cba-01");

        Map<Integer, Map<String, Object>> ubicaciones = consultar(sesion);
        assertFalse((Boolean) ubicaciones.get(111).get("disponible"));
        assertNull(ubicaciones.get(111).get("latitud"));

        mvc.perform(post(PANTALLA + "/seleccion").param("nroBolsin", "111").session(sesion));
        mvc.perform(post(PANTALLA + "/confirmacion").session(sesion))
                .andExpect(flash().attributeExists("mensajeError"));
    }

    @Test
    void cancelarLimpiaElGestorYVuelveAInicio() throws Exception {
        MockHttpSession sesion = iniciarSesion("eb.cba-01");
        consultar(sesion);
        mvc.perform(post(PANTALLA + "/seleccion").param("nroBolsin", "101").session(sesion));

        mvc.perform(post(PANTALLA + "/cancelar").session(sesion)).andExpect(redirectedUrl("/inicio"));

        mvc.perform(post(PANTALLA + "/confirmacion").session(sesion))
                .andExpect(flash().attributeExists("mensajeError"));
    }

    @Test
    void sinSesionRedirigeAInicio() throws Exception {
        mvc.perform(get(PANTALLA)).andExpect(redirectedUrl("/inicio"));
    }

    @Test
    void inicioSinSesionOfreceLosUsuariosYNoLaConsulta() throws Exception {
        String html = mvc.perform(get("/inicio"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(html.contains("Ingresar como"));
        assertTrue(html.contains("value=\"eb.cba-01\""));
        assertTrue(html.contains("value=\"eb.juj-01\""));
        assertFalse(html.contains("id=\"opcConsultarBolsines\""));
    }

    @Test
    void elegirUnUsuarioPersisteLaSesionYHabilitaLaConsulta() throws Exception {
        long sesionesPrevias = repositorioSesion.count();

        MockHttpSession sesion = iniciarSesion("eb.cba-01");

        assertEquals(sesionesPrevias + 1, repositorioSesion.count());
        String html = mvc.perform(get("/inicio").session(sesion))
                .andExpect(status().isOk())
                .andExpect(model().attribute("usuarioActual", "eb.cba-01"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(html.contains("id=\"opcConsultarBolsines\""));
    }

    @Test
    void cambiarDeUsuarioNoArrastraElCUDelAnterior() throws Exception {
        MockHttpSession sesionCba = iniciarSesion("eb.cba-01");
        consultar(sesionCba);
        mvc.perform(post(PANTALLA + "/seleccion").param("nroBolsin", "101").session(sesionCba));

        MockHttpSession sesionJuj = ingresarComo("eb.juj-01", sesionCba);

        mvc.perform(post(PANTALLA + "/confirmacion").session(sesionJuj))
                .andExpect(flash().attributeExists("mensajeError"));
        assertTrue(consultar(sesionJuj).isEmpty());
    }

    @Test
    void unUsuarioInexistenteNoCreaSesion() throws Exception {
        long sesionesPrevias = repositorioSesion.count();

        mvc.perform(post("/ingresar").param("usuario", "nadie"))
                .andExpect(redirectedUrl("/inicio"))
                .andExpect(flash().attributeExists("mensajeError"));

        assertEquals(sesionesPrevias, repositorioSesion.count());
    }

    @Test
    void cerrarSesionVuelveAInicioSinUsuario() throws Exception {
        MockHttpSession sesion = iniciarSesion("eb.cba-01");

        mvc.perform(post("/logout").session(sesion)).andExpect(redirectedUrl("/inicio"));

        mvc.perform(get(PANTALLA)).andExpect(redirectedUrl("/inicio"));
    }

    @Test
    void yaNoExisteLaPantallaDeLogin() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isNotFound());
    }
}