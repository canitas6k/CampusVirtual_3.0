-- =============================================================
-- Migración V3 — Sprint 3: Seguridad y auditoría
-- =============================================================
-- Tablas añadidas:
--   audit_log      — Registro inmutable de acciones relevantes.
--                    Obligatorio RGPD/LOPDGDD para trazabilidad.
--   login_attempts — Historial de intentos de login (éxito/fallo).
--                    Permite bloqueo automático tras N fallos.
--
-- Diseño de audit_log:
--   - El campo payload (JSON) almacena diff old/new o detalles
--     relevantes sin requerir tabla por cada tipo de evento.
--   - actor_id NULL cuando la acción es del sistema/anónimo.
--   - Nunca se hace DELETE sobre esta tabla (inmutabilidad).
--
-- Diseño de login_attempts:
--   - Ventana de bloqueo por ventana deslizante (ej: 15 min).
--   - El índice (username, created_at) permite contar fallos
--     en la ventana sin full scan.
--   - El índice (ip, created_at) permite bloqueo por IP también.
-- =============================================================

USE campus_virtual;

-- ─── AUDITORÍA ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    actor_id    INT         NULL
        COMMENT 'user_id del que ejecuta la acción. NULL = sistema/anónimo.',
    action      VARCHAR(50) NOT NULL
        COMMENT 'Código del evento: USER_CREATE, USER_DELETE, GRADE_CHANGE, COURSE_ARCHIVE, LOGIN_OK, LOGIN_FAIL...',
    entity_type VARCHAR(50) NOT NULL
        COMMENT 'Tabla afectada: user, course, task, submission...',
    entity_id   INT         NOT NULL
        COMMENT 'PK del registro afectado.',
    payload     JSON        NULL
        COMMENT 'Diff old/new o detalles del evento en formato JSON.',
    ip          VARCHAR(45) NULL
        COMMENT 'IP del cliente. IPv4 o IPv6.',
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (actor_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_audit_actor_time  (actor_id, created_at),
    INDEX idx_audit_entity      (entity_type, entity_id),
    INDEX idx_audit_action_time (action, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Registro inmutable de auditoría. NO borrar filas.';

-- ─── INTENTOS DE LOGIN ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS login_attempts (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL
        COMMENT 'Username introducido (puede no existir).',
    success     BOOLEAN     NOT NULL
        COMMENT 'TRUE = login correcto. FALSE = fallo.',
    ip          VARCHAR(45) NULL
        COMMENT 'IP del cliente.',
    user_agent  VARCHAR(500) NULL
        COMMENT 'Navegador/app (útil para detectar bots).',
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_login_user_time (username, created_at),
    INDEX idx_login_ip_time   (ip, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Historial de intentos de autenticación para detección de fuerza bruta.';

-- =============================================================
-- VERIFICACIÓN
-- =============================================================
SELECT table_name, table_comment
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('audit_log', 'login_attempts');
