package com.campusvirtual.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Factoría de vistas para la aplicación.
 * Sin caché de vistas: cada carga crea una instancia fresca,
 * eliminando el problema de datos residuales entre sesiones.
 */
public class ViewFactory {
    private BorderPane mainContainer;

    /**
     * Registra el contenedor principal para la navegación central.
     * Cada MainController (Student/Professor/Admin) lo llamará en su initialize().
     */
    public void setMainContainer(BorderPane container) {
        this.mainContainer = container;
    }

    /**
     * Navega el panel central del contenedor principal a la vista indicada.
     */
    public void navigateToCenter(Node view) {
        if (mainContainer != null) {
            mainContainer.setCenter(view);
        }
    }

    /**
     * Carga una vista FXML y devuelve el nodo raíz.
     * Siempre crea una instancia nueva (sin caché).
     */
    @SuppressWarnings("unchecked")
    public <T> T loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            return (T) loader.load();
        } catch (IOException e) {
            System.err.println("Error cargando vista: " + fxmlPath);
            throw new RuntimeException("No se pudo cargar la vista: " + fxmlPath, e);
        }
    }

    /**
     * Muestra la ventana de login.
     */
    public void showLoginWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        createStage(loader);
    }

    /**
     * Muestra la ventana principal según el rol del usuario logueado.
     */
    public void showMainWindow() {
        String fxml = switch (AppState.getInstance().getUserRole()) {
            case PROFESSOR -> "/fxml/professor/main.fxml";
            case ADMIN     -> "/fxml/admin/main.fxml";
            default        -> "/fxml/student/main.fxml";
        };
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        createStage(loader);
    }

    /**
     * Cierra un Stage.
     */
    public void closeStage(Stage stage) {
        stage.close();
    }

    // ── Método interno para crear ventanas ────────────────────

    private void createStage(FXMLLoader loader) {
        Scene scene;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            throw new RuntimeException("Error creando ventana", e);
        }

        Stage stage = new Stage();

        // Cargar icono de forma segura
        InputStream iconStream = getClass().getResourceAsStream("/images/icon.jpg");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }

        stage.setTitle("Campus Virtual");
        stage.setMinWidth(950);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }
}
