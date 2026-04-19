-- =============================================================
-- Migración V5 — Sprint 5: Tabla de notificaciones
-- =============================================================
-- notifications — Bandeja de entrada de avisos del sistema.
--
-- Casos de uso:
--   - "Tu entrega ha sido calificada" → alumno.
--   - "Nueva tarea en POO" → alumno.
--   - "Hay 3 entregas pendientes de calificar" → profesor.
--   - "Usuario X creado" → admin.
--
-- Diseño:
--   - read_at NULL = no leída. Non-NULL = fecha de lectura.
--   - type es un código corto libre (GRADE_RECEIVED, NEW_TASK...).
--   - La app puede lanzar un polling o usar WebSocket en el futuro.
-- =============================================================

USE campus_virtual;

CREATE TABLE IF NOT EXISTS notifications (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    user_id    INT         NOT NULL
        COMMENT 'Destinatario de la notificación.',
    type       VARCHAR(50) NOT NULL
        COMMENT 'Código: GRADE_RECEIVED, NEW_TASK, SUBMISSION_PENDING, USER_WELCOME...',
    title      VARCHAR(200) NOT NULL,
    body       TEXT         NULL
        COMMENT 'Cuerpo expandido de la notificación (opcional).',
    link       VARCHAR(500) NULL
        COMMENT 'Ruta interna de la app (ej: /course/3/task/7).',
    read_at    TIMESTAMP    NULL DEFAULT NULL
        COMMENT 'NULL = no leída. Non-NULL = marcada como leída.',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, read_at),
    INDEX idx_notif_user_time (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Bandeja de notificaciones del sistema para todos los roles.';

-- =============================================================
-- VERIFICACIÓN
-- =============================================================
SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name = 'notifications';
