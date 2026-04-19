package com.campusvirtual.dao;

import com.campusvirtual.config.DatabaseConfig;

import java.sql.*;

/**
 * DAO para la tabla audit_log.
 * Registra acciones relevantes del sistema para trazabilidad
 * (cumplimiento RGPD/LOPDGDD y auditoría interna).
 *
 * Códigos de acción definidos como constantes estáticas.
 * El campo payload acepta JSON libre para almacenar contexto adicional.
 */
public class AuditLogDao {

    // ── Códigos de acción ─────────────────────────────────────
    public static final String LOGIN_SUCCESS     = "LOGIN_SUCCESS";
    public static final String LOGIN_FAIL        = "LOGIN_FAIL";
    public static final String USER_CREATE       = "USER_CREATE";
    public static final String USER_DELETE       = "USER_DELETE";
    public static final String USER_DEACTIVATE   = "USER_DEACTIVATE";
    public static final String USER_ACTIVATE     = "USER_ACTIVATE";
    public static final String USER_PROFILE_EDIT = "USER_PROFILE_EDIT";
    public static final String PASSWORD_CHANGE   = "PASSWORD_CHANGE";
    public static final String COURSE_CREATE     = "COURSE_CREATE";
    public static final String COURSE_ARCHIVE    = "COURSE_ARCHIVE";
    public static final String COURSE_ENROLL     = "COURSE_ENROLL";
    public static final String COURSE_UNENROLL   = "COURSE_UNENROLL";
    public static final String TASK_CREATE       = "TASK_CREATE";
    public static final String TASK_ARCHIVE      = "TASK_ARCHIVE";
    public static final String GRADE_CHANGE      = "GRADE_CHANGE";
    public static final String FILE_UPLOAD       = "FILE_UPLOAD";
    public static final String FILE_DELETE       = "FILE_DELETE";
    public static final String SUBMISSION_CREATE = "SUBMISSION_CREATE";

    // ── Tipos de entidad ──────────────────────────────────────
    public static final String ENTITY_USER       = "user";
    public static final String ENTITY_COURSE     = "course";
    public static final String ENTITY_TASK       = "task";
    public static final String ENTITY_SUBMISSION = "submission";
    public static final String ENTITY_FILE       = "file";
    public static final String ENTITY_SYSTEM     = "system";

    /**
     * Registra una acción en el log de auditoría.
     *
     * @param actorId    ID del usuario que realiza la acción (0 = anónimo/sistema)
     * @param action     Código de acción (usar las constantes de esta clase)
     * @param entityType Tipo de entidad afectada
     * @param entityId   ID del registro afectado
     * @param payload    Detalles en JSON (puede ser null)
     * @param ip         IP del cliente (puede ser null en desktop)
     */
    public void log(int actorId, String action, String entityType, int entityId,
                    String payload, String ip) {
        String sql = "INSERT INTO audit_log (actor_id, action, entity_type, entity_id, payload, ip) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            if (actorId > 0) ps.setInt(1, actorId); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, action);
            ps.setString(3, entityType);
            ps.setInt(4, entityId);
            ps.setString(5, payload);   // null → SQL NULL automático con setString
            ps.setString(6, ip);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Log de auditoría nunca debe bloquear la operación principal
            System.err.println("AuditLogDao.log: " + e.getMessage());
        }
    }

    /**
     * Sobrecarga sin payload ni IP (para operaciones desktop sin contexto de red).
     */
    public void log(int actorId, String action, String entityType, int entityId) {
        log(actorId, action, entityType, entityId, null, null);
    }

    /**
     * Sobrecarga con payload JSON simple.
     */
    public void log(int actorId, String action, String entityType, int entityId, String payload) {
        log(actorId, action, entityType, entityId, payload, null);
    }
}
