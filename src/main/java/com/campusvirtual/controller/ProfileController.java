package com.campusvirtual.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.campusvirtual.core.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del perfil de usuario, compartido por todos los roles.
 * Permite editar datos personales y cambiar contraseña.
 */
public class ProfileController implements Initializable {
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label idLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField degreeField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private Button saveBtn;
    @FXML private Label messageLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AppState state = AppState.getInstance();

        // Cabecera
        nameLabel.setText(state.getUserFullName());
        emailLabel.setText(state.getUserEmail());
        idLabel.setText("ID: " + state.getUserId() + " | Rol: " + state.getUserRole().name());

        // Campos editables
        firstNameField.setText(state.getUserFirstName());
        lastNameField.setText(state.getUserLastName());
        emailField.setText(state.getUserEmail());

        // Cargar titulación desde la BD
        String degree = state.getUserDao().findAll().stream()
                .filter(u -> u.getId() == state.getUserId())
                .map(u -> u.getDegree())
                .findFirst().orElse("");
        degreeField.setText(degree != null ? degree : "");

        saveBtn.setOnAction(e -> save());
    }

    private void save() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String degree = degreeField.getText().trim();
        String currentPwd = currentPasswordField.getText();
        String newPwd = newPasswordField.getText();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            showMessage("Nombre, apellidos y email son obligatorios.", true);
            return;
        }

        AppState state = AppState.getInstance();
        int userId = state.getUserId();

        // Si quiere cambiar la contraseña
        if (!newPwd.isEmpty()) {
            if (currentPwd.isEmpty()) {
                showMessage("Introduce la contraseña actual para cambiarla.", true);
                return;
            }

            String currentHash = state.getUserDao().getPasswordHash(userId);
            if (currentHash == null) {
                showMessage("Error al verificar la contraseña.", true);
                return;
            }

            BCrypt.Result result = BCrypt.verifyer().verify(currentPwd.toCharArray(), currentHash);
            if (!result.verified) {
                showMessage("Contraseña actual incorrecta.", true);
                return;
            }

            // Actualizar contraseña
            String newHash = BCrypt.withDefaults().hashToString(12, newPwd.toCharArray());
            state.getUserDao().updatePassword(userId, newHash);
        }

        // Actualizar perfil
        boolean updated = state.getUserDao().updateProfile(userId, firstName, lastName, email, degree);
        if (updated) {
            state.setLoggedUser(userId, firstName, lastName, email, state.getUserRole());
            nameLabel.setText(firstName + " " + lastName);
            emailLabel.setText(email);
            currentPasswordField.clear();
            newPasswordField.clear();
            showMessage("Perfil actualizado correctamente ✓", false);
        } else {
            showMessage("Error al actualizar el perfil.", true);
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.setText(text);
        messageLabel.setStyle(isError
                ? "-fx-text-fill: #DC2626; -fx-font-size: 13px;"
                : "-fx-text-fill: #059669; -fx-font-size: 13px;");
        messageLabel.setVisible(true);
    }
}
