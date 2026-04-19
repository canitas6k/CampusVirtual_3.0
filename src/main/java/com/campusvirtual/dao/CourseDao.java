package com.campusvirtual.dao;

import com.campusvirtual.config.DatabaseConfig;
import com.campusvirtual.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD sobre la tabla courses y enrollments.
 * Desde Sprint 2 usa soft-delete: "eliminar" archiva el curso
 * (deleted_at = NOW()) en lugar de destruir la fila y sus históricos.
 */
public class CourseDao {

    /** Obtiene todas las asignaturas activas con nombre del profesor (para admin). */
    public List<Course> findAll() {
        String sql = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) AS professor_name " +
                     "FROM courses c LEFT JOIN users u ON c.professor_id = u.user_id " +
                     "WHERE c.deleted_at IS NULL " +
                     "ORDER BY c.name";
        return executeQuery(sql);
    }

    /** Obtiene las asignaturas activas asignadas a un profesor. */
    public List<Course> findByProfessor(int professorId) {
        String sql = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) AS professor_name " +
                     "FROM courses c LEFT JOIN users u ON c.professor_id = u.user_id " +
                     "WHERE c.professor_id = ? AND c.deleted_at IS NULL ORDER BY c.name";
        List<Course> courses = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, professorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) courses.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CourseDao.findByProfessor: " + e.getMessage());
        }
        return courses;
    }

    /** Obtiene las asignaturas activas en las que un alumno está matriculado. */
    public List<Course> findByStudent(int studentId) {
        String sql = "SELECT c.*, CONCAT(u.first_name, ' ', u.last_name) AS professor_name " +
                     "FROM courses c " +
                     "JOIN enrollments e ON c.course_id = e.course_id " +
                     "LEFT JOIN users u ON c.professor_id = u.user_id " +
                     "WHERE e.student_id = ? AND c.deleted_at IS NULL ORDER BY c.name";
        List<Course> courses = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) courses.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en CourseDao.findByStudent: " + e.getMessage());
        }
        return courses;
    }

    /** Obtiene los alumnos matriculados en una asignatura. */
    public List<com.campusvirtual.model.User> findStudentsByCourse(int courseId) {
        String sql = "SELECT u.* FROM users u " +
                     "JOIN enrollments e ON u.user_id = e.student_id " +
                     "WHERE e.course_id = ? ORDER BY u.last_name, u.first_name";
        List<com.campusvirtual.model.User> students = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(new com.campusvirtual.model.User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        com.campusvirtual.model.enums.AccountType.valueOf(rs.getString("role")),
                        rs.getString("degree"),
                        rs.getBoolean("active")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en CourseDao.findStudentsByCourse: " + e.getMessage());
        }
        return students;
    }

    /** Crea una nueva asignatura. */
    public boolean create(String name, String description, int professorId) {
        String sql = "INSERT INTO courses (name, description, professor_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, professorId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en CourseDao.create: " + e.getMessage());
            return false;
        }
    }

    /**
     * Archiva (soft-delete) una asignatura.
     * La fila permanece en BD para conservar el histórico de unidades,
     * ficheros, tareas y entregas. No aparece en la UI a partir de este momento.
     */
    public boolean delete(int courseId) {
        String sql = "UPDATE courses SET deleted_at = NOW() WHERE course_id = ? AND deleted_at IS NULL";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en CourseDao.delete: " + e.getMessage());
            return false;
        }
    }

    /** Matricula a un alumno en una asignatura. */
    public boolean enroll(int studentId, int courseId) {
        String sql = "INSERT INTO enrollments (student_id, course_id) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en CourseDao.enroll: " + e.getMessage());
            return false;
        }
    }

    /** Desmatricula a un alumno de una asignatura. */
    public boolean unenroll(int studentId, int courseId) {
        String sql = "DELETE FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en CourseDao.unenroll: " + e.getMessage());
            return false;
        }
    }

    // ── Métodos auxiliares ────────────────────────────────────

    private List<Course> executeQuery(String sql) {
        List<Course> courses = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) courses.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error en CourseDao: " + e.getMessage());
        }
        return courses;
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
            rs.getInt("course_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getInt("professor_id"),
            rs.getString("professor_name")
        );
    }
}
