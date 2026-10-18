package com.example.vista;

import com.example.modelo.Usuario;
import com.example.persistencia.Usuarios;

import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaLogin extends Application {

    @Override
    public void start(Stage primaryStage) {
        ImageView logo = crearLogo();
        Label titulo = new Label("PRISMA");
        titulo.setId("titulo");
        Label subtitulo = new Label("Software Educativo");
        subtitulo.setId("subtitulo");

        TextField nombreUsuario = new TextField();
        nombreUsuario.setPromptText("Usuario");
        nombreUsuario.setPrefSize(250, 40);
        nombreUsuario.setMaxWidth(250);

        PasswordField contrasena = new PasswordField();
        contrasena.setPromptText("Contraseña");
        contrasena.setPrefSize(250, 40);
        contrasena.setMaxWidth(250);

        Button iniciarSesion = new Button("Iniciar Sesión");
        iniciarSesion.setPrefSize(250, 30);
        iniciarSesion.setMaxWidth(250);
        Label mensaje = new Label();

        iniciarSesion.setOnAction(evento -> {
            iniciarSesion.setDisable(true);
            mensaje.setText("");
            try {
                Usuario usuario = new Usuarios().autenticar(
                    nombreUsuario.getText(), contrasena.getText()
                );
                if (usuario == null) {
                    mensaje.setText("Usuario o contraseña incorrectos");
                } else if ("Administrador".equalsIgnoreCase(usuario.getRol())) {
                    primaryStage.setScene(
                        new MenuAdministrador().crearEscena(primaryStage, usuario)
                    );
                } else if ("Docente".equalsIgnoreCase(usuario.getRol())) {
                    primaryStage.setScene(
                        new MenuDocente().crearEscena(primaryStage, usuario)
                    );
                } else if ("Estudiante".equalsIgnoreCase(usuario.getRol())) {
                    primaryStage.setScene(
                        new MenuEstudiante().crearEscena(primaryStage, usuario)
                    );
                }
            } catch (SQLException | IllegalArgumentException error) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setTitle("Error de acceso");
                alerta.setHeaderText("No se pudo validar el usuario.");
                alerta.setContentText(error.getMessage());
                alerta.showAndWait();
            } finally {
                iniciarSesion.setDisable(false);
            }
        });

        VBox panelLogin = new VBox(
            12, logo, titulo, subtitulo, nombreUsuario, contrasena, iniciarSesion, mensaje
        );
        panelLogin.setAlignment(Pos.CENTER);
        panelLogin.setMaxSize(350, 400);
        panelLogin.setId("panel-login");

        VBox root = new VBox(panelLogin);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(
            getClass().getResource("/com/example/estilos.css").toExternalForm()
        );
        primaryStage.setTitle("Ventana de login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public ImageView crearLogo() {
        Image imagenLogo = new Image(
            getClass().getResourceAsStream("/com/example/logo.png")
        );
        ImageView logo = new ImageView(imagenLogo);
        logo.setFitWidth(100);
        logo.setPreserveRatio(true);
        return logo;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
