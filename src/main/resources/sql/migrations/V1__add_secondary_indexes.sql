-- =============================================================
-- Migración V1 — Sprint 1: Índices secundarios compuestos
-- =============================================================
-- Objetivo: convertir consultas críticas en O(log n) y eliminar
--           "Using filesort" en ORDER BY recurrentes.
--
-- Riesgo:   ninguno. Solo añade índices, no modifica datos ni
--           estructura de columnas. Reversible con DROP INDEX.
--
-- Tiempo:   instantáneo en BD < 100k filas. En BD grande, MySQL
--           usa ALGORITHM=INPLACE sin bloquear escrituras.
--
-- IMPORTANTE: este script es IDEMPOTENTE. Usa procedimiento
-- temporal para crear índices solo si no existen, evitando el
-- error 1061 ("Duplicate key name") al re-ejecutarlo.
--
-- NOTA SOBRE ÍNDICES OMITIDOS:
-- MySQL crea automáticamente un índice por cada FK. No se añaden
-- índices simples redundantes:
--   - enrollments.course_id    (auto por FK)
--   - files.unit_id            (auto por FK)
--   - submissions.student_id   (auto por FK)
--   - units.course_id          (auto por FK, lo cubre el compuesto)
--   - tasks.unit_id            (auto por FK, lo cubre el compuesto)
-- Solo se añaden los COMPUESTOS, que sirven al WHERE Y al ORDER BY
-- a la vez (evitan filesort) y los multi-columna que un FK no cubre.
-- =============================================================

USE campus_virtual;

DELIMITER $$

DROP PROCEDURE IF EXISTS create_index_if_not_exists $$
CREATE PROCEDURE create_index_if_not_exists(
    IN p_table  VARCHAR(64),
    IN p_index  VARCHAR(64),
    IN p_cols   VARCHAR(255)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name   = p_table
          AND index_name   = p_index
    ) THEN
        SET @ddl = CONCAT('CREATE INDEX ', p_index, ' ON ', p_table, ' (', p_cols, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

-- ─── USERS ────────────────────────────────────────────────────
-- Filtros frecuentes: WHERE role=? AND active=? (admin lista usuarios por rol).
-- Hoy: type=ALL (full table scan). Después: type=ref.
CALL create_index_if_not_exists('users', 'idx_users_role_active', 'role, active');

-- ─── UNITS ────────────────────────────────────────────────────
-- findByCourse() ordena por sort_order. Compuesto evita "Using filesort".
CALL create_index_if_not_exists('units', 'idx_units_course_order', 'course_id, sort_order');

-- ─── TASKS ────────────────────────────────────────────────────
-- findByUnit() ORDER BY deadline. Compuesto evita "Using filesort".
CALL create_index_if_not_exists('tasks', 'idx_tasks_unit_deadline', 'unit_id, deadline');

-- ─── SUBMISSIONS ──────────────────────────────────────────────
-- findPendingByProfessor(): WHERE task_id=? AND grade IS NULL.
-- Compuesto sirve al filtro Y a la subconsulta de pendientes,
-- y permite COUNT(*) WHERE grade IS NULL sin tocar tabla.
CALL create_index_if_not_exists('submissions', 'idx_subs_task_grade', 'task_id, grade');

DROP PROCEDURE create_index_if_not_exists;

-- =============================================================
-- VERIFICACIÓN — Listar índices nuevos
-- =============================================================
SELECT table_name, index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND index_name LIKE 'idx_%'
GROUP BY table_name, index_name
ORDER BY table_name, index_name;
