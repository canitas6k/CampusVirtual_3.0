package com.campusvirtual.controller.student.cell;

import com.campusvirtual.controller.student.StudentCourseDetailController;
import com.campusvirtual.core.AppState;
import com.campusvirtual.model.Course;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import java.io.IOException;

/**
 * Celda personalizada para la lista de cursos.
 * Usa FXML para el diseño y Java para la lógica.
 * El bug de shadow variable del código antiguo está corregido.
 */
public class CourseCell extends ListCell<Course> {
    @FXML private Label courseNameLabel;
    @FXML private Label courseDescLabel;
    @FXML private Button openBtn;

    private Node root;
    private boolean fxmlLoaded = false;  // Corregido: flag en lugar de shadow variable

    @Override
    protected void updateItem(Course course, boolean empty) {
        super.updateItem(course, empty);

        if (empty || course == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (!fxmlLoaded) {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/student/cell/course_cell.fxml"));
                loader.setController(this);
                try {
                    root = loader.load();
                    openBtn.setOnAction(e -> onOpen());
                    fxmlLoaded = true;
                } catch (IOException e) {
                    throw new RuntimeException("Error cargando CourseCell.fxml", e);
                }
            }

            courseNameLabel.setText(course.getName());
            courseDescLabel.setText(course.getDescription() != null
                    ? course.getDescription() : "");
            setGraphic(root);
        }
    }

    /**
     * Navega al detalle del curso seleccionado.
     * Usa ViewFactory.navigateToCenter() en lugar de acceder al MainController directamente.
     */
    private void onOpen() {
        Course course = getItem();
        if (course == null) return;

        // Cargar vista de detalle y pasarle los datos del curso
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/student/course_detail.fxml"));
        try {
            Node view = loader.load();
            StudentCourseDetailController controller = loader.getController();
            controller.setCourse(course);
            AppState.getInstance().getViewFactory().navigateToCenter(view);
        } catch (IOException e) {
            System.err.println("Error cargando detalle del curso: " + e.getMessage());
        }
    }
}
