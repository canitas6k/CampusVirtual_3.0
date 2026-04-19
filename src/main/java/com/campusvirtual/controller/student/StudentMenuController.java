package com.campusvirtual.controller.student;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.enums.StudentMenuOption;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del menú lateral del estudiante.
 * Emite eventos de navegación mediante JavaFX Properties (patrón Observer).
 */
public class StudentMenuController implements Initializable {
    @FXML private Button dashboardBtn;
    @FXML private Button coursesBtn;
    @FXML private Button tasksBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;

    private final ObjectProperty<StudentMenuOption> selectedOption = new SimpleObjectProperty<>();
    public ObjectProperty<StudentMenuOption> selectedOptionProperty() { return selectedOption; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dashboardBtn.setOnAction(e -> selectedOption.set(StudentMenuOption.DASHBOARD));
        coursesBtn.setOnAction(e -> selectedOption.set(StudentMenuOption.COURSES));
        tasksBtn.setOnAction(e -> selectedOption.set(StudentMenuOption.TASKS));
        historyBtn.setOnAction(e -> selectedOption.set(StudentMenuOption.HISTORY));
        profileBtn.setOnAction(e -> selectedOption.set(StudentMenuOption.PROFILE));

        logoutBtn.setOnAction(e -> {
            // Limpiar sesión antes de volver al login
            AppState.getInstance().clearSession();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            AppState.getInstance().getViewFactory().showLoginWindow();
            AppState.getInstance().getViewFactory().closeStage(stage);
        });
    }
}
