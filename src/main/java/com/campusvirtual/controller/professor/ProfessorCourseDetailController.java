package com.campusvirtual.controller.professor;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del detalle de curso para el profesor.
 * Permite gestionar unidades, subir ficheros y crear tareas.
 */
public class ProfessorCourseDetailController implements Initializable {
    @FXML private Button backBtn;
    @FXML private Label courseTitleLabel;
    @FXML private TextField unitNameField;
    @FXML private Button addUnitBtn;
    @FXML private VBox unitsContainer;

    private Course course;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        backBtn.setOnAction(e ->
            AppState.getInstance().getViewFactory().navigateToCenter(
                AppState.getInstance().getViewFactory().loadView("/fxml/professor/courses.fxml")));
        addUnitBtn.setOnAction(e -> addUnit());
    }

    public void setCourse(Course course) {
        this.course = course;
        courseTitleLabel.setText(course.getName());
        loadUnits();
    }

    private void addUnit() {
        String name = unitNameField.getText().trim();
        if (name.isEmpty()) return;

        List<Unit> existing = AppState.getInstance().getUnitDao().findByCourse(course.getId());
        int order = existing.size() + 1;

        AppState.getInstance().getUnitDao().create(course.getId(), name, order);
        unitNameField.clear();
        loadUnits();
    }

    private void loadUnits() {
        unitsContainer.getChildren().clear();
        List<Unit> units = AppState.getInstance().getUnitDao().findByCourse(course.getId());

        for (Unit unit : units) {
            unitsContainer.getChildren().add(createUnitCard(unit));
        }

        if (units.isEmpty()) {
            Label empty = new Label("No hay unidades. Añade la primera arriba.");
            empty.setStyle("-fx-text-fill: #64748B; -fx-padding: 15;");
            unitsContainer.getChildren().add(empty);
        }
    }

    private VBox createUnitCard(Unit unit) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-padding: 15;");

        // Cabecera de la unidad
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label unitTitle = new Label("📘 " + unit.getName());
        unitTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteUnitBtn = new Button("Eliminar");
        deleteUnitBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; " +
                             "-fx-background-radius: 6; -fx-padding: 4 12; -fx-font-size: 12px; -fx-cursor: hand;");
        deleteUnitBtn.setOnAction(e -> {
            AppState.getInstance().getUnitDao().delete(unit.getId());
            loadUnits();
        });
        header.getChildren().addAll(unitTitle, spacer, deleteUnitBtn);
        card.getChildren().add(header);

        // Ficheros
        Label filesLabel = new Label("📁 Ficheros:");
        filesLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        card.getChildren().add(filesLabel);

        List<FileResource> files = AppState.getInstance().getFileDao().findByUnit(unit.getId());
        for (FileResource file : files) {
            card.getChildren().add(createFileRow(file));
        }

        Button addFileBtn = new Button("+ Subir Fichero");
        addFileBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                          "-fx-background-radius: 6; -fx-padding: 4 14; -fx-font-size: 12px; -fx-cursor: hand;");
        addFileBtn.setOnAction(e -> uploadFile(unit));
        card.getChildren().add(addFileBtn);

        // Tareas
        Label tasksLabel = new Label("📝 Tareas:");
        tasksLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        VBox.setMargin(tasksLabel, new Insets(10, 0, 0, 0));
        card.getChildren().add(tasksLabel);

        List<Task> tasks = AppState.getInstance().getTaskDao().findByUnit(unit.getId());
        for (Task task : tasks) {
            card.getChildren().add(createTaskRow(task));
        }

        // Formulario para nueva tarea
        HBox taskForm = new HBox(8);
        taskForm.setAlignment(Pos.CENTER_LEFT);
        TextField taskTitle = new TextField();
        taskTitle.setPromptText("Título de la tarea");
        taskTitle.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #CBD5E1; " +
                         "-fx-border-radius: 4; -fx-background-radius: 4; -fx-pref-height: 32;");
        HBox.setHgrow(taskTitle, Priority.ALWAYS);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Fecha límite");
        datePicker.setStyle("-fx-pref-height: 32;");

        Button addTaskBtn = new Button("+ Tarea");
        addTaskBtn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; " +
                          "-fx-background-radius: 6; -fx-padding: 4 14; -fx-font-size: 12px; -fx-cursor: hand;");
        addTaskBtn.setOnAction(e -> {
            if (!taskTitle.getText().trim().isEmpty()) {
                LocalDate deadline = datePicker.getValue();
                AppState.getInstance().getTaskDao().create(
                    unit.getId(), taskTitle.getText().trim(), "", deadline, 10.0);
                taskTitle.clear();
                datePicker.setValue(null);
                loadUnits();
            }
        });

        taskForm.getChildren().addAll(taskTitle, datePicker, addTaskBtn);
        card.getChildren().add(taskForm);

        return card;
    }

    private HBox createFileRow(FileResource file) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 4; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

        Label name = new Label("  " + file.getFileName() + " (" + file.getFileSizeText() + ")");
        name.setStyle("-fx-font-size: 13px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("×");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                         "-fx-font-weight: bold; -fx-background-radius: 50; " +
                         "-fx-min-width: 24; -fx-min-height: 24; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            AppState.getInstance().getFileDao().delete(file.getId());
            // Eliminar fichero físico también
            try { Files.deleteIfExists(Path.of(file.getStoragePath())); } catch (IOException ignored) {}
            loadUnits();
        });

        row.getChildren().addAll(name, spacer, deleteBtn);
        return row;
    }

    private HBox createTaskRow(Task task) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 4; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

        Label title = new Label("  " + task.getTitle());
        title.setStyle("-fx-font-size: 13px;");
        Label deadline = new Label(task.getDeadlineText());
        deadline.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("×");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; " +
                         "-fx-font-weight: bold; -fx-background-radius: 50; " +
                         "-fx-min-width: 24; -fx-min-height: 24; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            AppState.getInstance().getTaskDao().delete(task.getId());
            loadUnits();
        });

        row.getChildren().addAll(title, deadline, spacer, deleteBtn);
        return row;
    }

    private void uploadFile(Unit unit) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecciona fichero para subir");
        File file = chooser.showOpenDialog(unitsContainer.getScene().getWindow());
        if (file != null) {
            String destDir = "uploads/courses/" + course.getId() + "/" + unit.getId();
            try {
                Files.createDirectories(Path.of(destDir));
                Path target = Path.of(destDir, file.getName());
                Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

                String mimeType = Files.probeContentType(file.toPath());
                AppState.getInstance().getFileDao().create(
                    unit.getId(), file.getName(), target.toString(),
                    mimeType != null ? mimeType : "application/octet-stream",
                    file.length());
                loadUnits();
            } catch (IOException e) {
                System.err.println("Error subiendo fichero: " + e.getMessage());
            }
        }
    }
}
