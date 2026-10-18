package com.example.vista;

import com.example.modelo.Usuario;
import com.example.persistencia.Usuarios;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuAdministrador {

    private final Usuarios usuariosDAO = new Usuarios();

    public Scene crearEscena(Stage stage, Usuario usuarioSesion) {
        stage.setTitle("Gestión de usuarios");

        Label marca = new Label("PRISMA");
        marca.setId("marca");
        ImageView logo = new VentanaLogin().crearLogo();

        Button btnUsuarios = crearBotonNavegacion("Usuarios y permisos");
        Button btnProgramas = crearBotonNavegacion("Programas");
        Button btnSesion = new Button("Cerrar sesión");
        btnSesion.setId("btnSesion");
        btnUsuarios.getStyleClass().add("selected");

        VBox menu = new VBox(15, marca, logo, btnUsuarios, btnProgramas, btnSesion);
        menu.setAlignment(Pos.TOP_CENTER);
        menu.setId("panelGestion");
        menu.setMaxWidth(200);
        VBox.setVgrow(menu, Priority.ALWAYS);

        Label etiquetaSesion = new Label("Sesión:");
        Label nombreSesion = new Label(usuarioSesion.getNombre());
        HBox barraSesion = new HBox(20, etiquetaSesion, nombreSesion);
        barraSesion.setAlignment(Pos.CENTER_LEFT);
        barraSesion.setMinHeight(40);
        barraSesion.setPrefHeight(40);
        barraSesion.setMaxHeight(40);
        barraSesion.setId("panelSesion");

        ObservableList<Usuario> usuarios = FXCollections.observableArrayList();
        TableView<Usuario> tabla = crearTablaUsuarios(usuarios);
        cargarUsuarios(usuarios);

        Label encabezadoUsuarios = new Label("Gestión de Usuarios");
        encabezadoUsuarios.setId("tituloGestion");
        Button nuevoUsuario = new Button("Nuevo usuario");
        nuevoUsuario.setOnAction(evento -> mostrarFormularioUsuario(null)
            .ifPresent(datos -> guardarUsuarioNuevo(datos, usuarios)));
        HBox tituloUsuarios = new HBox(12, encabezadoUsuarios, nuevoUsuario);
        tituloUsuarios.setAlignment(Pos.CENTER_LEFT);

        VBox panelUsuarios = new VBox(20, tituloUsuarios, tabla);
        panelUsuarios.setId("segundoPanel");
        VBox.setVgrow(tabla, Priority.ALWAYS);
        VBox.setVgrow(panelUsuarios, Priority.ALWAYS);

        VBox panelProgramas = new VistaProgramas().crearPanel(false, "");
        panelProgramas.setId("segundoPanel");
        VBox.setVgrow(panelProgramas, Priority.ALWAYS);

        VBox contenido = new VBox(15, barraSesion, panelUsuarios);
        HBox.setHgrow(contenido, Priority.ALWAYS);

        btnUsuarios.setOnAction(evento -> mostrarPanel(
            contenido, panelUsuarios, btnUsuarios, btnProgramas
        ));
        btnProgramas.setOnAction(evento -> mostrarPanel(
            contenido, panelProgramas, btnProgramas, btnUsuarios
        ));
        btnSesion.setOnAction(evento -> new VentanaLogin().start(stage));

        HBox raiz = new HBox(15, menu, contenido);
        raiz.setPadding(new Insets(15));

        Scene escena = new Scene(raiz, 800, 600);
        escena.getStylesheets().add(
            getClass().getResource("/com/example/estilosGestion.css").toExternalForm()
        );
        return escena;
    }

    private TableView<Usuario> crearTablaUsuarios(
        ObservableList<Usuario> usuarios
    ) {
        TableView<Usuario> tabla = new TableView<>(usuarios);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.getColumns().addAll(
            columna("Usuario", Usuario::getNombreUsuario),
            columna("Nombre", Usuario::getNombre),
            columna("Email", Usuario::getEmail),
            columna("Rol", Usuario::getRol)
        );

        TableColumn<Usuario, Void> acciones = new TableColumn<>("Acciones");
        acciones.setCellFactory(columna -> new TableCell<>() {
            private final Button editar = new Button("Editar");
            private final Button eliminar = new Button("Eliminar");
            private final HBox botones = new HBox(8, editar, eliminar);

            {
                editar.getStyleClass().add("action-button");
                eliminar.getStyleClass().add("delete-button");
                botones.setAlignment(Pos.CENTER);
                editar.setOnAction(evento -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    mostrarFormularioUsuario(usuario)
                        .ifPresent(datos -> actualizarUsuario(usuario, datos, usuarios));
                });
                eliminar.setOnAction(evento -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    confirmarEliminacion(usuario, usuarios);
                });
            }

            @Override
            protected void updateItem(Void item, boolean vacia) {
                super.updateItem(item, vacia);
                setGraphic(vacia ? null : botones);
            }
        });
        acciones.setId("center");
        tabla.getColumns().add(acciones);
        return tabla;
    }

    private TableColumn<Usuario, String> columna(
        String titulo, Function<Usuario, String> valor
    ) {
        TableColumn<Usuario, String> columna = new TableColumn<>(titulo);
        columna.setCellValueFactory(celda ->
            new javafx.beans.property.ReadOnlyStringWrapper(
                valor.apply(celda.getValue())
            )
        );
        columna.setId("center");
        return columna;
    }

    private java.util.Optional<DatosUsuario> mostrarFormularioUsuario(
        Usuario usuario
    ) {
        boolean nuevo = usuario == null;
        Dialog<DatosUsuario> dialogo = new Dialog<>();
        dialogo.setTitle(nuevo ? "Crear usuario" : "Editar usuario");
        dialogo.setHeaderText(nuevo
            ? "Completa los datos de la cuenta"
            : "Actualiza los datos de la cuenta");

        ButtonType guardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(guardar, ButtonType.CANCEL);

        TextField nombreUsuario = new TextField(
            nuevo ? "" : usuario.getNombreUsuario()
        );
        TextField nombre = new TextField(nuevo ? "" : usuario.getNombre());
        TextField email = new TextField(nuevo ? "" : usuario.getEmail());
        PasswordField contrasena = new PasswordField();
        contrasena.setPromptText(nuevo
            ? "Mínimo 8 caracteres"
            : "Dejar vacía para conservarla");
        ComboBox<String> rol = new ComboBox<>();
        rol.getItems().addAll("Administrador", "Docente", "Estudiante");
        if (nuevo) {
            rol.setPromptText("Selecciona un rol");
        } else {
            rol.setValue(usuario.getRol());
        }

        VBox formulario = new VBox(
            12,
            campoFormulario("Nombre de usuario", nombreUsuario),
            campoFormulario("Nombre completo", nombre),
            campoFormulario("Correo electrónico", email),
            campoFormulario("Contraseña", contrasena),
            campoFormulario("Rol", rol)
        );
        dialogo.getDialogPane().setContent(formulario);
        dialogo.getDialogPane().setPrefWidth(500);

        Button botonGuardar = (Button) dialogo.getDialogPane().lookupButton(guardar);
        Runnable validar = () -> botonGuardar.setDisable(
            nombreUsuario.getText().isBlank()
                || nombre.getText().isBlank()
                || !email.getText().contains("@")
                || rol.getValue() == null
                || (nuevo && contrasena.getText().length() < 8)
                || (!contrasena.getText().isBlank()
                    && contrasena.getText().length() < 8)
        );
        nombreUsuario.textProperty().addListener(
            (observable, anterior, actual) -> validar.run()
        );
        nombre.textProperty().addListener(
            (observable, anterior, actual) -> validar.run()
        );
        email.textProperty().addListener(
            (observable, anterior, actual) -> validar.run()
        );
        contrasena.textProperty().addListener(
            (observable, anterior, actual) -> validar.run()
        );
        rol.valueProperty().addListener(
            (observable, anterior, actual) -> validar.run()
        );
        validar.run();

        dialogo.setResultConverter(boton -> boton == guardar
            ? new DatosUsuario(
                nombreUsuario.getText().trim(),
                nombre.getText().trim(),
                email.getText().trim(),
                rol.getValue(),
                contrasena.getText()
            )
            : null
        );
        return dialogo.showAndWait();
    }

    private void guardarUsuarioNuevo(
        DatosUsuario datos, ObservableList<Usuario> usuarios
    ) {
        try {
            Usuario usuario = usuariosDAO.insertar(
                datos.nombreUsuario(), datos.nombre(), datos.email(),
                datos.rol(), datos.contrasena()
            );
            usuarios.add(usuario);
        } catch (SQLException error) {
            mostrarError("crear el usuario", error);
        }
    }

    private void actualizarUsuario(
        Usuario original, DatosUsuario datos, ObservableList<Usuario> usuarios
    ) {
        Usuario actualizado = new Usuario(
            original.getId(), datos.nombreUsuario(), datos.nombre(),
            datos.email(), datos.rol()
        );
        try {
            if (!usuariosDAO.actualizar(actualizado, datos.contrasena())) {
                mostrarError(
                    "actualizar el usuario",
                    new SQLException("No se encontró el usuario en la base de datos.")
                );
                return;
            }
            usuarios.set(usuarios.indexOf(original), actualizado);
        } catch (SQLException error) {
            mostrarError("actualizar el usuario", error);
        }
    }

    private void confirmarEliminacion(
        Usuario usuario, ObservableList<Usuario> usuarios
    ) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar usuario");
        confirmacion.setHeaderText(
            "¿Eliminar la cuenta de " + usuario.getNombreUsuario() + "?"
        );
        confirmacion.setContentText("Esta acción no se puede deshacer.");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            if (usuariosDAO.eliminar(usuario.getId())) {
                usuarios.remove(usuario);
            } else {
                mostrarError(
                    "eliminar el usuario",
                    new SQLException(
                        "No se puede eliminar el último administrador o el usuario "
                            + "ya no existe."
                    )
                );
            }
        } catch (SQLException error) {
            mostrarError("eliminar el usuario", error);
        }
    }

    private void cargarUsuarios(ObservableList<Usuario> usuarios) {
        try {
            List<Usuario> encontrados = usuariosDAO.listar();
            usuarios.setAll(encontrados);
        } catch (SQLException error) {
            mostrarError("cargar los usuarios", error);
        }
    }

    private void mostrarPanel(
        VBox contenido, VBox panel, Button botonActivo, Button botonInactivo
    ) {
        contenido.getChildren().set(1, panel);
        botonInactivo.getStyleClass().remove("selected");
        botonActivo.getStyleClass().add("selected");
    }

    private Button crearBotonNavegacion(String texto) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("nav-button");
        return boton;
    }

    private VBox campoFormulario(String texto, javafx.scene.control.Control control) {
        Label etiqueta = new Label(texto);
        control.setMaxWidth(Double.MAX_VALUE);
        VBox campo = new VBox(5, etiqueta, control);
        return campo;
    }

    private void mostrarError(String operacion, Exception error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de base de datos");
        alerta.setHeaderText("No se pudo " + operacion + ".");
        alerta.setContentText(error.getMessage());
        alerta.showAndWait();
    }

    private record DatosUsuario(
        String nombreUsuario,
        String nombre,
        String email,
        String rol,
        String contrasena
    ) {
    }
}
