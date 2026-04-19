package com.campusvirtual.controller.professor;

import com.campusvirtual.core.AppState;
import com.campusvirtual.model.enums.ProfessorMenuOption;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del menú lateral del profesor.
 */
public class ProfessorMenuController implements Initializable {
    @FXML private Button coursesBtn;
    @FXML private Button gradingBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;

    private final ObjectProperty<ProfessorMenuOption> selectedOption = new SimpleObjectProperty<>();
    public ObjectProperty<ProfessorMenuOption> selectedOptionProperty() { return selectedOption; }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        coursesBtn.setOnAction(e -> selectedOption.set(ProfessorMenuOption.COURSES));
        gradingBtn.setOnAction(e -> selectedOption.set(ProfessorMenuOption.GRADING));
        profileBtn.setOnAction(e -> selectedOption.set(ProfessorMenuOption.PROFILE));

        logoutBtn.setOnAction(e -> {
            AppState.getInstance().clearSession();
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            AppState.getInstance().getViewFactory().showLoginWindow();
            AppState.getInstance().getViewFactory().closeStage(stage);
        });
    }
}
