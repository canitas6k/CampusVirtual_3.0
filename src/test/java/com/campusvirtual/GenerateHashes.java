package com.campusvirtual;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utilidad para generar hashes BCrypt de las contraseñas semilla.
 * Ejecutar como aplicación Java para obtener los hashes.
 * Luego actualizar la tabla users en MySQL con los valores generados.
 *
 * IMPORTANTE: Este fichero está en src/test/ para no incluirlo en producción.
 */
public class GenerateHashes {
    public static void main(String[] args) {
        String[][] users = {
            {"admin",   "admin1234"},
            {"prof1",   "Prof123!"},
            {"prof2",   "Prof123!"},
            {"alumno1", "Alumno123!"},
            {"alumno2", "Alumno123!"},
            {"alumno3", "Alumno123!"}
        };

        System.out.println("=== Hashes BCrypt generados ===\n");

        for (String[] user : users) {
            String hash = BCrypt.withDefaults().hashToString(12, user[1].toCharArray());
            System.out.println("Usuario: " + user[0] + " | Password: " + user[1]);
            System.out.println("Hash: " + hash);
            System.out.println();
        }

        System.out.println("=== SQL para actualizar la BD ===\n");
        for (String[] user : users) {
            String hash = BCrypt.withDefaults().hashToString(12, user[1].toCharArray());
            System.out.printf("UPDATE users SET password_hash = '%s' WHERE username = '%s';%n",
                    hash, user[0]);
        }
    }
}
