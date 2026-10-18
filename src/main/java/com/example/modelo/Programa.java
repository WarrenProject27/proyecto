package com.example.modelo;

import java.io.File;

public record Programa(
    int id,
    String asignatura,
    String grado,
    String nivel,
    String docente,
    File archivoPdf
) {
}
