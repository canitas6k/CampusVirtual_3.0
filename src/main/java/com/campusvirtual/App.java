package com.campusvirtual;

import com.campusvirtual.config.DatabaseConfig;
import com.campusvirtual.core.AppState;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación Campus Virtual.
 * Inicializa la conexión a BD y muestra la ventana de login......!!!!
 * prueba 123456456
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        AppState.getInstance().getViewFactory().showLoginWindow();
    }

    /**
     * Cierra la conexión a la base de datos al cerrar la aplicación.
     */
    @Override
    public void stop() {
        DatabaseConfig.close();
    }
}
