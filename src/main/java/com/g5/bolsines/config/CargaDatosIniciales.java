package com.g5.bolsines.config;

import com.g5.bolsines.entity.Bolsin;
import com.g5.bolsines.entity.CM;
import com.g5.bolsines.entity.Empleado;
import com.g5.bolsines.entity.Estado;
import com.g5.bolsines.entity.Rol;
import com.g5.bolsines.entity.Usuario;
import com.g5.bolsines.persistencia.RepositorioBolsin;
import com.g5.bolsines.persistencia.RepositorioCM;
import com.g5.bolsines.persistencia.RepositorioEmpleado;
import com.g5.bolsines.persistencia.RepositorioEstado;
import com.g5.bolsines.persistencia.RepositorioRol;
import com.g5.bolsines.persistencia.RepositorioUsuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Datos de prueba de la sección 9. Solo se cargan si la BD está vacía. */
@Component
public class CargaDatosIniciales implements CommandLineRunner {

    private static final String XTR = "XTR-4500L";
    private static final String NAVTRACK = "NavTrack QX-7A";
    private static final String GEOPULSE = "GeoPulse MTR-900";

    private final RepositorioEstado repositorioEstado;
    private final RepositorioCM repositorioCM;
    private final RepositorioRol repositorioRol;
    private final RepositorioEmpleado repositorioEmpleado;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioBolsin repositorioBolsin;

    private final Map<String, Estado> estados = new HashMap<>();
    private final Map<String, CM> cms = new HashMap<>();
    private final Map<String, Empleado> encargados = new HashMap<>();

    public CargaDatosIniciales(RepositorioEstado repositorioEstado, RepositorioCM repositorioCM, RepositorioRol repositorioRol, RepositorioEmpleado repositorioEmpleado, RepositorioUsuario repositorioUsuario, RepositorioBolsin repositorioBolsin) {
        this.repositorioEstado = repositorioEstado;
        this.repositorioCM = repositorioCM;
        this.repositorioRol = repositorioRol;
        this.repositorioEmpleado = repositorioEmpleado;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioBolsin = repositorioBolsin;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (repositorioEstado.count() > 0) {
            return;
        }
        cargarEstados();
        cargarCMs();
        cargarEmpleadosYUsuarios();
        cargarBolsines();
    }

    private void cargarEstados() {
        for (String nombre : List.of("Creado", "Cerrado", "Enviado", "RecibidoEnCMDestino", "DeBaja")) {
            estados.put(nombre, repositorioEstado.save(new Estado(nombre, "Bolsin", "Bolsín " + nombre)));
        }
    }

    private void cargarCMs() {
        cargarCM("CM-CBA-01", "Córdoba", "Bv. Illia 100, Córdoba", "0351-4000000");
        cargarCM("CM-CBA-02", "Córdoba Norte", "Av. Monseñor Pablo Cabrera 3000, Córdoba", "0351-4100000");
        cargarCM("CM-ROS-02", "Rosario", "Córdoba 1200, Rosario", "0341-4000000");
        cargarCM("CM-MDZ-03", "Mendoza", "San Martín 800, Mendoza", "0261-4000000");
        cargarCM("CM-BA-01", "Buenos Aires", "Av. Belgrano 1500, CABA", "011-40000000");
        cargarCM("CM-TUC-01", "Tucumán", "24 de Septiembre 600, San Miguel de Tucumán", "0381-4000000");
        cargarCM("CM-SLA-01", "Salta", "Caseros 400, Salta", "0387-4000000");
        cargarCM("CM-JUJ-01", "Jujuy", "Belgrano 700, San Salvador de Jujuy", "0388-4000000");
        cargarCM("CM-NQN-01", "Neuquén", "Av. Argentina 200, Neuquén", "0299-4000000");
        cargarCM("CM-MDQ-01", "Mar del Plata", "Av. Colón 2500, Mar del Plata", "0223-4000000");
        cargarCM("CM-LPL-01", "La Plata", "Calle 7 900, La Plata", "0221-4000000");
    }


        private void cargarCM(String codigo, String nombre, String direccion, String telefono) {
        String mail = "cm." + alias(codigo) + "@bolsines.test";
        cms.put(codigo, repositorioCM.save(new CM(codigo, "CM " + nombre, direccion, mail, telefono)));
    }

    /** "CM-CBA-01" → "cba-01", para armar usuarios y mails legibles. */
    private String alias(String codigoCM) {
        return codigoCM.substring(3).toLowerCase();
    }

    private void cargarEmpleadosYUsuarios() {
        Rol encargado = repositorioRol.save(new Rol("Encargado de Bolsines", "Gestiona los bolsines de la CM"));
        Rol gerente = repositorioRol.save(new Rol("Gerente de Comisión Médica", "Gerencia la CM"));

        for (CM cm : cms.values()) {
            String alias = alias(cm.getCodigo());
            Empleado eb = repositorioEmpleado.save(new Empleado("Encargado", cm.getCodigo(),
                    "eb." + alias + "@bolsines.test", encargado, cm));
            repositorioEmpleado.save(new Empleado("Gerente", cm.getCodigo(),
                    "gcm." + alias + "@bolsines.test", gerente, cm));
            repositorioUsuario.save(new Usuario("eb." + alias, eb));
            encargados.put(cm.getCodigo(), eb);
        }
    }

        private void cargarBolsines() {
        cargarBolsin(101, XTR, "CM-CBA-01", "CM-ROS-02", "Enviado");
        cargarBolsin(102, NAVTRACK, "CM-ROS-02", "CM-MDZ-03", "Enviado");
        cargarBolsin(103, GEOPULSE, "CM-MDZ-03", "CM-BA-01", "Enviado");
        cargarBolsin(104, XTR, "CM-BA-01", "CM-CBA-01", "Enviado");
        cargarBolsin(105, NAVTRACK, "CM-TUC-01", "CM-SLA-01", "Enviado");
        cargarBolsin(106, GEOPULSE, "CM-SLA-01", "CM-JUJ-01", "Enviado");
        cargarBolsin(107, XTR, "CM-NQN-01", "CM-MDQ-01", "Enviado");
        cargarBolsin(108, NAVTRACK, "CM-MDQ-01", "CM-BA-01", "Enviado");
        cargarBolsin(109, GEOPULSE, "CM-LPL-01", "CM-CBA-01", "Enviado");
        cargarBolsin(110, XTR, "CM-CBA-02", "CM-ROS-02", "Enviado");
        // Flujo A3: enviado según nuestro sistema, pero el GPS no lo conoce (responde 404)
        cargarBolsin(111, GEOPULSE, "CM-CBA-01", "CM-ROS-02", "Enviado");
        // No debe aparecer para eb.cba-01: sigue Cerrado, ni siquiera se consulta al GPS
        cargarBolsin(112, XTR, "CM-CBA-01", "CM-MDZ-03", "Cerrado");
    }

    private void cargarBolsin(int numero, String modeloGPS, String origen, String destino, String estadoFinal) {
        LocalDateTime creado = LocalDateTime.now().minusDays(1);
        LocalDateTime cerrado = creado.plusHours(2);
        LocalDateTime enviado = cerrado.plusHours(2);
        Empleado responsable = encargados.get(origen);

        Bolsin bolsin = new Bolsin(numero, "PR-" + numero, creado.toLocalDate(), 2.5 + numero % 10 * 0.5,
                modeloGPS, cms.get(origen), cms.get(destino));
        bolsin.agregarCambioEstado(creado, cerrado, estados.get("Creado"), responsable);
        if (estadoFinal.equals("Cerrado")) {
            bolsin.agregarCambioEstado(cerrado, null, estados.get("Cerrado"), responsable);
        } else {
            bolsin.agregarCambioEstado(cerrado, enviado, estados.get("Cerrado"), responsable);
            bolsin.agregarCambioEstado(enviado, null, estados.get("Enviado"), responsable);
        }
        repositorioBolsin.save(bolsin);
    }
}
