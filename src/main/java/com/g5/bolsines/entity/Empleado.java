package com.g5.bolsines.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String mail;

    @ManyToOne
    private Rol rol;

    @ManyToOne
    private CM asignadoA;

    protected Empleado() {
    }

    public Empleado(String nombre, String apellido, String mail, Rol rol, CM asignadoA) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.mail = mail;
        this.rol = rol;
        this.asignadoA = asignadoA;
    }

    public CM getCM() {
        return asignadoA;
    } // CU36 paso 2

    public boolean esTuCM(CM cm) {
        return asignadoA.getCodigo().equals(cm.getCodigo());
    } // CU36 paso 10

    public boolean sosGCM() {
        return rol.sosGCM();
    } // CU36 paso 10

    public String getMail() {
        return mail;
    } // CU36 paso 10

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    } // CU36 paso 1
}
