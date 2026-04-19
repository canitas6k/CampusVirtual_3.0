package com.campusvirtual.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.campusvirtual.core.AppState;
import com.campusvirtual.dao.AuditLogDao;
import com.campusvirtual.model.User;
import com.campusvirtual.model.enums.AccountType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador de la pantalla de login.
 *
 * Seguridad:
 *  - Mensajes de error genéricos (no revelan si el usuario existe).
 *  - Bloqueo por sesión: máximo MAX_ATTEMPTS_SESSION fallos en la sesión activa.
 *  - Bloqueo por BD: consulta login_attempts para bloquear si hay ≥ MAX_ATTEMPTS_DB
 *    fallos en los últimos BLOCK_WINDOW_MINUTES minutos (persiste entre reinicios).
 *  - Registro de todos los intentos en login_attempts.
 *  - Registro de logins exitosos en audit_log.
 */
public class LoginController implements Initializable {

    @FXML private ChoiceBox<AccountType> roleChoiceBox;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Label errorLabel;

    // Protección en sesión (reinicia al cerrar la app)
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS_SESSION = 5;

    // Protección persistente en BD (sobrevive reinicios)
    private static final int MAX_ATTEMPTS_DB      = 10;
    private static final int BLOCK_WINDOW_MINUTES = 15;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleChoiceBox.setItems(FXCollections.observableArrayList(
                AccountType.STUDENT, AccountType.PROFESSOR, AccountType.ADMIN));
        roleChoiceBox.setValue(AccountType.STUDENT);

        loginBtn.setOnAction(e -> onLogin());
        passwordField.setOnAction(e -> onLogin());
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // ── 1. Validación de campos vacíos ────────────────────
        if (username.isEmpty() || password.isEmpty()) {
            showError("Por favor, completa todos los campos.");
            return;
        }

        // ── 2. Bloqueo por sesión ──────────────────────────────
        if (failedAttempts >= MAX_ATTEMPTS_SESSION) {
            showError("Demasiados intentos fallidos. Reinicia la aplicación.");
            loginBtn.setDisable(true);
            return;
        }

        // ── 3. Bloqueo persistente (BD) ───────────────────────
        if (AppState.getInstance().getLoginAttemptDao()
                .isBlocked(username, MAX_ATTEMPTS_DB, BLOCK_WINDOW_MINUTES)) {
            showError("Cuenta bloqueada temporalmente. Inténtalo en " +
                      BLOCK_WINDOW_MINUTES + " minutos.");
            loginBtn.setDisable(true);
            AppState.getInstance().getLoginAttemptDao().record(username, false, null);
            return;
        }

        // ── 4. Consultar usuario en BD ─────────────────────────
        Optional<User> userOpt = AppState.getInstance().getUserDao().findByUsername(username);

        if (userOpt.isEmpty()) {
            registerFailure(username, "usuario no existe");
            return;
        }

        User user = userOpt.get();

        // ── 5. Verificar contraseña con BCrypt ─────────────────
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash());
        if (!result.verified) {
            registerFailure(username, "contraseña incorrecta");
            return;
        }

        // ── 6. Verificar rol seleccionado ─────────────────────
        // Mensaje genérico para no revelar el rol real del usuario
        if (roleChoiceBox.getValue() != user.getRole()) {
            registerFailure(username, "rol incorrecto");
            return;
        }

        // ── 7. Verificar cuenta activa ─────────────────────────
        if (!user.isActive()) {
            registerFailure(username, "cuenta inactiva");
            showError("Esta cuenta está desactivada. Contacta con el administrador.");
            return;
        }

        // ── 8. Login exitoso ──────────────────────────────────
        AppState.getInstance().getLoginAttemptDao().record(username, true, null);
        AppState.getInstance().getAuditLogDao().log(
                user.getId(),
                AuditLogDao.LOGIN_SUCCESS,
                AuditLogDao.ENTITY_USER,
                user.getId(),
                "{\"role\":\"" + user.getRole().name() + "\"}"
        );

        AppState.getInstance().setLoggedUser(
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole()
        );

        Stage currentStage = (Stage) loginBtn.getScene().getWindow();
        AppState.getInstance().getViewFactory().showMainWindow();
        AppState.getInstance().getViewFactory().closeStage(currentStage);
    }

    /** Registra fallo, incrementa contador de sesión y muestra error genérico. */
    private void registerFailure(String username, String reason) {
        failedAttempts++;
        AppState.getInstance().getLoginAttemptDao().record(username, false, null);
        AppState.getInstance().getAuditLogDao().log(
                0,
                AuditLogDao.LOGIN_FAIL,
                AuditLogDao.ENTITY_USER,
                0,
                "{\"username\":\"" + username + "\",\"reason\":\"" + reason + "\"}"
        );
        showError("Credenciales incorrectas.");
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }
}
