package com.campusvirtual.controller.professor;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.Course;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de la lista de asignaturas del profesor.
 * Muestra las asignaturas asignadas con opción de gestionar cada una.
 */
public class ProfessorCoursesController implements Initializable {
    @FXML private VBox courseCardsBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int profId = AppState.getInstance().getUserId();
        List<Course> courses = AppState.getInstance().getCourseDao().findByProfessor(profId);

        courseCardsBox.getChildren().clear();
        for (Course course : courses) {
            courseCardsBox.getChildren().add(createCourseCard(course));
        }

        if (courses.isEmpty()) {
            Label empty = new Label("No tienes asignaturas asignadas.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748B; -fx-padding: 30;");
            courseCardsBox.getChildren().add(empty);
        }
    }

    private HBox createCourseCard(Course course) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                     "-fx-border-color: #E2E8F0; -fx-border-radius: 10; -fx-padding: 18; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1);");
        card.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(course.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        Label desc = new Label(course.getDescription() != null ? course.getDescription() : "");
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        info.getChildren().addAll(name, desc);

        Button manageBtn = new Button("Gestionar");
        manageBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; " +
                         "-fx-background-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
        manageBtn.setOnAction(e -> openCourseDetail(course));

        card.getChildren().addAll(info, manageBtn);
        return card;
    }

    private void openCourseDetail(Course course) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/professor/course_detail.fxml"));
            Node view = loader.load();
            ProfessorCourseDetailController controller = loader.getController();
            controller.setCourse(course);
            AppState.getInstance().getViewFactory().navigateToCenter(view);
        } catch (IOException e) {
            System.err.println("Error cargando detalle del curso: " + e.getMessage());
        }
    }
}
