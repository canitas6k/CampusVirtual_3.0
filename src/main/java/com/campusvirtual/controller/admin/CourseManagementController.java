package com.campusvirtual.controller.admin;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.Course;
import com.campusvirtual.model.User;
import com.campusvirtual.model.enums.AccountType;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador de gestión de asignaturas y matrículas del administrador.
 */
public class CourseManagementController implements Initializable {
    // Alta de asignatura
    @FXML private TextField courseName;
    @FXML private TextField courseDesc;
    @FXML private ChoiceBox<User> professorChoice;
    @FXML private Button createCourseBtn;
    @FXML private Label courseMessage;

    // Tabla de asignaturas
    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, String> colCId;
    @FXML private TableColumn<Course, String> colCName;
    @FXML private TableColumn<Course, String> colCDesc;
    @FXML private TableColumn<Course, String> colCProf;
    @FXML private TableColumn<Course, String> colCAction;

    // Matrículas
    @FXML private ChoiceBox<User> enrollStudent;
    @FXML private ChoiceBox<Course> enrollCourse;
    @FXML private Button enrollBtn;
    @FXML private Label enrollMessage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar profesores en el combo
        List<User> professors = AppState.getInstance().getUserDao().findByRole(AccountType.PROFESSOR);
        professorChoice.setItems(FXCollections.observableArrayList(professors));
        professorChoice.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(User u) { return u != null ? u.getFullName() : ""; }
            @Override public User fromString(String s) { return null; }
        });

        // Cargar alumnos en el combo de matrícula
        List<User> students = AppState.getInstance().getUserDao().findByRole(AccountType.STUDENT);
        enrollStudent.setItems(FXCollections.observableArrayList(students));
        enrollStudent.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(User u) { return u != null ? u.getFullName() + " (" + u.getUsername() + ")" : ""; }
            @Override public User fromString(String s) { return null; }
        });

        createCourseBtn.setOnAction(e -> createCourse());
        enrollBtn.setOnAction(e -> enrollStudentAction());

        setupTable();
        loadCourses();
    }

    private void createCourse() {
        String name = courseName.getText().trim();
        String desc = courseDesc.getText().trim();
        User prof = professorChoice.getValue();

        if (name.isEmpty()) {
            showCourseMsg("El nombre es obligatorio.", true);
            return;
        }
        if (prof == null) {
            showCourseMsg("Selecciona un profesor.", true);
            return;
        }

        boolean ok = AppState.getInstance().getCourseDao().create(name, desc, prof.getId());
        if (ok) {
            showCourseMsg("Asignatura creada correctamente ✓", false);
            courseName.clear(); courseDesc.clear();
            loadCourses();
        } else {
            showCourseMsg("Error al crear asignatura.", true);
        }
    }

    private void enrollStudentAction() {
        User student = enrollStudent.getValue();
        Course course = enrollCourse.getValue();

        if (student == null || course == null) {
            showEnrollMsg("Selecciona alumno y asignatura.", true);
            return;
        }

        boolean ok = AppState.getInstance().getCourseDao().enroll(student.getId(), course.getId());
        if (ok) {
            showEnrollMsg("Matrícula realizada ✓", false);
        } else {
            showEnrollMsg("Error. ¿Ya está matriculado?", true);
        }
    }

    private void setupTable() {
        colCId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colCName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colCDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));
        colCProf.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProfessorName()));

        colCAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Eliminar");
            {
                btn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; " +
                           "-fx-background-radius: 6; -fx-padding: 3 10; -fx-font-size: 12px; -fx-cursor: hand;");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Course course = getTableView().getItems().get(getIndex());
                btn.setOnAction(e -> {
                    AppState.getInstance().getCourseDao().delete(course.getId());
                    loadCourses();
                });
                setGraphic(btn);
            }
        });
    }

    private void loadCourses() {
        List<Course> courses = AppState.getInstance().getCourseDao().findAll();
        coursesTable.setItems(FXCollections.observableArrayList(courses));
        enrollCourse.setItems(FXCollections.observableArrayList(courses));
    }

    private void showCourseMsg(String msg, boolean err) {
        courseMessage.setText(msg);
        courseMessage.setStyle(err ? "-fx-text-fill: #DC2626;" : "-fx-text-fill: #059669;");
        courseMessage.setVisible(true);
    }

    private void showEnrollMsg(String msg, boolean err) {
        enrollMessage.setText(msg);
        enrollMessage.setStyle(err ? "-fx-text-fill: #DC2626;" : "-fx-text-fill: #059669;");
        enrollMessage.setVisible(true);
    }
}
