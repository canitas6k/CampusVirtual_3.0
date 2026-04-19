package com.campusvirtual.dao;

import com.campusvirtual.config.DatabaseConfig;
import com.campusvirtual.model.Unit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD sobre la tabla units.
 */
public class UnitDao {

    /** Obtiene todas las unidades de una asignatura, ordenadas. */
    public List<Unit> findByCourse(int courseId) {
        String sql = "SELECT * FROM units WHERE course_id = ? ORDER BY sort_order, unit_id";
        List<Unit> units = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) units.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en UnitDao.findByCourse: " + e.getMessage());
        }
        return units;
    }

    /** Crea una nueva unidad. */
    public boolean create(int courseId, String name, int sortOrder) {
        String sql = "INSERT INTO units (course_id, name, sort_order) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setString(2, name);
            ps.setInt(3, sortOrder);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en UnitDao.create: " + e.getMessage());
            return false;
        }
    }

    /** Elimina una unidad (y sus ficheros/tareas en cascada). */
    public boolean delete(int unitId) {
        String sql = "DELETE FROM units WHERE unit_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, unitId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en UnitDao.delete: " + e.getMessage());
            return false;
        }
    }

    private Unit mapRow(ResultSet rs) throws SQLException {
        return new Unit(
            rs.getInt("unit_id"),
            rs.getInt("course_id"),
            rs.getString("name"),
            rs.getInt("sort_order")
        );
    }
}
