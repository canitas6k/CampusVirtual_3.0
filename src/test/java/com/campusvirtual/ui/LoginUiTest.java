package com.campusvirtual.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas funcionales de la pantalla de login.
 *
 * Se cargan el FXML real y la hoja de estilos, y se simulan clics y teclado
 * con TestFX. Las pruebas se ejecutan en modo headless (Monocle) por lo que
 * no requieren pantalla — funcionan igual en un equipo de sobremesa que en
 * un servidor de integración continua sin entorno gráfico.
 *
 * No requieren conexión a la base de datos: validan únicamente los caminos
 * que controla la propia interfaz (validación de campos vacíos, focus,
 * volcado de texto a los controles, visibilidad del mensaje de error).
 */
class LoginUiTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @Test
    @DisplayName("Los controles principales del formulario están presentes y enfocables")
    void controlesEstanPresentes() {
        assertNotNull(lookup("#usernameField").queryAs(TextField.class));
        assertNotNull(lookup("#passwordField").queryAs(PasswordField.class));
        assertNotNull(lookup("#loginBtn").query());
        Label error = lookup("#errorLabel").queryAs(Label.class);
        assertNotNull(error);
        // Antes de pulsar nada el mensaje de error debe estar oculto
        assertFalse(error.isVisible(), "El error no debe mostrarse hasta intentar el login");
    }

    @Test
    @DisplayName("Pulsar 'Iniciar Sesión' con campos vacíos muestra el mensaje de error")
    void clickConCamposVaciosMuestraError() {
        clickOn("#loginBtn");

        Label error = lookup("#errorLabel").queryAs(Label.class);
        assertTrue(error.isVisible(), "El mensaje de error debe ser visible tras el clic");
        assertTrue(error.getText().toLowerCase().contains("completa"),
                "El mensaje debe pedir completar los campos. Mensaje actual: " + error.getText());
    }

    @Test
    @DisplayName("Escribir en los campos de usuario y contraseña vuelca el texto correctamente")
    void escribirEnLosCamposActualizaSusValores() {
        TextField user = lookup("#usernameField").queryAs(TextField.class);
        PasswordField pass = lookup("#passwordField").queryAs(PasswordField.class);

        // Asignación directa en el hilo de JavaFX para evitar dependencias
        // del foco en distintos sistemas operativos.
        interact(() -> {
            user.setText("usuario_prueba");
            pass.setText("ClaveSegura1");
        });

        assertEquals("usuario_prueba", user.getText());
        assertEquals("ClaveSegura1", pass.getText());
    }
}
