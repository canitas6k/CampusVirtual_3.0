package com.campusvirtual.controller.admin;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.enums.AdminMenuOption;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador principal del área de administración.
 */
public class AdminMainController implements Initializable {
    @FXML private BorderPane mainContainer;
    @FXML private AdminMenuController menuController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AppState.getInstance().getViewFactory().setMainContainer(mainContainer);

        menuController.selectedOptionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            Node view = switch (newVal) {
                case USERS   -> AppState.getInstance().getViewFactory().loadView("/fxml/admin/users.fxml");
                case COURSES -> AppState.getInstance().getViewFactory().loadView("/fxml/admin/courses.fxml");
            };
            mainContainer.setCenter(view);
        });
    }
}
