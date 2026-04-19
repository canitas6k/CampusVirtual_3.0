package com.campusvirtual.controller.student;

import com.campusvirtual.controller.student.cell.CourseCell;
import com.campusvirtual.core.AppState;
import com.campusvirtual.model.Course;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de la lista de asignaturas del estudiante.
 * Carga las asignaturas matriculadas desde la BD.
 */
public class StudentCoursesController implements Initializable {
    @FXML private ListView<Course> courseListView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<Course> courses = AppState.getInstance().getCourseDao()
                .findByStudent(AppState.getInstance().getUserId());

        courseListView.setItems(FXCollections.observableArrayList(courses));
        courseListView.setCellFactory(lv -> new CourseCell());
    }
}
