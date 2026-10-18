package com.example.vista;

import com.example.logica.Funciones;
import com.example.modelo.Programa;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Function;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

public class VistaProgramas {

    public VBox crearPanel(boolean permitirEdicion, String nombreDocente) {
        Funciones funciones = new Funciones();

        // Controles para buscar programas por asignatura, grado y nivel.
        Label titulo = new Label("Programas curriculares");
        titulo.setId("tituloGestion");

        TextField asignatura = new TextField();
        asignatura.setPromptText("Ej: Matemática");

        ComboBox<String> grado = new ComboBox<>();
        grado.getItems().addAll("Todos", "1°", "2°", "3°");
        grado.setValue("Todos");

        ComboBox<String> nivel = new ComboBox<>();
        nivel.getItems().addAll("Todos", "EBI (Ciclo Básico)", "Bachillerato");
        nivel.setValue("Todos");

        HBox filtros = new HBox(10,
            campoFormulario("Asignatura", asignatura),
            campoFormulario("Grado", grado),
            campoFormulario("Nivel", nivel)
        );
        HBox.setHgrow(filtros.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(filtros.getChildren().get(1), Priority.ALWAYS);
        HBox.setHgrow(filtros.getChildren().get(2), Priority.ALWAYS);

        Button buscar = new Button("Buscar");
        buscar.setId("buscarPrograma");
        Button limpiar = new Button("Limpiar");
        limpiar.setId("limpiarPrograma");

        Button nuevo = new Button("Nuevo Programa");
        nuevo.setId("nuevoPrograma");

        HBox barraBusqueda = new HBox(10, filtros, buscar, limpiar);
        if (permitirEdicion) {
            barraBusqueda.getChildren().add(nuevo);
        }
        barraBusqueda.setAlignment(Pos.BOTTOM_LEFT);
        HBox.setHgrow(filtros, Priority.ALWAYS);

        ObservableList<Programa> programas = FXCollections.observableArrayList();
        try {
            programas.addAll(funciones.obtenerProgramas());
        } catch (SQLException error) {
            mostrarError("cargar los programas", error);
        }

        nuevo.setOnAction(e -> {
            Dialog<Programa> dialogo = new Dialog<>();
            dialogo.setTitle("Nuevo Programa Curricular");
            dialogo.setHeaderText("Completa los datos del programa");

            ButtonType guardar = new ButtonType(
                "Guardar", ButtonBar.ButtonData.OK_DONE
            );
            dialogo.getDialogPane().getButtonTypes().addAll(
                guardar, ButtonType.CANCEL
            );

            TextField asignaturaNueva = new TextField();
            asignaturaNueva.setPromptText("Nombre de asignatura");

            ComboBox<String> gradoNuevo = new ComboBox<>();
            gradoNuevo.getItems().addAll("1°", "2°", "3°");
            gradoNuevo.setPromptText("Selecciona el grado");

            ComboBox<String> nivelNuevo = new ComboBox<>();
            nivelNuevo.getItems().addAll("EBI (Ciclo Básico)", "Bachillerato");
            nivelNuevo.setPromptText("Selecciona el nivel");

            Button botonGuardar = (Button) dialogo.getDialogPane().lookupButton(guardar);

            //bloque de validar que los campos esten completos.----------------------
            Runnable validarFormulario = () -> botonGuardar.setDisable(
                !funciones.datosValidos(
                    asignaturaNueva.getText(),
                    gradoNuevo.getValue(),
                    nivelNuevo.getValue()
                )
            );

            asignaturaNueva.textProperty().addListener(
                (observable, anterior, actual) -> validarFormulario.run()
                );
            gradoNuevo.valueProperty().addListener(
                (observable, anterior, actual) -> validarFormulario.run()
                );
            nivelNuevo.valueProperty().addListener(
                (observable, anterior, actual) -> validarFormulario.run()
                );
            validarFormulario.run();
            //guardar va a empezar deshabilitado y se va a activar cuando todos los campos tengan valores
            //pdf es algo opcional asi que no se va a validar.

            ObjectProperty<File> archivoPdfSeleccionado =
                new SimpleObjectProperty<>();
            Button adjuntarPdf = new Button("Adjuntar PDF");
            Label nombrePdf = new Label("Sin archivo adjunto");
            adjuntarPdf.setOnAction(evento -> {
                FileChooser selectorPdf = new FileChooser();
                selectorPdf.setTitle("Seleccionar programa curricular");
                selectorPdf.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
                );

                File archivo = selectorPdf.showOpenDialog(
                    dialogo.getDialogPane().getScene().getWindow()
                );
                if (archivo != null) {
                    archivoPdfSeleccionado.set(archivo);
                    nombrePdf.setText(archivo.getName());
                }
            });

            GridPane formulario = new GridPane();
            formulario.setHgap(10);
            formulario.setVgap(10);
            formulario.addRow(0, new Label("Asignatura:"), asignaturaNueva);
            formulario.addRow(1, new Label("Grado:"), gradoNuevo);
            formulario.addRow(2, new Label("Nivel:"), nivelNuevo);
            formulario.addRow(3, new Label("Docente:"), new Label(nombreDocente));
            formulario.addRow(
                4, new Label("PDF (opcional):"), new HBox(8, adjuntarPdf, nombrePdf)
            );
            dialogo.getDialogPane().setContent(formulario);

            dialogo.setResultConverter(boton -> {
                if (boton == guardar) {
                    return funciones.crearPrograma(
                        asignaturaNueva.getText().trim(),
                        gradoNuevo.getValue(),
                        nivelNuevo.getValue(),
                        nombreDocente,
                        archivoPdfSeleccionado.get()
                    );
                }
                return null;
            });

            dialogo.showAndWait().ifPresent(programa -> {
                try {
                    programas.add(funciones.agregarPrograma(programa));
                } catch (SQLException error) {
                    mostrarError("guardar el programa", error);
                }
            });
        });

        FilteredList<Programa> programasFiltrados = new FilteredList<>(programas, programa -> true);

        TableView<Programa> tablaProgramas = new TableView<>(programasFiltrados);
        tablaProgramas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Label cantidad = new Label();
        programas.addListener((ListChangeListener<Programa>) cambio ->
            cantidad.setText(
                programasFiltrados.size() + " programa(s) encontrados."
            )
        );
        // Las columnas muestran los datos del programa mediante sus accesores.
        tablaProgramas.getColumns().addAll(
            columnaPrograma("Asignatura", Programa::asignatura),
            columnaPrograma("Grado", Programa::grado),
            columnaPrograma("Nivel", Programa::nivel),
            columnaPrograma("Docente", Programa::docente)
        );

        TableColumn<Programa, Void> columnaAcciones = new TableColumn<>("Acciones");
        columnaAcciones.setCellFactory(columna -> new TableCell<>() {
            private final Button ver = new Button("Ver");
            private final Button editar = new Button("Editar");
            private final Button eliminar = new Button("Eliminar");
            private final HBox acciones = new HBox(5);

            {
                ver.getStyleClass().add("ver");
                editar.getStyleClass().add("editar");
                eliminar.getStyleClass().add("eliminar");

                acciones.getChildren().add(ver);
                if (permitirEdicion) {
                    acciones.getChildren().addAll(editar, eliminar);
                }

                // conservamos el setOnAction del boton ver de administrador
                // Muestra los datos de la fila seleccionada en una ventana informativa.
                ver.setOnAction(e -> {
                    Programa programa = getTableView().getItems().get(getIndex());
                    Alert detalle = new Alert(Alert.AlertType.INFORMATION);
                    detalle.setTitle("Programa curricular");
                    detalle.setHeaderText(programa.asignatura());
                    detalle.setContentText(
                        "Grado: " + programa.grado() + "\n" +
                        "Nivel: " + programa.nivel() + "\n" +
                        "Docente: " + programa.docente() + "\n" +
                        (programa.archivoPdf() == null
                            ? "Sin PDF adjunto"
                            : "PDF adjunto: " + programa.archivoPdf().getName())
                    );
                    if (programa.archivoPdf() == null) {
                        detalle.showAndWait();
                    } else {
                        ButtonType abrirPdf = new ButtonType(
                            "Abrir PDF", ButtonBar.ButtonData.OTHER
                        );
                        detalle.getButtonTypes().setAll(abrirPdf, ButtonType.CLOSE);
                        detalle.showAndWait().ifPresent(respuesta -> {
                            if (respuesta == abrirPdf) {
                                abrirPdf(programa.archivoPdf());
                            }
                        });
                    }
                });

                editar.setOnAction(e -> {
                    Programa original = getTableView().getItems().get(getIndex());

                    Dialog<Programa> dialogo = new Dialog<>();
                    dialogo.setTitle("Editar programa curricular");
                    dialogo.setHeaderText("Modifica los datos del programa");

                    ButtonType guardarEdicion = new ButtonType(
                        "Guardar cambios", ButtonBar.ButtonData.OK_DONE
                    );
                    dialogo.getDialogPane().getButtonTypes().addAll(
                        guardarEdicion, ButtonType.CANCEL
                    );

                    TextField asignaturaEditada =
                        new TextField(original.asignatura());

                    ComboBox<String> gradoEditado = new ComboBox<>();
                    gradoEditado.getItems().addAll("1°", "2°", "3°");
                    gradoEditado.setValue(original.grado());

                    ComboBox<String> nivelEditado = new ComboBox<>();
                    nivelEditado.getItems().addAll(
                        "EBI (Ciclo Básico)", "Bachillerato"
                    );
                    nivelEditado.setValue(original.nivel());

                    ObjectProperty<File> pdfEditado =
                        new SimpleObjectProperty<>(original.archivoPdf());
                    Button cambiarPdf = new Button("Cambiar PDF");
                    Label nombrePdfEditado = new Label(
                        original.archivoPdf() == null
                            ? "Sin archivo adjunto"
                            : original.archivoPdf().getName()
                    );
                    cambiarPdf.setOnAction(evento -> {
                        FileChooser selectorPdf = new FileChooser();
                        selectorPdf.setTitle("Seleccionar programa curricular");
                        selectorPdf.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter(
                                "Archivos PDF", "*.pdf"
                            )
                        );

                        File archivo = selectorPdf.showOpenDialog(
                            dialogo.getDialogPane().getScene().getWindow()
                        );
                        if (archivo != null) {
                            pdfEditado.set(archivo);
                            nombrePdfEditado.setText(archivo.getName());
                        }
                    });

                    GridPane formularioEdicion = new GridPane();
                    formularioEdicion.setHgap(10);
                    formularioEdicion.setVgap(10);
                    formularioEdicion.addRow(
                        0, new Label("Asignatura:"), asignaturaEditada
                    );
                    formularioEdicion.addRow(
                        1, new Label("Grado:"), gradoEditado
                    );
                    formularioEdicion.addRow(
                        2, new Label("Nivel:"), nivelEditado
                    );
                    formularioEdicion.addRow(
                        3, new Label("Docente:"), new Label(original.docente())
                    );
                    formularioEdicion.addRow(
                        4, new Label("PDF (opcional):"),
                        new HBox(8, cambiarPdf, nombrePdfEditado)
                    );
                    dialogo.getDialogPane().setContent(formularioEdicion);

                    Button botonGuardarEdicion = (Button) dialogo.getDialogPane()
                        .lookupButton(guardarEdicion);
                    Runnable validarEdicion = () -> botonGuardarEdicion.setDisable(
                        !funciones.datosValidos(
                            asignaturaEditada.getText(),
                            gradoEditado.getValue(),
                            nivelEditado.getValue()
                        )
                    );
                    asignaturaEditada.textProperty().addListener(
                        (observable, anterior, actual) -> validarEdicion.run()
                    );
                    gradoEditado.valueProperty().addListener(
                        (observable, anterior, actual) -> validarEdicion.run()
                    );
                    nivelEditado.valueProperty().addListener(
                        (observable, anterior, actual) -> validarEdicion.run()
                    );
                    validarEdicion.run();

                    dialogo.setResultConverter(boton -> {
                        if (boton == guardarEdicion) {
                            return funciones.editarPrograma(
                                original,
                                asignaturaEditada.getText().trim(),
                                gradoEditado.getValue(),
                                nivelEditado.getValue(),
                                pdfEditado.get()
                            );
                        }
                        return null;
                    });

                    dialogo.showAndWait().ifPresent(programaEditado -> {
                        try {
                            funciones.actualizarPrograma(programaEditado);
                            int indice = programas.indexOf(original);
                            if (indice < 0) {
                                throw new IllegalStateException(
                                    "El programa actualizado ya no está en la lista."
                                );
                            }
                            programas.set(indice, programaEditado);
                        } catch (SQLException | IllegalStateException error) {
                            mostrarError("actualizar el programa", error);
                        }
                    });
                });

                //funcionamiento del boton eliminar (ventanas de confirmacion y alertas)
                eliminar.setOnAction(e -> {
                    Programa programa = getTableView().getItems().get(getIndex());

                    Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmacion.setTitle("Eliminar programa");
                    confirmacion.setHeaderText(
                        "¿Seguro de eliminar este programa de " + programa.asignatura() + "?"
                    );
                    confirmacion.setContentText("Esta acción no se puede deshacer.");

                    if (confirmacion.showAndWait().orElse(ButtonType.CANCEL)
                            == ButtonType.OK) {
                        try {
                            if (funciones.eliminarPrograma(programa)) {
                                programas.remove(programa);
                            } else {
                                mostrarError(
                                    "eliminar el programa",
                                    new SQLException(
                                        "No se encontró el programa en la base de datos."
                                    )
                                );
                            }
                        } catch (SQLException error) {
                            mostrarError("eliminar el programa", error);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : acciones);
            }
        });
        columnaAcciones.setId("center");
        tablaProgramas.getColumns().add(columnaAcciones);
        VBox.setVgrow(tablaProgramas, Priority.ALWAYS);

        // Aplica conjuntamente los criterios elegidos y actualiza el total de resultados.

        Runnable aplicarFiltros = () -> {
            String gradoSeleccionado = grado.getValue();
            String nivelSeleccionado = nivel.getValue();
            programasFiltrados.setPredicate(programa ->
                funciones.coincideConFiltros(
                    programa,
                    asignatura.getText(),
                    gradoSeleccionado,
                    nivelSeleccionado
                )
            );
            cantidad.setText(programasFiltrados.size() + " programa(s) encontrados.");
        };
        buscar.setOnAction(e -> aplicarFiltros.run());
        // Restablece los campos y vuelve a mostrar todos los programas.
        limpiar.setOnAction(e -> {
            asignatura.clear();
            grado.setValue("Todos");
            nivel.setValue("Todos");
            aplicarFiltros.run();
        });
        cantidad.setText(programas.size() + " programa(s) encontrados.");

        VBox panel = new VBox(16, titulo, barraBusqueda, tablaProgramas, cantidad);
        panel.setPadding(new Insets(25));
        return panel;
    }

    private TableColumn<Programa, String> columnaPrograma(
        String titulo, Function<Programa, String> valor
    ) {
        TableColumn<Programa, String> columna = new TableColumn<>(titulo);
        // Convierte el valor obtenido del programa en el observable que usa la tabla.
        columna.setCellValueFactory(celda ->
            new ReadOnlyStringWrapper(valor.apply(celda.getValue()))
        );
        columna.setId("center");
        return columna;
    }

    private VBox campoFormulario(String texto, Control control) {
        Label etiqueta = new Label(texto);
        control.setMaxWidth(Double.MAX_VALUE);

        VBox campo = new VBox(5);
        campo.getChildren().addAll(etiqueta, control);
        return campo;
    }

    private void mostrarError(String operacion, Exception error) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de base de datos");
        alerta.setHeaderText("No se pudo " + operacion + ".");
        alerta.setContentText(error.getMessage());
        alerta.showAndWait();
    }

    private void abrirPdf(File archivoPdf) {
        if (!Desktop.isDesktopSupported()
                || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            mostrarErrorPdf(
                "Este equipo no permite abrir enlaces en el navegador predeterminado."
            );
            return;
        }
        if (!archivoPdf.isFile()) {
            mostrarErrorPdf("No se encontró el archivo PDF adjunto.");
            return;
        }
        try {
            Desktop.getDesktop().browse(archivoPdf.toURI());
        } catch (IOException | UnsupportedOperationException | SecurityException error) {
            mostrarErrorPdf("No se pudo abrir el PDF: " + error.getMessage());
        }
    }

    private void mostrarErrorPdf(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("No se pudo abrir el PDF");
        alerta.setHeaderText("Error al abrir el archivo adjunto.");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

}
