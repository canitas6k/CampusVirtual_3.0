package com.campusvirtual.controller.admin;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.enums.AdminMenuOption;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del menú lateral del administrador.
 */
public class AdminMenuController implements Initializable {
    @FXML private Button usersBtn;
    @FXML private Button coursesBtn;
    @FXML private Button logoutBtn;

    private final ObjectProperty<AdminMenuOption> selectedOption = new SimpleObjectProperty<>();
    public ObjectProperty<AdminMenuOption> selectedOptionProperty() { return selectedOption; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usersBtn.setOnAction(e -> selectedOption.set(AdminMenuOption.USERS));
        coursesBtn.setOnAction(e -> selectedOption.set(AdminMenuOption.COURSES));

        logoutBtn.setOnAction(e -> {
            AppState.getInstance().clearSession();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            AppState.getInstance().getViewFactory().showLoginWindow();
            AppState.getInstance().getViewFactory().closeStage(stage);
        });
    }
}
