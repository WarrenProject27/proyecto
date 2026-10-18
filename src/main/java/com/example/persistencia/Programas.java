package com.example.persistencia;

import com.example.modelo.Programa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class Programas {

    public List<Programa> listar() throws SQLException {
        String sql = """
            SELECT id, asignatura, grado, nivel, docente, pdf_nombre, pdf_datos
            FROM programas
            ORDER BY id
            """;
        List<Programa> programas = new ArrayList<>();

        try (Connection conexion = Conexion.abrir();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultados = consulta.executeQuery()) {
            while (resultados.next()) {
                programas.add(mapearPrograma(resultados));
            }
        }
        return programas;
    }

    public Programa insertar(Programa programa) throws SQLException {
        String sql = """
            INSERT INTO programas
                (asignatura, grado, nivel, docente, pdf_nombre, pdf_datos)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conexion = Conexion.abrir();
             PreparedStatement insercion = conexion.prepareStatement(
                 sql, Statement.RETURN_GENERATED_KEYS
             )) {
            asignarCampos(insercion, programa);
            insercion.executeUpdate();

            try (ResultSet claves = insercion.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException(
                        "MySQL no devolvió el identificador del programa creado."
                    );
                }
                return new Programa(
                    claves.getInt(1),
                    programa.asignatura(),
                    programa.grado(),
                    programa.nivel(),
                    programa.docente(),
                    programa.archivoPdf()
                );
            }
        }
    }

    public boolean actualizar(Programa programa) throws SQLException {
        String sql = """
            UPDATE programas
            SET asignatura = ?, grado = ?, nivel = ?, docente = ?,
                pdf_nombre = ?, pdf_datos = ?
            WHERE id = ?
            """;
        try (Connection conexion = Conexion.abrir();
             PreparedStatement actualizacion = conexion.prepareStatement(sql)) {
            asignarCampos(actualizacion, programa);
            actualizacion.setInt(7, programa.id());
            return actualizacion.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM programas WHERE id = ?";
        try (Connection conexion = Conexion.abrir();
             PreparedStatement eliminacion = conexion.prepareStatement(sql)) {
            eliminacion.setInt(1, id);
            return eliminacion.executeUpdate() == 1;
        }
    }

    private void asignarCampos(PreparedStatement sentencia, Programa programa)
        throws SQLException {
        sentencia.setString(1, programa.asignatura());
        sentencia.setString(2, programa.grado());
        sentencia.setString(3, programa.nivel());
        sentencia.setString(4, programa.docente());

        File archivoPdf = programa.archivoPdf();
        if (archivoPdf == null) {
            sentencia.setNull(5, Types.VARCHAR);
            sentencia.setNull(6, Types.BLOB);
            return;
        }

        try {
            sentencia.setString(5, archivoPdf.getName());
            sentencia.setBytes(6, Files.readAllBytes(archivoPdf.toPath()));
        } catch (IOException error) {
            throw new SQLException("No se pudo leer el PDF adjunto.", error);
        }
    }

    private Programa mapearPrograma(ResultSet resultado) throws SQLException {
        int id = resultado.getInt("id");
        byte[] contenidoPdf = resultado.getBytes("pdf_datos");
        File archivoPdf = contenidoPdf == null
            ? null
            : crearArchivoTemporal(id, resultado.getString("pdf_nombre"), contenidoPdf);

        return new Programa(
            id,
            resultado.getString("asignatura"),
            resultado.getString("grado"),
            resultado.getString("nivel"),
            resultado.getString("docente"),
            archivoPdf
        );
    }

    private File crearArchivoTemporal(int id, String nombre, byte[] contenido)
        throws SQLException {
        String nombreArchivo = nombre == null || nombre.isBlank()
            ? "programa.pdf"
            : Path.of(nombre).getFileName().toString();
        try {
            Path carpetaTemporal = Files.createTempDirectory("programa-" + id + "-");
            Path archivoTemporal = carpetaTemporal.resolve(nombreArchivo);
            Files.write(archivoTemporal, contenido);
            carpetaTemporal.toFile().deleteOnExit();
            archivoTemporal.toFile().deleteOnExit();
            return archivoTemporal.toFile();
        } catch (IOException error) {
            throw new SQLException("No se pudo recuperar el PDF del programa.", error);
        }
    }
}
