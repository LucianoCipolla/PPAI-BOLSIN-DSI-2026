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
        cargarCM("CBA", "Córdoba", "Bv. Illia 100, Córdoba", "0351-4000000");
        cargarCM("ROS", "Rosario", "Córdoba 1200, Rosario", "0341-4000000");
        cargarCM("MDZ", "Mendoza", "San Martín 800, Mendoza", "0261-4000000");
        cargarCM("PNA", "Paraná", "Urquiza 500, Paraná", "0343-4000000");
        cargarCM("VMA", "Villa María", "Bv. España 300, Villa María", "0353-4000000");
        cargarCM("CMC", "Central CABA", "Av. Belgrano 1500, CABA", "011-40000000");
    }

    private void cargarCM(String codigo, String nombre, String direccion, String telefono) {
        String mail = "cm." + codigo.toLowerCase() + "@bolsines.test";
        cms.put(codigo, repositorioCM.save(new CM(codigo, "CM " + nombre, direccion, mail, telefono)));
    }

    private void cargarEmpleadosYUsuarios() {
        Rol encargado = repositorioRol.save(new Rol("Encargado de Bolsines", "Gestiona los bolsines de la CM"));
        Rol gerente = repositorioRol.save(new Rol("Gerente de Comisión Médica", "Gerencia la CM"));

        for (CM cm : cms.values()) {
            String codigo = cm.getCodigo().toLowerCase();
            Empleado eb = repositorioEmpleado.save(new Empleado("Encargado", cm.getCodigo(),
                    "eb." + codigo + "@bolsines.test", encargado, cm));
            repositorioEmpleado.save(new Empleado("Gerente", cm.getCodigo(),
                    "gcm." + codigo + "@bolsines.test", gerente, cm));
            repositorioUsuario.save(new Usuario("eb." + codigo, eb));
            encargados.put(cm.getCodigo(), eb);
        }
    }

    private void cargarBolsines() {
        cargarBolsin(1001, XTR, "CBA", "ROS", "Enviado");
        cargarBolsin(1002, XTR, "CBA", "MDZ", "Enviado");
        cargarBolsin(1003, NAVTRACK, "CBA", "CMC", "Enviado");
        cargarBolsin(1004, NAVTRACK, "CBA", "VMA", "Enviado");
        cargarBolsin(1005, GEOPULSE, "CBA", "PNA", "Enviado");
        cargarBolsin(1007, GEOPULSE, "CBA", "ROS", "Enviado");
        // No deben aparecer para eb.cba: 1006 sigue Cerrado y 2001 salió de Rosario
        cargarBolsin(1006, XTR, "CBA", "MDZ", "Cerrado");
        cargarBolsin(2001, XTR, "ROS", "CBA", "Enviado");
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
