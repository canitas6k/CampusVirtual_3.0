package com.campusvirtual.controller.student;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.enums.StudentMenuOption;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador principal del área de estudiante.
 * Escucha los cambios del menú y actualiza el contenido central.
 */
public class StudentMainController implements Initializable {
    @FXML private BorderPane mainContainer;
    @FXML private StudentMenuController menuController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Registrar el contenedor en ViewFactory para navegación global
        AppState.getInstance().getViewFactory().setMainContainer(mainContainer);

        // Escuchar cambios en la selección del menú
        menuController.selectedOptionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            Node view = switch (newVal) {
                case DASHBOARD -> AppState.getInstance().getViewFactory().loadView("/fxml/student/dashboard.fxml");
                case COURSES   -> AppState.getInstance().getViewFactory().loadView("/fxml/student/courses.fxml");
                case TASKS     -> AppState.getInstance().getViewFactory().loadView("/fxml/student/tasks.fxml");
                case HISTORY   -> AppState.getInstance().getViewFactory().loadView("/fxml/student/history.fxml");
                case PROFILE   -> AppState.getInstance().getViewFactory().loadView("/fxml/common/profile.fxml");
            };
            mainContainer.setCenter(view);
        });
    }
}
