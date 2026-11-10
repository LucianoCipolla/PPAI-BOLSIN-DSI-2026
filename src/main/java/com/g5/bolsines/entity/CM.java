package com.g5.bolsines.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private String nombre;
    private String direccion;
    private String mail;
    private String telefono;

    protected CM() {
    }

    public CM(String codigo, String nombre, String direccion, String mail, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.mail = mail;
        this.telefono = telefono;
    }

    public String getNombre() {
        return nombre;
    } // CU36 paso 2

    public String getCodigo() {
        return codigo;
    } // CU36 pasos 4 y 5
}
