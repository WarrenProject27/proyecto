package com.example.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexion {

    private static final String URL_PREDETERMINADA =
        "jdbc:mysql://localhost:3306/programas_db?useSSL=false&serverTimezone=UTC";

    private Conexion() {
    }

    public static Connection abrir() throws SQLException {
        String url = variableDeEntorno("DB_URL", URL_PREDETERMINADA);
        String usuario = variableDeEntorno("DB_USER", "root");
        String contrasena = System.getenv().getOrDefault("DB_PASSWORD", "");

        try {
            return DriverManager.getConnection(url, usuario, contrasena);
        } catch (SQLException error) {
            throw new SQLException(
                "No se pudo conectar con MySQL. Comprueba que XAMPP esté iniciado, "
                    + "que exista la base programas_db y que DB_URL, DB_USER y "
                    + "DB_PASSWORD sean correctos.",
                error
            );
        }
    }

    private static String variableDeEntorno(String nombre, String valorPredeterminado) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? valorPredeterminado : valor;
    }
}
