package com.campusvirtual;

import javafx.application.Application;

/**
 * Punto de entrada de la JVM.
 *
 * Se separa de App (que extiende Application) porque en proyectos
 * JavaFX con el sistema de módulos (JPMS), la JVM no puede lanzar
 * directamente una clase que herede de Application sin el módulo
 * javafx.graphics en el module-path. Esta clase actúa de puente
 * y evita el error "JavaFX runtime components are missing".
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
