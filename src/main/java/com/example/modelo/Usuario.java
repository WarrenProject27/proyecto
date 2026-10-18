package com.example.modelo;

public class Usuario {
    private final int id;
    private final String nombreUsuario;
    private String nombre;
    private String email;
    private String rol;

    public Usuario(int id, String nombreUsuario, String nombre, String email, String rol) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
    }

    public int getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}
