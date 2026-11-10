package com.g5.bolsines.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Bolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private int numeroBolsin;

    private String numeroPrecinto;
    private LocalDate fecha;
    private double peso;
    private String modeloGPS;

    @ManyToOne
    private CM cmOrigen;

    @ManyToOne
    private CM cmDestino;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "bolsin_id")
    private List<CambioEstadoBolsin> cambioEstado = new ArrayList<>();

    protected Bolsin() {
    }

    public Bolsin(int numeroBolsin, String numeroPrecinto, LocalDate fecha, double peso, String modeloGPS, CM cmOrigen, CM cmDestino) {
        this.numeroBolsin = numeroBolsin;
        this.numeroPrecinto = numeroPrecinto;
        this.fecha = fecha;
        this.peso = peso;
        this.modeloGPS = modeloGPS;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
    }

    public void agregarCambioEstado(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, Estado estado, Empleado responsable) {
        cambioEstado.add(new CambioEstadoBolsin(fechaHoraInicio, fechaHoraFin, estado, responsable));
    } // CU36 sin paso: solo la usa CargaDatosIniciales

    public boolean esTuCMOrigen(CM cm) {
        return cmOrigen.getCodigo().equals(cm.getCodigo());
    } // CU36 paso 3

    public boolean sosEnviado() {
        for (CambioEstadoBolsin cambio : cambioEstado) {
            if (cambio.sosActual()) {
                return cambio.sosEnviado();
            }
        }
        return false;
    } // CU36 paso 3

    public int getNumeroBolsin() {
        return numeroBolsin;
    } // CU36 pasos 4 y 5

    public String getNumeroPrecinto() {
        return numeroPrecinto;
    } // CU36 paso 6

    public CM getCMDestino() {
        return cmDestino;
    } // CU36 pasos 4 y 5

    public CM getCMOrigen() {
        return cmOrigen;
    } // CU36 pasos 4 y 5

    public String getModeloGPS() {
        return modeloGPS;
    } // CU36 pasos 4 y 5
}
