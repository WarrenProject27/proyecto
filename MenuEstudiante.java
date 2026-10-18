package com.example.vista;

import com.example.modelo.Usuario;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuEstudiante {

    public Scene crearEscena(Stage stage, Usuario usuario) {
        Label marca = new Label("PRISMA");
        marca.setId("marca");

        ImageView logo = new VentanaLogin().crearLogo();

        Button btnProgramas = new Button("Programas");
        btnProgramas.getStyleClass().addAll("nav-button", "selected");

        Button btnCerrarSesion = new Button("Cerrar sesión");
        btnCerrarSesion.setId("btnSesion");
        btnCerrarSesion.setOnAction(e -> new VentanaLogin().start(stage));

        VBox menuLateral = new VBox(
            15, marca, logo, btnProgramas, btnCerrarSesion
        );
        menuLateral.setAlignment(Pos.TOP_CENTER);
        menuLateral.setId("panelGestion");
        menuLateral.setMaxWidth(200);

        Label sesion = new Label("Sesión: " + usuario.getNombre());
        sesion.setId("panelSesion");

        VBox vistaProgramas =
            new VistaProgramas().crearPanel(false, usuario.getNombre());
        vistaProgramas.setId("segundoPanel");
        VBox.setVgrow(vistaProgramas, Priority.ALWAYS);

        VBox contenido = new VBox(15, sesion, vistaProgramas);
        HBox.setHgrow(contenido, Priority.ALWAYS);

        HBox raiz = new HBox(15, menuLateral, contenido);
        raiz.setPadding(new Insets(15));

        Scene escena = new Scene(raiz, 800, 600);
        escena.getStylesheets().add(
            getClass().getResource("/com/example/estilosGestion.css").toExternalForm()
        );
        return escena;
    }
}