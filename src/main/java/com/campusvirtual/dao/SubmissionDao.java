package com.campusvirtual.dao;

import com.campusvirtual.config.DatabaseConfig;
import com.campusvirtual.model.Submission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para operaciones sobre la tabla submissions (entregas de tareas).
 */
public class SubmissionDao {

    /** Obtiene todas las entregas de una tarea específica (para el profesor). */
    public List<Submission> findByTask(int taskId) {
        String sql = "SELECT s.*, CONCAT(u.first_name, ' ', u.last_name) AS student_name " +
                     "FROM submissions s JOIN users u ON s.student_id = u.user_id " +
                     "WHERE s.task_id = ? ORDER BY s.submitted_at";
        return executeWithParam(sql, taskId);
    }

    /** Obtiene todas las entregas de un alumno con info de tarea y asignatura. */
    public List<Submission> findByStudent(int studentId) {
        String sql = "SELECT s.*, CONCAT(u.first_name,' ',u.last_name) AS student_name, " +
                     "t.title AS task_title, c.name AS course_name " +
                     "FROM submissions s " +
                     "JOIN users u ON s.student_id = u.user_id " +
                     "JOIN tasks t ON s.task_id = t.task_id " +
                     "JOIN units un ON t.unit_id = un.unit_id " +
                     "JOIN courses c ON un.course_id = c.course_id " +
                     "WHERE s.student_id = ? ORDER BY s.submitted_at DESC";
        List<Submission> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Submission sub = mapRow(rs);
                    sub.setStudentName(rs.getString("student_name"));
                    sub.setTaskTitle(rs.getString("task_title"));
                    sub.setCourseName(rs.getString("course_name"));
                    list.add(sub);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SubmissionDao.findByStudent: " + e.getMessage());
        }
        return list;
    }

    /**
     * Obtiene entregas pendientes de calificar para un profesor
     * (de todas sus asignaturas).
     */
    public List<Submission> findPendingByProfessor(int professorId) {
        String sql = "SELECT s.*, CONCAT(u.first_name,' ',u.last_name) AS student_name, " +
                     "t.title AS task_title, c.name AS course_name " +
                     "FROM submissions s " +
                     "JOIN users u ON s.student_id = u.user_id " +
                     "JOIN tasks t ON s.task_id = t.task_id " +
                     "JOIN units un ON t.unit_id = un.unit_id " +
                     "JOIN courses c ON un.course_id = c.course_id " +
                     "WHERE c.professor_id = ? AND s.grade IS NULL " +
                     "ORDER BY s.submitted_at";
        List<Submission> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, professorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Submission sub = mapRow(rs);
                    sub.setStudentName(rs.getString("student_name"));
                    sub.setTaskTitle(rs.getString("task_title"));
                    sub.setCourseName(rs.getString("course_name"));
                    list.add(sub);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SubmissionDao.findPendingByProfessor: " + e.getMessage());
        }
        return list;
    }

    /** Verifica si un alumno ya ha entregado una tarea. */
    public Optional<Submission> findByTaskAndStudent(int taskId, int studentId) {
        String sql = "SELECT s.*, CONCAT(u.first_name,' ',u.last_name) AS student_name " +
                     "FROM submissions s JOIN users u ON s.student_id = u.user_id " +
                     "WHERE s.task_id = ? AND s.student_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Submission sub = mapRow(rs);
                    sub.setStudentName(rs.getString("student_name"));
                    return Optional.of(sub);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SubmissionDao.findByTaskAndStudent: " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Crea una nueva entrega. */
    public boolean create(int taskId, int studentId, String filePath, String comment) {
        String sql = "INSERT INTO submissions (task_id, student_id, file_path, student_comment) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.setInt(2, studentId);
            ps.setString(3, filePath);
            ps.setString(4, comment);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en SubmissionDao.create: " + e.getMessage());
            return false;
        }
    }

    /** Califica una entrega (profesor). */
    public boolean grade(int submissionId, double gradeValue, String comment) {
        String sql = "UPDATE submissions SET grade = ?, professor_comment = ?, " +
                     "graded_at = CURRENT_TIMESTAMP WHERE submission_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, gradeValue);
            ps.setString(2, comment);
            ps.setInt(3, submissionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en SubmissionDao.grade: " + e.getMessage());
            return false;
        }
    }

    // ── Auxiliares ─────────────────────────────────────────────

    private List<Submission> executeWithParam(String sql, int param) {
        List<Submission> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Submission sub = mapRow(rs);
                    sub.setStudentName(rs.getString("student_name"));
                    list.add(sub);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SubmissionDao: " + e.getMessage());
        }
        return list;
    }

    private Submission mapRow(ResultSet rs) throws SQLException {
        return new Submission(
            rs.getInt("submission_id"),
            rs.getInt("task_id"),
            rs.getInt("student_id"),
            rs.getTimestamp("submitted_at") != null ? rs.getTimestamp("submitted_at").toLocalDateTime() : null,
            rs.getString("file_path"),
            rs.getString("student_comment"),
            rs.getObject("grade") != null ? rs.getDouble("grade") : null,
            rs.getString("professor_comment"),
            rs.getTimestamp("graded_at") != null ? rs.getTimestamp("graded_at").toLocalDateTime() : null
        );
    }
}
