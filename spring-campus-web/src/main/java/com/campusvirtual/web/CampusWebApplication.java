package com.campusvirtual.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del servidor web Campus Virtual — Fase 3.
 * Arrancar con: mvn spring-boot:run
 * Acceder en:   http://localhost:8080
 */
@SpringBootApplication
public class CampusWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusWebApplication.class, args);
    }
}
