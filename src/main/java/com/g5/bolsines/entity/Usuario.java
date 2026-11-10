package com.g5.bolsines.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuario;

    @OneToOne
    private Empleado empleado;

    protected Usuario() {
    }

    public Usuario(String usuario, Empleado empleado) {
        this.usuario = usuario;
        this.empleado = empleado;
    }

    public Empleado getEmp() {
        return empleado;
    } // CU36 paso 2

    public String getUsuario() {
        return usuario;
    } // CU36 sin paso: lo usa el desplegable "Ingresar como"
}
