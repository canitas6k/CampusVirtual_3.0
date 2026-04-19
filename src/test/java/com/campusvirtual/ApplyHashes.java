package com.campusvirtual;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.campusvirtual.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Utilidad: genera hashes BCrypt y los escribe directamente en la BD via JDBC.
 * Ejecutar con: mvn exec:java -Dexec.mainClass="com.campusvirtual.ApplyHashes" -Dexec.classpathScope=test
 */
public class ApplyHashes {

    private static final String[][] USERS = {
        {"admin",   "admin1234"},
        {"prof1",   "Prof123!"},
        {"prof2",   "Prof123!"},
        {"alumno1", "Alumno123!"},
        {"alumno2", "Alumno123!"},
        {"alumno3", "Alumno123!"},
    };

    public static void main(String[] args) throws Exception {
        System.out.println("=== Aplicando hashes BCrypt a la BD ===");
        Connection conn = DatabaseConfig.getConnection();
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";

        int updated = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String[] u : USERS) {
                String hash = BCrypt.withDefaults().hashToString(12, u[1].toCharArray());
                ps.setString(1, hash);
                ps.setString(2, u[0]);
                int rows = ps.executeUpdate();
                System.out.printf("  %-10s -> %s (%d fila)%n", u[0], hash.substring(0, 20) + "...", rows);
                updated += rows;
            }
        }
        System.out.println("=== " + updated + " usuarios actualizados. ===");
        DatabaseConfig.close();
    }
}
