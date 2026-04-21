package com.campusvirtual.dao;

import com.campusvirtual.config.DatabaseConfig;
import com.campusvirtual.model.User;
import com.campusvirtual.model.enums.AccountType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operaciones sobre la tabla users.
 * Todos los métodos usan try-with-resources y PreparedStatement.
 */
public class UserDao {

    /**
     * Busca un usuario activo por username. Devuelve Optional, nunca ResultSet.
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ? AND active = TRUE";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en UserDao.findByUsername: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Busca usuarios cuyo username, nombre, apellido o email contengan la cadena dada.
     * La comparación es insensible a mayúsculas/minúsculas.
     *
     * @param query cadena de búsqueda (se aplica LIKE %query%)
     * @return lista de usuarios que coinciden con el criterio
     */
    public List<User> search(String query) {
        String like = "%" + query.toLowerCase() + "%";
        String sql = "SELECT * FROM users WHERE " +
                     "LOWER(username) LIKE ? OR " +
                     "LOWER(first_name) LIKE ? OR " +
                     "LOWER(last_name) LIKE ? OR " +
                     "LOWER(COALESCE(email,'')) LIKE ? " +
                     "ORDER BY role, last_name";
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en UserDao.search: " + e.getMessage());
        }
        return users;
    }

    /**
     * Obtiene todos los usuarios del sistema (para panel de administración).
     */
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY role, last_name";
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en UserDao.findAll: " + e.getMessage());
        }
        return users;
    }

    /**
     * Obtiene todos los usuarios con un rol específico.
     */
    public List<User> findByRole(AccountType role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY last_name";
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en UserDao.findByRole: " + e.getMessage());
        }
        return users;
    }

    /**
     * Cambia el estado activo/inactivo de un usuario (soft delete).
     */
    public void updateStatus(int userId, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error en UserDao.updateStatus: " + e.getMessage());
        }
    }

    /**
     * Actualiza el perfil de un usuario (sin contraseña).
     */
    public boolean updateProfile(int userId, String firstName, String lastName,
                                 String email, String degree) {
        String sql = "UPDATE users SET first_name=?, last_name=?, email=?, degree=? WHERE user_id=?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, degree);
            ps.setInt(5, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en UserDao.updateProfile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza la contraseña de un usuario (ya hasheada con BCrypt).
     */
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en UserDao.updatePassword: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el hash de la contraseña actual de un usuario.
     */
    public String getPasswordHash(int userId) {
        String sql = "SELECT password_hash FROM users WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("password_hash");
            }
        } catch (SQLException e) {
            System.err.println("Error en UserDao.getPasswordHash: " + e.getMessage());
        }
        return null;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     */
    public boolean create(String username, String passwordHash, String firstName,
                          String lastName, String email, AccountType role) {
        String sql = "INSERT INTO users (username, password_hash, first_name, last_name, email, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, email);
            ps.setString(6, role.name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en UserDao.create: " + e.getMessage());
            return false;
        }
    }

    /**
     * Comprueba si un usuario tiene entregas académicas registradas.
     */
    public boolean hasSubmissions(int userId) {
        String sql = "SELECT 1 FROM submissions WHERE student_id = ? LIMIT 1";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error en UserDao.hasSubmissions: " + e.getMessage());
            return true; // conservador: si falla la consulta, no permitir borrado
        }
    }

    /**
     * Elimina permanentemente un usuario de la base de datos.
     * Las matrículas caen por ON DELETE CASCADE; los cursos quedan con
     * professor_id = NULL (ON DELETE SET NULL).
     *
     * @return true si se eliminó correctamente, false en caso contrario.
     */
    public boolean delete(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en UserDao.delete: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mapea una fila del ResultSet a un objeto User.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            AccountType.valueOf(rs.getString("role")),
            rs.getString("degree"),
            rs.getBoolean("active")
        );
    }
}
