package com.campusvirtual.controller.student;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.Course;
import com.campusvirtual.model.Task;
import com.campusvirtual.model.Submission;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador del tablero del estudiante.
 * Muestra resumen de asignaturas, tareas pendientes y próximas entregas.
 */
public class DashboardController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private Label courseCountLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label submissionCountLabel;
    @FXML private VBox upcomingTasksBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AppState state = AppState.getInstance();
        int userId = state.getUserId();

        welcomeLabel.setText("Bienvenido, " + state.getUserFirstName());

        // Contar asignaturas
        List<Course> courses = state.getCourseDao().findByStudent(userId);
        courseCountLabel.setText(String.valueOf(courses.size()));

        // Contar tareas y entregas
        List<Task> tasks = state.getTaskDao().findByStudent(userId);
        List<Submission> submissions = state.getSubmissionDao().findByStudent(userId);

        long pendingCount = tasks.stream()
            .filter(t -> submissions.stream().noneMatch(s -> s.getTaskId() == t.getId()))
            .count();
        pendingTasksLabel.setText(String.valueOf(pendingCount));
        submissionCountLabel.setText(String.valueOf(submissions.size()));

        // Próximas tareas (las 5 más cercanas)
        tasks.stream()
            .filter(t -> t.getDeadline() != null && !t.getDeadline().isBefore(LocalDate.now()))
            .filter(t -> submissions.stream().noneMatch(s -> s.getTaskId() == t.getId()))
            .limit(5)
            .forEach(task -> {
                HBox row = new HBox(10);
                row.setStyle("-fx-padding: 8; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");
                Label title = new Label(task.getCourseName() + " — " + task.getTitle());
                title.setStyle("-fx-font-size: 13px;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Label deadline = new Label(task.getDeadlineText());
                deadline.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 13px;");
                row.getChildren().addAll(title, spacer, deadline);
                upcomingTasksBox.getChildren().add(row);
            });

        if (upcomingTasksBox.getChildren().isEmpty()) {
            upcomingTasksBox.getChildren().add(
                new Label("No tienes tareas pendientes 🎉"));
        }
    }
}
