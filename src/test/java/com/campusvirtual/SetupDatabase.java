package com.campusvirtual;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/**
 * Utilidad completa: crea campus_virtual, ejecuta schema.sql y aplica hashes BCrypt.
 */
public class SetupDatabase {

    private static final String[][] USERS = {
        {"admin",   "admin1234"},
        {"prof1",   "Prof123!"},
        {"prof2",   "Prof123!"},
        {"alumno1", "Alumno123!"},
        {"alumno2", "Alumno123!"},
        {"alumno3", "Alumno123!"},
    };

    public static void main(String[] args) throws Exception {

        // Leer db.properties
        Properties props = new Properties();
        try (InputStream is = new FileInputStream("db.properties")) {
            props.load(is);
        }
        String fullUrl  = props.getProperty("db.url");   // jdbc:mysql://localhost:3306/campus_virtual
        String user     = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        // URL sin nombre de BD para crear la BD primero
        String serverUrl = fullUrl.replaceAll("/campus_virtual.*", "")
                                  + "?useSSL=false&serverTimezone=Europe/Madrid&allowPublicKeyRetrieval=true";

        System.out.println("Conectando al servidor MySQL: " + serverUrl);

        try (Connection conn = DriverManager.getConnection(serverUrl, user, password);
             Statement st = conn.createStatement()) {

            // ── 1. Crear la BD ───────────────────────────────────
            System.out.println("\n[1/3] Creando base de datos campus_virtual...");
            st.execute("CREATE DATABASE IF NOT EXISTS campus_virtual " +
                       "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            st.execute("USE campus_virtual");
            System.out.println("      ✅ campus_virtual creada / ya existía.");

            // ── 2. Ejecutar schema.sql (tablas + datos semilla) ──
            System.out.println("\n[2/3] Ejecutando schema.sql...");
            String[] lines = Files.readAllLines(
                Path.of("src/main/resources/sql/schema.sql"),
                java.nio.charset.StandardCharsets.UTF_8).toArray(new String[0]);

            // Parser línea a línea: acumula hasta ";" ignorando comentarios
            StringBuilder buf = new StringBuilder();
            int executed = 0;
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) continue;
                buf.append(" ").append(trimmedLine);
                if (trimmedLine.endsWith(";")) {
                    String stmt = buf.toString().trim();
                    // Quitar el ";" final para execute()
                    stmt = stmt.substring(0, stmt.length() - 1).trim();
                    buf = new StringBuilder();
                    String upper = stmt.toUpperCase();
                    if (stmt.isEmpty() || upper.startsWith("CREATE DATABASE") || upper.startsWith("USE ")) continue;
                    try {
                        st.execute(stmt);
                        executed++;
                    } catch (SQLException e) {
                        if (!e.getMessage().contains("Duplicate entry") && !e.getMessage().contains("already exists")) {
                            System.out.println("      ⚠️  " + e.getMessage().split("\n")[0]);
                        }
                    }
                }
            }
            System.out.println("      ✅ " + executed + " sentencias ejecutadas.");

            // ── 3. Aplicar hashes BCrypt ─────────────────────────
            System.out.println("\n[3/3] Aplicando hashes BCrypt (cost 12)...");
            String sql = "UPDATE campus_virtual.users SET password_hash = ? WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (String[] u : USERS) {
                    String hash = BCrypt.withDefaults().hashToString(12, u[1].toCharArray());
                    ps.setString(1, hash);
                    ps.setString(2, u[0]);
                    int rows = ps.executeUpdate();
                    System.out.printf("      %-10s -> %s... (%d fila)%n",
                        u[0], hash.substring(0, 20), rows);
                }
            }
            System.out.println("      ✅ Hashes aplicados.");

            // ── Verificación final ───────────────────────────────
            System.out.println("\n=== Estado final de la tabla users ===");
            try (ResultSet rs = st.executeQuery(
                    "SELECT username, role, active, LEFT(password_hash,7) hash_ok " +
                    "FROM campus_virtual.users ORDER BY role")) {
                System.out.printf("%-12s %-12s %-8s %s%n", "Username","Role","Active","Hash");
                System.out.println("-".repeat(48));
                while (rs.next()) {
                    System.out.printf("%-12s %-12s %-8s %s...%n",
                        rs.getString("username"), rs.getString("role"),
                        rs.getBoolean("active") ? "TRUE" : "FALSE",
                        rs.getString("hash_ok"));
                }
            }
        }
        System.out.println("\n✅ Setup completado. La BD está lista para el proyecto.");
    }
}
