-- =============================================================
-- Migración V2 — Sprint 2: Saneado de tipos, soft-delete, UNIQUE email
-- =============================================================
-- Cambios incluidos:
--   1. Sanear emails placeholder "." → NULL (4 usuarios de prueba)
--   2. submissions.grade: DOUBLE → DECIMAL(4,2)
--      Razón: floating-point no es apto para notas numéricas.
--      DECIMAL(4,2) garantiza exactitud: 0.00 – 99.99.
--   3. courses: ADD deleted_at, updated_at
--      Soft-delete: borrar curso archiva en vez de destruir histórico.
--      updated_at se actualiza automáticamente en cada UPDATE.
--   4. tasks: ADD deleted_at
--      Soft-delete: eliminar tarea no purga entregas de alumnos.
--   5. users: UNIQUE(email) tras sanear duplicados
--      Previene dos cuentas con el mismo correo.
--
-- Dependencias: V1 debe estar aplicada.
-- Riesgo: BAJO. Únicamente types más estrictos y nuevas columnas.
--         Los DAOs ya filtran NULL correctamente.
-- Reversible: ver comentarios "ROLLBACK" al final.
-- =============================================================

USE campus_virtual;

-- ─── 1. Sanear emails placeholder ─────────────────────────────
-- Convierte "." en NULL para que el UNIQUE posterior funcione.
-- MySQL permite múltiples NULL en columnas UNIQUE.
UPDATE users SET email = NULL WHERE email = '.';

-- Verificación
SELECT CONCAT('Emails saneados: ', COUNT(*), ' filas con email "." convertidas a NULL')
    AS resultado
FROM users WHERE email IS NULL AND active = TRUE;

-- ─── 2. Nota: DECIMAL(4,2) en lugar de DOUBLE ─────────────────
-- DECIMAL(4,2): 2 decimales exactos, rango 00.00 – 99.99.
-- Ejemplo: 8.5 se almacena como 8.50, nunca como 8.4999999...
ALTER TABLE submissions
    MODIFY COLUMN grade DECIMAL(4,2) NULL COMMENT 'Nota 0-10 con 2 dec. exactos. NULL = sin calificar.';

-- ─── 3. Soft-delete y auditoría en courses ────────────────────
-- deleted_at NULL  = curso activo y visible.
-- deleted_at != NULL = curso archivado (oculto en UI pero conservado).
-- updated_at se actualiza automáticamente en cada UPDATE (MySQL).
ALTER TABLE courses
    ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL
        COMMENT 'Soft-delete: fecha de archivo del curso. NULL = activo.'
        AFTER created_at,
    ADD COLUMN updated_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT 'Última modificación (automático).'
        AFTER deleted_at;

-- ─── 4. Soft-delete en tasks ──────────────────────────────────
ALTER TABLE tasks
    ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL
        COMMENT 'Soft-delete: fecha de archivo de la tarea. NULL = activa.'
        AFTER created_at;

-- ─── 5. UNIQUE email ──────────────────────────────────────────
-- Solo funciona después del paso 1 (emails "." ya son NULL).
ALTER TABLE users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

-- =============================================================
-- VERIFICACIÓN FINAL
-- =============================================================
SELECT
    table_name,
    column_name,
    column_type,
    is_nullable,
    column_comment
FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND (
      (table_name = 'submissions' AND column_name = 'grade')   OR
      (table_name = 'courses'     AND column_name IN ('deleted_at','updated_at')) OR
      (table_name = 'tasks'       AND column_name = 'deleted_at')
  )
ORDER BY table_name, column_name;

SELECT
    index_name, GROUP_CONCAT(column_name) AS cols, non_unique
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name = 'users'
  AND index_name = 'uk_users_email'
GROUP BY index_name, non_unique;

-- =============================================================
-- ROLLBACK (ejecutar solo si hay que deshacer)
-- =============================================================
-- ALTER TABLE submissions  MODIFY COLUMN grade DOUBLE NULL;
-- ALTER TABLE courses      DROP COLUMN deleted_at, DROP COLUMN updated_at;
-- ALTER TABLE tasks        DROP COLUMN deleted_at;
-- ALTER TABLE users        DROP INDEX uk_users_email;
-- UPDATE users SET email = '.' WHERE email IS NULL AND role != 'ADMIN';
