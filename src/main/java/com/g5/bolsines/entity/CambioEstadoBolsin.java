package com.g5.bolsines.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class CambioEstadoBolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    @ManyToOne
    private Estado estado;

    @ManyToOne
    private Empleado responsable;

    protected CambioEstadoBolsin() {
    }

    public CambioEstadoBolsin(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, Estado estado, Empleado responsable) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.estado = estado;
        this.responsable = responsable;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    } // CU36 paso 3

    public boolean sosEnviado() {
        return estado.sosEnviado();
    } // CU36 paso 3
}
