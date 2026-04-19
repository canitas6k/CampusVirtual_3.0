package com.campusvirtual.controller.admin;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.campusvirtual.core.AppState;
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
 * Controlador de gestión de usuarios del administrador.
 * Permite dar de alta y activar/desactivar usuarios.
 */
public class UserManagementController implements Initializable {
    @FXML private TextField newUsername;
    @FXML private PasswordField newPassword;
    @FXML private TextField newFirstName;
    @FXML private TextField newLastName;
    @FXML private TextField newEmail;
    @FXML private ChoiceBox<AccountType> roleChoice;
    @FXML private Button createUserBtn;
    @FXML private Label userMessageLabel;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, String> colAction;
    @FXML private TableColumn<User, String> colDelete;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleChoice.setItems(FXCollections.observableArrayList(
                AccountType.STUDENT, AccountType.PROFESSOR, AccountType.ADMIN));
        roleChoice.setValue(AccountType.STUDENT);

        createUserBtn.setOnAction(e -> createUser());
        setupTable();
        loadUsers();
    }

    private void createUser() {
        String username = newUsername.getText().trim();
        String password = newPassword.getText();
        String firstName = newFirstName.getText().trim();
        String lastName = newLastName.getText().trim();
        String email = newEmail.getText().trim();
        AccountType role = roleChoice.getValue();

        if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showMessage("Rellena todos los campos obligatorios.", true);
            return;
        }

        // Hash de la contraseña con BCrypt
        String hash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        boolean ok = AppState.getInstance().getUserDao()
                .create(username, hash, firstName, lastName, email, role);

        if (ok) {
            showMessage("Usuario creado correctamente ✓", false);
            newUsername.clear(); newPassword.clear();
            newFirstName.clear(); newLastName.clear(); newEmail.clear();
            loadUsers();
        } else {
            showMessage("Error al crear usuario. ¿El username ya existe?", true);
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRoleText()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getActiveText()));

        // Columna de acción: activar/desactivar
        colAction.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                User user = getTableView().getItems().get(getIndex());
                String label = user.isActive() ? "Desactivar" : "Activar";
                String color = user.isActive() ? "#DC2626" : "#059669";

                Button btn = new Button(label);
                btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                           "-fx-background-radius: 6; -fx-padding: 3 10; -fx-font-size: 12px; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    AppState.getInstance().getUserDao().updateStatus(user.getId(), !user.isActive());
                    loadUsers();
                });
                setGraphic(btn);
            }
        });

        // Columna de eliminar permanente (oculto para la propia cuenta)
        colDelete.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                User user = getTableView().getItems().get(getIndex());
                // No mostrar el botón en la propia fila del admin autenticado
                if (user.getId() == AppState.getInstance().getUserId()) {
                    setGraphic(null);
                    return;
                }

                Button btn = new Button("Eliminar");
                btn.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: white; " +
                           "-fx-background-radius: 6; -fx-padding: 3 10; -fx-font-size: 12px; -fx-cursor: hand;");
                btn.setOnAction(e -> confirmAndDelete(user));
                setGraphic(btn);
            }
        });
    }

    /**
     * Muestra diálogo de confirmación y ejecuta el borrado si el usuario acepta.
     * Aplica las siguientes guardas de seguridad antes de eliminar:
     *  - No eliminar la propia cuenta del admin autenticado.
     *  - No eliminar usuarios con entregas académicas registradas.
     */
    private void confirmAndDelete(User user) {
        if (user.getId() == AppState.getInstance().getUserId()) {
            showMessage("No puedes eliminar tu propia cuenta.", true);
            return;
        }
        if (AppState.getInstance().getUserDao().hasSubmissions(user.getId())) {
            showMessage("'" + user.getUsername() + "' tiene entregas académicas y no puede eliminarse.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar permanentemente al usuario '" + user.getUsername() + "'?\n" +
                "Esta acción no se puede deshacer.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Confirmar eliminación");
        confirm.setTitle("Eliminar usuario");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = AppState.getInstance().getUserDao().delete(user.getId());
                if (ok) {
                    showMessage("Usuario '" + user.getUsername() + "' eliminado correctamente ✓", false);
                    loadUsers();
                } else {
                    showMessage("No se pudo eliminar el usuario. Revisa el log.", true);
                }
            }
        });
    }

    private void loadUsers() {
        List<User> users = AppState.getInstance().getUserDao().findAll();
        usersTable.setItems(FXCollections.observableArrayList(users));
    }

    private void showMessage(String msg, boolean isError) {
        userMessageLabel.setText(msg);
        userMessageLabel.setStyle(isError
                ? "-fx-text-fill: #DC2626; -fx-font-size: 13px;"
                : "-fx-text-fill: #059669; -fx-font-size: 13px;");
        userMessageLabel.setVisible(true);
    }
}
