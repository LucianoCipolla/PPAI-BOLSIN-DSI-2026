package com.g5.bolsines.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String ambito;
    private String descripcion;

    protected Estado() {
    }

    public Estado(String nombre, String ambito, String descripcion) {
        this.nombre = nombre;
        this.ambito = ambito;
        this.descripcion = descripcion;
    }

    public boolean sosEnviado() {
        return nombre.equals("Enviado");
    } // CU36 paso 3
}
