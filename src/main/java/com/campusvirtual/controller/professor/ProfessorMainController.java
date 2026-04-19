package com.campusvirtual.controller.professor;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.enums.ProfessorMenuOption;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador principal del área de profesor.
 */
public class ProfessorMainController implements Initializable {
    @FXML private BorderPane mainContainer;
    @FXML private ProfessorMenuController menuController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AppState.getInstance().getViewFactory().setMainContainer(mainContainer);

        menuController.selectedOptionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            Node view = switch (newVal) {
                case COURSES -> AppState.getInstance().getViewFactory().loadView("/fxml/professor/courses.fxml");
                case GRADING -> AppState.getInstance().getViewFactory().loadView("/fxml/professor/grading.fxml");
                case PROFILE -> AppState.getInstance().getViewFactory().loadView("/fxml/common/profile.fxml");
            };
            mainContainer.setCenter(view);
        });
    }
}
