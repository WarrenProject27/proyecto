package com.example.logica;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import com.example.modelo.Programa;
import com.example.persistencia.Programas;

public class Funciones {

    private final Programas programas;

    public Funciones() {
        programas = new Programas();
    }

    public List<Programa> obtenerProgramas() throws SQLException {
        return programas.listar();
    }

    public boolean datosValidos(String asignatura, String grado, String nivel) {
        return asignatura != null && !asignatura.isBlank()
            && grado != null && !grado.isBlank()
            && nivel != null && !nivel.isBlank();
    }

    public Programa crearPrograma(
        String asignatura, String grado, String nivel, String docente, File archivoPdf
    ) {
        validarDatos(asignatura, grado, nivel);
        return new Programa(0, asignatura.trim(), grado, nivel, docente, archivoPdf);
    }

    public Programa agregarPrograma(Programa programa) throws SQLException {
        if (programa == null) {
            throw new IllegalArgumentException("El programa es obligatorio.");
        }
        return programas.insertar(programa);
    }

    public Programa editarPrograma(
        Programa original, String asignatura, String grado, String nivel, File archivoPdf
    ) {
        if (original == null) {
            throw new IllegalArgumentException("Debe especificarse el programa a editar.");
        }
        validarDatos(asignatura, grado, nivel);
        return new Programa(
            original.id(), asignatura.trim(), grado, nivel, original.docente(), archivoPdf
        );
    }

    public void actualizarPrograma(Programa actualizado) throws SQLException {
        if (actualizado == null) {
            throw new IllegalArgumentException(
                "El programa actualizado es obligatorio."
            );
        }
        if (!programas.actualizar(actualizado)) {
            throw new IllegalStateException(
                "No se encontró el programa que se intentaba actualizar."
            );
        }
    }

    public boolean eliminarPrograma(Programa programa) throws SQLException {
        if (programa == null) {
            throw new IllegalArgumentException("El programa es obligatorio.");
        }
        return programas.eliminar(programa.id());
    }

    public boolean coincideConFiltros(
        Programa programa, String asignatura, String grado, String nivel
    ) {
        String texto = asignatura == null
            ? ""
            : asignatura.trim().toLowerCase(Locale.ROOT);

        return (texto.isEmpty()
                || programa.asignatura().toLowerCase(Locale.ROOT).contains(texto))
            && ("Todos".equals(grado) || programa.grado().equals(grado))
            && ("Todos".equals(nivel) || programa.nivel().equals(nivel));
    }

    private void validarDatos(String asignatura, String grado, String nivel) {
        if (!datosValidos(asignatura, grado, nivel)) {
            throw new IllegalArgumentException(
                "Asignatura, grado y nivel son obligatorios."
            );
        }
    }
}
