package com.campusvirtual.dao;

import com.campusvirtual.config.DatabaseConfig;

import java.sql.*;

/**
 * DAO para la tabla login_attempts.
 * Registra todos los intentos de autenticación (éxito y fallo)
 * y permite consultar fallos recientes para bloqueo por fuerza bruta.
 */
public class LoginAttemptDao {

    /**
     * Registra un intento de login.
     *
     * @param username nombre de usuario introducido (puede no existir en BD)
     * @param success  true si el login fue exitoso
     * @param ip       IP del cliente (puede ser null en desktop)
     */
    public void record(String username, boolean success, String ip) {
        String sql = "INSERT INTO login_attempts (username, success, ip, user_agent) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setBoolean(2, success);
            ps.setString(3, ip);
            ps.setString(4, "JavaFX-Desktop");
            ps.executeUpdate();
        } catch (SQLException e) {
            // No bloquear login si falla el registro — el intento continúa igualmente
            System.err.println("LoginAttemptDao.record: " + e.getMessage());
        }
    }

    /**
     * Cuenta los intentos fallidos recientes para un usuario.
     * Se usa para decidir si bloquear la cuenta antes de intentar la autenticación.
     *
     * @param username nombre de usuario
     * @param windowMinutes ventana de tiempo en minutos (ej: 15)
     * @return número de fallos en la ventana
     */
    public int countRecentFailed(String username, int windowMinutes) {
        String sql = "SELECT COUNT(*) FROM login_attempts " +
                     "WHERE username = ? AND success = FALSE " +
                     "AND created_at > NOW() - INTERVAL ? MINUTE";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, windowMinutes);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("LoginAttemptDao.countRecentFailed: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Verifica si un usuario está bloqueado (≥ MAX_FAILS fallos en la ventana).
     *
     * @param username     nombre de usuario
     * @param maxFails     número máximo de fallos permitidos
     * @param windowMinutes ventana de tiempo en minutos
     * @return true si el usuario debe ser bloqueado
     */
    public boolean isBlocked(String username, int maxFails, int windowMinutes) {
        return countRecentFailed(username, windowMinutes) >= maxFails;
    }
}
