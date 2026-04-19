-- =============================================================
-- Migración V4 — Sprint 4: Estructura académica fundacional
-- =============================================================
-- Tablas añadidas (sin modificar las existentes):
--   academic_years  — Curso académico (2025-26, 2026-27...).
--   degrees         — Catálogo de titulaciones del centro.
--
-- Estas tablas son FUNDACIONALES: las funcionalidades actuales
-- de courses/enrollments/tasks siguen funcionando sin cambios.
-- En el futuro se puede asociar un course a un academic_year
-- y un degree para completar el modelo educativo completo.
--
-- Por qué no se migra courses → course_offerings todavía:
--   Requiere refactor completo de DAOs, controladores y vistas.
--   Se deja para una fase posterior documentada.
-- =============================================================

USE campus_virtual;

-- ─── CURSOS ACADÉMICOS ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS academic_years (
    id         INT         AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(20) NOT NULL UNIQUE
        COMMENT 'Ej: 2025-26, 2026-27.',
    start_date DATE        NOT NULL,
    end_date   DATE        NOT NULL,
    is_current BOOLEAN     NOT NULL DEFAULT FALSE
        COMMENT 'Solo uno puede ser TRUE. Gestionar en aplicación.',
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_academic_current (is_current)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Cursos académicos del centro.';

-- ─── TITULACIONES ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS degrees (
    id         INT          AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(20)  NOT NULL UNIQUE
        COMMENT 'Código corto: GII, GADE, GDI...',
    name       VARCHAR(200) NOT NULL
        COMMENT 'Nombre completo: Grado en Ingeniería Informática.',
    years      TINYINT      NOT NULL DEFAULT 4
        COMMENT 'Duración en años del grado.',
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_degrees_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Catálogo de titulaciones ofertadas por el centro.';

-- ─── DATOS DE EJEMPLO ─────────────────────────────────────────
INSERT INTO academic_years (name, start_date, end_date, is_current) VALUES
('2025-26', '2025-09-01', '2026-06-30', TRUE),
('2026-27', '2026-09-01', '2027-06-30', FALSE);

INSERT INTO degrees (code, name, years, active) VALUES
('GII',  'Grado en Ingeniería Informática',           4, TRUE),
('GIS',  'Grado en Ingeniería del Software',          4, TRUE),
('GADE', 'Grado en Administración y Dirección de Empresas', 4, TRUE),
('GDI',  'Grado en Diseño e Innovación Digital',     4, TRUE);

-- =============================================================
-- VERIFICACIÓN
-- =============================================================
SELECT 'academic_years' AS tabla, COUNT(*) AS filas FROM academic_years
UNION ALL
SELECT 'degrees', COUNT(*) FROM degrees;
