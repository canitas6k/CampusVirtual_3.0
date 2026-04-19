package com.campusvirtual.dao;

import com.campusvirtual.config.DatabaseConfig;
import com.campusvirtual.model.FileResource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD sobre la tabla files (ficheros subidos a unidades).
 */
public class FileDao {

    /** Obtiene todos los ficheros de una unidad. */
    public List<FileResource> findByUnit(int unitId) {
        String sql = "SELECT * FROM files WHERE unit_id = ? ORDER BY uploaded_at";
        List<FileResource> files = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, unitId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) files.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en FileDao.findByUnit: " + e.getMessage());
        }
        return files;
    }

    /** Obtiene un fichero por su ID. */
    public FileResource findById(int fileId) {
        String sql = "SELECT * FROM files WHERE file_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error en FileDao.findById: " + e.getMessage());
        }
        return null;
    }

    /** Registra un nuevo fichero en la base de datos. */
    public boolean create(int unitId, String fileName, String storagePath,
                          String mimeType, long fileSize) {
        String sql = "INSERT INTO files (unit_id, file_name, storage_path, mime_type, file_size) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, unitId);
            ps.setString(2, fileName);
            ps.setString(3, storagePath);
            ps.setString(4, mimeType);
            ps.setLong(5, fileSize);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en FileDao.create: " + e.getMessage());
            return false;
        }
    }

    /** Elimina un fichero de la base de datos (el fichero físico debe borrarse aparte). */
    public boolean delete(int fileId) {
        String sql = "DELETE FROM files WHERE file_id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, fileId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en FileDao.delete: " + e.getMessage());
            return false;
        }
    }

    private FileResource mapRow(ResultSet rs) throws SQLException {
        return new FileResource(
            rs.getInt("file_id"),
            rs.getInt("unit_id"),
            rs.getString("file_name"),
            rs.getString("storage_path"),
            rs.getString("mime_type"),
            rs.getLong("file_size")
        );
    }
}
