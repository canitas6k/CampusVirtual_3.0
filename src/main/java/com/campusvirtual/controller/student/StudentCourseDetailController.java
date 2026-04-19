package com.campusvirtual.controller.student;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador del detalle de un curso para el estudiante.
 * Muestra unidades con ficheros (descargables) y tareas (entregables).
 */
public class StudentCourseDetailController implements Initializable {
    @FXML private Button backBtn;
    @FXML private Label courseTitleLabel;
    @FXML private Label courseDescLabel;
    @FXML private Accordion unitAccordion;

    private Course course;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        backBtn.setOnAction(e -> {
            // Volver a la lista de cursos
            AppState.getInstance().getViewFactory().navigateToCenter(
                AppState.getInstance().getViewFactory().loadView("/fxml/student/courses.fxml"));
        });
    }

    /**
     * Recibe los datos del curso y construye el contenido dinámicamente.
     */
    public void setCourse(Course course) {
        this.course = course;
        courseTitleLabel.setText(course.getName());
        courseDescLabel.setText(course.getDescription() != null ? course.getDescription() : "");
        loadUnits();
    }

    private void loadUnits() {
        unitAccordion.getPanes().clear();
        List<Unit> units = AppState.getInstance().getUnitDao().findByCourse(course.getId());

        for (Unit unit : units) {
            VBox content = new VBox(8);
            content.setPadding(new Insets(10));
            content.setStyle("-fx-background-color: white;");

            // Ficheros de la unidad
            List<FileResource> files = AppState.getInstance().getFileDao().findByUnit(unit.getId());
            if (!files.isEmpty()) {
                content.getChildren().add(createSectionLabel("📁 Ficheros"));
                for (FileResource file : files) {
                    content.getChildren().add(createFileRow(file));
                }
            }

            // Tareas de la unidad
            List<Task> tasks = AppState.getInstance().getTaskDao().findByUnit(unit.getId());
            if (!tasks.isEmpty()) {
                Label taskHeader = createSectionLabel("📝 Tareas");
                VBox.setMargin(taskHeader, new Insets(10, 0, 0, 0));
                content.getChildren().add(taskHeader);
                for (Task task : tasks) {
                    content.getChildren().add(createTaskRow(task));
                }
            }

            if (content.getChildren().isEmpty()) {
                content.getChildren().add(new Label("Esta unidad no tiene contenido todavía."));
            }

            TitledPane pane = new TitledPane(unit.getName(), content);
            unitAccordion.getPanes().add(pane);
        }

        if (units.isEmpty()) {
            Label emptyLabel = new Label("Esta asignatura no tiene unidades todavía.");
            emptyLabel.setStyle("-fx-padding: 20;");
            unitAccordion.getPanes().add(new TitledPane("Sin contenido", emptyLabel));
        }
    }

    private HBox createFileRow(FileResource file) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 6; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

        Label nameLabel = new Label(file.getFileName() + " (" + file.getFileSizeText() + ")");
        nameLabel.setStyle("-fx-font-size: 13px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button downloadBtn = new Button("Descargar");
        downloadBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; " +
                           "-fx-background-radius: 6; -fx-padding: 4 12; -fx-font-size: 12px; -fx-cursor: hand;");
        downloadBtn.setOnAction(e -> downloadFile(file));

        row.getChildren().addAll(nameLabel, spacer, downloadBtn);
        return row;
    }

    private HBox createTaskRow(Task task) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 6; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label deadlineLabel = new Label("Fecha: " + task.getDeadlineText());
        deadlineLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Verificar si ya se ha entregado
        Optional<Submission> existing = AppState.getInstance().getSubmissionDao()
                .findByTaskAndStudent(task.getId(), AppState.getInstance().getUserId());

        Button actionBtn;
        if (existing.isPresent()) {
            Submission sub = existing.get();
            String text = sub.isGraded() ? "Nota: " + sub.getGradeText() : "Entregada ✓";
            actionBtn = new Button(text);
            actionBtn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                             "-fx-background-radius: 6; -fx-padding: 4 12; -fx-font-size: 12px;");
            actionBtn.setDisable(true);
        } else {
            actionBtn = new Button("Entregar");
            actionBtn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; " +
                             "-fx-background-radius: 6; -fx-padding: 4 12; -fx-font-size: 12px; -fx-cursor: hand;");
            actionBtn.setOnAction(e -> submitTask(task));
        }

        row.getChildren().addAll(titleLabel, deadlineLabel, spacer, actionBtn);
        return row;
    }

    private void downloadFile(FileResource file) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecciona carpeta de descarga");
        File dir = chooser.showDialog(unitAccordion.getScene().getWindow());
        if (dir != null) {
            try {
                Path source = Path.of(file.getStoragePath());
                Path target = dir.toPath().resolve(file.getFileName());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                showAlert("Descarga completada", "Fichero guardado en: " + target, Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Error", "No se pudo descargar el fichero: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void submitTask(Task task) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecciona tu entrega");
        File file = chooser.showOpenDialog(unitAccordion.getScene().getWindow());
        if (file != null) {
            // Copiar fichero a directorio de entregas
            String destDir = "uploads/submissions/" + task.getId() + "/" + AppState.getInstance().getUserId();
            try {
                Files.createDirectories(Path.of(destDir));
                Path target = Path.of(destDir, file.getName());
                Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

                // Registrar en BD
                boolean ok = AppState.getInstance().getSubmissionDao()
                        .create(task.getId(), AppState.getInstance().getUserId(),
                                target.toString(), "Entrega realizada");
                if (ok) {
                    showAlert("Entrega registrada", "Tu tarea ha sido entregada correctamente.",
                              Alert.AlertType.INFORMATION);
                    loadUnits(); // Refrescar vista
                }
            } catch (IOException e) {
                showAlert("Error", "No se pudo subir el fichero: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        return label;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
