package com.campusvirtual.controller.professor;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.Submission;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controlador de calificaciones del profesor.
 * Muestra entregas pendientes de evaluar con campos para nota y comentario inline.
 */
public class GradingController implements Initializable {
    @FXML private TableView<Submission> submissionsTable;
    @FXML private TableColumn<Submission, String> colCourse;
    @FXML private TableColumn<Submission, String> colTask;
    @FXML private TableColumn<Submission, String> colStudent;
    @FXML private TableColumn<Submission, String> colDate;
    @FXML private TableColumn<Submission, String> colGradeInput;
    @FXML private TableColumn<Submission, String> colComment;
    @FXML private TableColumn<Submission, String> colAction;

    // Almacena temporalmente los valores de nota y comentario por submission
    private final Map<Integer, TextField> gradeFields = new HashMap<>();
    private final Map<Integer, TextField> commentFields = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int profId = AppState.getInstance().getUserId();
        List<Submission> pending = AppState.getInstance().getSubmissionDao()
                .findPendingByProfessor(profId);

        colCourse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseName()));
        colTask.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTaskTitle()));
        colStudent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubmittedAtText()));

        // Columna de nota editable
        colGradeInput.setCellFactory(col -> new TableCell<>() {
            private final TextField field = new TextField();
            { field.setPrefWidth(60); field.setPromptText("0-10"); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Submission sub = getTableView().getItems().get(getIndex());
                gradeFields.put(sub.getId(), field);
                setGraphic(field);
            }
        });

        // Columna de comentario editable
        colComment.setCellFactory(col -> new TableCell<>() {
            private final TextField field = new TextField();
            { field.setPromptText("Comentario..."); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Submission sub = getTableView().getItems().get(getIndex());
                commentFields.put(sub.getId(), field);
                setGraphic(field);
            }
        });

        // Columna de acción
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Evaluar");
            {
                btn.setStyle("-fx-background-color: #059669; -fx-text-fill: white; " +
                           "-fx-background-radius: 6; -fx-padding: 3 10; -fx-font-size: 12px; -fx-cursor: hand;");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Submission sub = getTableView().getItems().get(getIndex());
                btn.setOnAction(e -> gradeSubmission(sub));
                setGraphic(btn);
            }
        });

        submissionsTable.setItems(FXCollections.observableArrayList(pending));

        if (pending.isEmpty()) {
            submissionsTable.setPlaceholder(
                new Label("No hay entregas pendientes de evaluar 🎉"));
        }
    }

    private void gradeSubmission(Submission sub) {
        TextField gradeField = gradeFields.get(sub.getId());
        TextField commentField = commentFields.get(sub.getId());

        if (gradeField == null || gradeField.getText().trim().isEmpty()) {
            showAlert("Introduce una nota.");
            return;
        }

        try {
            double grade = Double.parseDouble(gradeField.getText().trim());
            if (grade < 0 || grade > 10) {
                showAlert("La nota debe estar entre 0 y 10.");
                return;
            }

            String comment = commentField != null ? commentField.getText().trim() : "";
            boolean ok = AppState.getInstance().getSubmissionDao()
                    .grade(sub.getId(), grade, comment);

            if (ok) {
                // Refrescar tabla
                initialize(null, null);
            }
        } catch (NumberFormatException e) {
            showAlert("La nota debe ser un número válido.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
