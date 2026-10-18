package com.example.persistencia;

import com.example.logica.PasswordHasher;
import com.example.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Usuarios {

    public Usuario autenticar(String nombreUsuario, String contrasena)
        throws SQLException {
        String sql = """
            SELECT id, nombre_usuario, nombre, email, rol, contrasena_hash
            FROM usuarios
            WHERE nombre_usuario = ?
            """;
        try (Connection conexion = Conexion.abrir();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nombreUsuario.trim());
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                if (!PasswordHasher.verificar(
                        contrasena, resultado.getString("contrasena_hash"))) {
                    return null;
                }
                return mapearUsuario(resultado);
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        String sql = """
            SELECT id, nombre_usuario, nombre, email, rol
            FROM usuarios
            ORDER BY nombre
            """;
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexion = Conexion.abrir();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultados = consulta.executeQuery()) {
            while (resultados.next()) {
                usuarios.add(mapearUsuario(resultados));
            }
        }
        return usuarios;
    }

    public Usuario insertar(
        String nombreUsuario, String nombre, String email, String rol,
        String contrasena
    ) throws SQLException {
        String sql = """
            INSERT INTO usuarios
                (nombre_usuario, nombre, email, rol, contrasena_hash)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conexion = Conexion.abrir();
             PreparedStatement insercion = conexion.prepareStatement(
                 sql, PreparedStatement.RETURN_GENERATED_KEYS
             )) {
            insercion.setString(1, nombreUsuario.trim());
            insercion.setString(2, nombre.trim());
            insercion.setString(3, email.trim());
            insercion.setString(4, rol);
            insercion.setString(5, PasswordHasher.hash(contrasena));
            insercion.executeUpdate();
            try (ResultSet claves = insercion.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException(
                        "MySQL no devolvió el identificador del usuario creado."
                    );
                }
                return new Usuario(
                    claves.getInt(1), nombreUsuario.trim(), nombre.trim(),
                    email.trim(), rol
                );
            }
        }
    }

    public boolean actualizar(
        Usuario usuario, String nuevaContrasena
    ) throws SQLException {
        String sql = nuevaContrasena == null || nuevaContrasena.isBlank()
            ? """
                UPDATE usuarios
                SET nombre_usuario = ?, nombre = ?, email = ?, rol = ?
                WHERE id = ?
                """
            : """
                UPDATE usuarios
                SET nombre_usuario = ?, nombre = ?, email = ?, rol = ?,
                    contrasena_hash = ?
                WHERE id = ?
                """;
        try (Connection conexion = Conexion.abrir()) {
            conexion.setAutoCommit(false);
            try {
                String rolActual = buscarRol(conexion, usuario.getId());
                if (rolActual == null) {
                    conexion.rollback();
                    return false;
                }
                if ("Administrador".equals(rolActual)
                        && !"Administrador".equals(usuario.getRol())
                        && contarAdministradores(conexion) <= 1) {
                    throw new SQLException(
                        "No se puede quitar el rol al último administrador."
                    );
                }

                try (PreparedStatement actualizacion =
                         conexion.prepareStatement(sql)) {
                    actualizacion.setString(1, usuario.getNombreUsuario().trim());
                    actualizacion.setString(2, usuario.getNombre().trim());
                    actualizacion.setString(3, usuario.getEmail().trim());
                    actualizacion.setString(4, usuario.getRol());
                    if (nuevaContrasena == null || nuevaContrasena.isBlank()) {
                        actualizacion.setInt(5, usuario.getId());
                    } else {
                        actualizacion.setString(
                            5, PasswordHasher.hash(nuevaContrasena)
                        );
                        actualizacion.setInt(6, usuario.getId());
                    }
                    boolean actualizado = actualizacion.executeUpdate() == 1;
                    conexion.commit();
                    return actualizado;
                }
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection conexion = Conexion.abrir()) {
            conexion.setAutoCommit(false);
            try {
                String rol = buscarRol(conexion, id);
                if (rol == null) {
                    conexion.rollback();
                    return false;
                }
                if ("Administrador".equals(rol)
                        && contarAdministradores(conexion) <= 1) {
                    conexion.rollback();
                    return false;
                }
                try (PreparedStatement eliminacion =
                         conexion.prepareStatement(
                             "DELETE FROM usuarios WHERE id = ?"
                         )) {
                    eliminacion.setInt(1, id);
                    boolean eliminado = eliminacion.executeUpdate() == 1;
                    conexion.commit();
                    return eliminado;
                }
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    private String buscarRol(Connection conexion, int id) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(
                 "SELECT rol FROM usuarios WHERE id = ? FOR UPDATE"
             )) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next() ? resultado.getString("rol") : null;
            }
        }
    }

    private int contarAdministradores(Connection conexion) throws SQLException {
        int cantidad = 0;
        try (PreparedStatement consulta = conexion.prepareStatement(
                 "SELECT id FROM usuarios WHERE rol = 'Administrador' FOR UPDATE"
             );
             ResultSet resultados = consulta.executeQuery()) {
            while (resultados.next()) {
                cantidad++;
            }
        }
        return cantidad;
    }

    private Usuario mapearUsuario(ResultSet resultado) throws SQLException {
        return new Usuario(
            resultado.getInt("id"),
            resultado.getString("nombre_usuario"),
            resultado.getString("nombre"),
            resultado.getString("email"),
            resultado.getString("rol")
        );
    }
}
