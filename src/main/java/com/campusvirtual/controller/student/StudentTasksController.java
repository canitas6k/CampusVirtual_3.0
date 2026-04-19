package com.campusvirtual.controller.student;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.Submission;
import com.campusvirtual.model.Task;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
 * Controlador de la vista de tareas del estudiante.
 * Muestra todas las tareas de las asignaturas matriculadas con estado de entrega.
 */
public class StudentTasksController implements Initializable {
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> colCourse;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colDeadline;
    @FXML private TableColumn<Task, String> colStatus;
    @FXML private TableColumn<Task, String> colAction;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int userId = AppState.getInstance().getUserId();
        List<Task> tasks = AppState.getInstance().getTaskDao().findByStudent(userId);

        colCourse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseName()));
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colDeadline.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeadlineText()));

        colStatus.setCellValueFactory(c -> {
            Optional<Submission> sub = AppState.getInstance().getSubmissionDao()
                    .findByTaskAndStudent(c.getValue().getId(), userId);
            if (sub.isEmpty()) return new SimpleStringProperty("Pendiente");
            return new SimpleStringProperty(sub.get().isGraded()
                    ? "Nota: " + sub.get().getGradeText() : "Entregada");
        });

        // Columna de acción con botón de entrega
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Entregar");
            {
                btn.setStyle("-fx-background-color: #D97706; -fx-text-fill: white; " +
                           "-fx-background-radius: 6; -fx-padding: 3 10; -fx-font-size: 12px; -fx-cursor: hand;");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Task task = getTableView().getItems().get(getIndex());
                Optional<Submission> sub = AppState.getInstance().getSubmissionDao()
                        .findByTaskAndStudent(task.getId(), userId);
                if (sub.isPresent()) {
                    setGraphic(null);
                } else {
                    btn.setOnAction(e -> submitTask(task));
                    setGraphic(btn);
                }
            }
        });

        tasksTable.setItems(FXCollections.observableArrayList(tasks));
    }

    private void submitTask(Task task) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecciona tu entrega");
        File file = chooser.showOpenDialog(tasksTable.getScene().getWindow());
        if (file != null) {
            int userId = AppState.getInstance().getUserId();
            String destDir = "uploads/submissions/" + task.getId() + "/" + userId;
            try {
                Files.createDirectories(Path.of(destDir));
                Path target = Path.of(destDir, file.getName());
                Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                AppState.getInstance().getSubmissionDao()
                        .create(task.getId(), userId, target.toString(), "Entrega realizada");
                // Refrescar
                initialize(null, null);
            } catch (IOException e) {
                System.err.println("Error al entregar tarea: " + e.getMessage());
            }
        }
    }
}
