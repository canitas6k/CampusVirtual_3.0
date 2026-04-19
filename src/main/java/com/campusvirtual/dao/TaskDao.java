package com.campusvirtual.dao;

import com.campusvirtual.config.DatabaseConfig;
import com.campusvirtual.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD sobre la tabla tasks.
 * Desde Sprint 2 usa soft-delete: "eliminar" archiva la tarea
 * (deleted_at = NOW()) conservando las entregas de alumnos asociadas.
 */
public class TaskDao {

    /** Obtiene todas las tareas activas de una unidad. */
    public List<Task> findByUnit(int unitId) {
        String sql = "SELECT * FROM tasks WHERE unit_id = ? AND deleted_at IS NULL ORDER BY deadline";
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, unitId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en TaskDao.findByUnit: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Obtiene todas las tareas activas del alumno (de sus asignaturas matriculadas)
     * con nombre de asignatura y unidad para la vista de tareas.
     */
    public List<Task> findByStudent(int studentId) {
        String sql = "SELECT t.*, c.name AS course_name, u.name AS unit_name " +
                     "FROM tasks t " +
                     "JOIN units u ON t.unit_id = u.unit_id " +
                     "JOIN courses c ON u.course_id = c.course_id " +
                     "JOIN enrollments e ON c.course_id = e.course_id " +
                     "WHERE e.student_id = ? AND t.deleted_at IS NULL AND c.deleted_at IS NULL " +
                     "ORDER BY t.deadline";
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task task = mapRow(rs);
                    task.setCourseName(rs.getString("course_name"));
                    task.setUnitName(rs.getString("unit_name"));
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en TaskDao.findByStudent: " + e.getMessage());
        }
        return tasks;
    }

    /** Obtiene todas las tareas activas de las asignaturas de un profesor. */
    public List<Task> findByProfessor(int professorId) {
        String sql = "SELECT t.*, c.name AS course_name, u.name AS unit_name " +
                     "FROM tasks t " +
                     "JOIN units u ON t.unit_id = u.unit_id " +
                     "JOIN courses c ON u.course_id = c.course_id " +
                     "WHERE c.professor_id = ? AND t.deleted_at IS NULL AND c.deleted_at IS NULL " +
                     "ORDER BY t.deadline";
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, professorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task task = mapRow(rs);
                    task.setCourseName(rs.getString("course_name"));
                    task.setUnitName(rs.getString("unit_name"));
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en TaskDao.findByProfessor: " + e.getMessage());
        }
        return tasks;
    }

    /** Crea una nueva tarea. */
    public boolean create(int unitId, String title, String description,
                          java.time.LocalDate deadline, double maxScore) {
        String sql = "INSERT INTO tasks (unit_id, title, description, deadline, max_score) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, unitId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setDate(4, deadline != null ? Date.valueOf(deadline) : null);
            ps.setDouble(5, maxScore);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en TaskDao.create: " + e.getMessage());
            return false;
        }
    }

    /**
     * Archiva (soft-delete) una tarea.
     * Las entregas de los alumnos se conservan para histórico y actas.
     */
    public boolean delete(int taskId) {
        String sql = "UPDATE tasks SET deleted_at = NOW() WHERE task_id = ? AND deleted_at IS NULL";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en TaskDao.delete: " + e.getMessage());
            return false;
        }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        return new Task(
            rs.getInt("task_id"),
            rs.getInt("unit_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getDate("deadline") != null ? rs.getDate("deadline").toLocalDate() : null,
            rs.getDouble("max_score")
        );
    }
}
