-- =============================================================
-- Campus Virtual 3.0 — Esquema de Base de Datos
-- Motor: MySQL 8.x
-- =============================================================
-- INSTRUCCIONES:
-- 1. Ejecutar este script completo en MySQL.
-- 2. Luego ejecutar GenerateHashes.java (en src/test/) para
--    generar los hashes BCrypt reales de las contraseñas.
-- 3. Actualizar los password_hash con los valores generados.
-- =============================================================
-- Historial de migraciones incluidas en este schema:
--   V1 — Índices secundarios compuestos (Sprint 1)
--   V2 — Tipos exactos, soft-delete, UNIQUE email (Sprint 2)
-- =============================================================

CREATE DATABASE IF NOT EXISTS campus_virtual
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campus_virtual;

-- ─── USUARIOS ────────────────────────────────────────────────
-- email: permite NULL (usuario sin email verificado).
-- UNIQUE(email) excluye NULLs → varios usuarios pueden tener email=NULL.
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NULL,
    role          ENUM('STUDENT','PROFESSOR','ADMIN') NOT NULL,
    degree        VARCHAR(200),
    active        BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- V1: índice compuesto para filtros de admin (role + active)
    INDEX idx_users_role_active (role, active),
    -- V2: email único (NULLs no cuentan como duplicados en MySQL)
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- ─── ASIGNATURAS ─────────────────────────────────────────────
-- deleted_at: soft-delete. NULL = activo. Non-NULL = archivado.
-- updated_at: se actualiza solo con ON UPDATE CURRENT_TIMESTAMP.
CREATE TABLE IF NOT EXISTS courses (
    course_id    INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    professor_id INT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- V2: soft-delete — nunca borrar fisicamente; archivar con deleted_at
    deleted_at   TIMESTAMP NULL DEFAULT NULL
        COMMENT 'Soft-delete: fecha de archivo. NULL = activo.',
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT 'Última modificación (automático).',
    FOREIGN KEY (professor_id) REFERENCES users(user_id)
        ON DELETE SET NULL
);

-- ─── MATRÍCULAS ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    course_id     INT NOT NULL,
    enrolled_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE(student_id, course_id)
);

-- ─── UNIDADES ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS units (
    unit_id    INT AUTO_INCREMENT PRIMARY KEY,
    course_id  INT NOT NULL,
    name       VARCHAR(200) NOT NULL,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    -- V1: compuesto para findByCourse ORDER BY sort_order sin filesort
    INDEX idx_units_course_order (course_id, sort_order)
);

-- ─── FICHEROS ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS files (
    file_id      INT AUTO_INCREMENT PRIMARY KEY,
    unit_id      INT NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    mime_type    VARCHAR(100),
    file_size    BIGINT DEFAULT 0,
    uploaded_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (unit_id) REFERENCES units(unit_id) ON DELETE CASCADE
);

-- ─── TAREAS ──────────────────────────────────────────────────
-- deleted_at: soft-delete. Las entregas de alumnos se conservan aunque
--             la tarea esté archivada (importante para histórico académico).
CREATE TABLE IF NOT EXISTS tasks (
    task_id     INT AUTO_INCREMENT PRIMARY KEY,
    unit_id     INT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    deadline    DATE,
    max_score   DOUBLE DEFAULT 10.0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- V2: soft-delete — archivar tarea sin borrar entregas asociadas
    deleted_at  TIMESTAMP NULL DEFAULT NULL
        COMMENT 'Soft-delete: fecha de archivo. NULL = activa.',
    FOREIGN KEY (unit_id) REFERENCES units(unit_id) ON DELETE CASCADE,
    -- V1: compuesto para findByUnit ORDER BY deadline sin filesort
    INDEX idx_tasks_unit_deadline (unit_id, deadline)
);

-- ─── ENTREGAS ────────────────────────────────────────────────
-- grade: DECIMAL(4,2) desde V2 (exactitud garantizada 0.00–99.99).
--        DOUBLE anterior podía producir 8.4999... en vez de 8.5.
CREATE TABLE IF NOT EXISTS submissions (
    submission_id    INT AUTO_INCREMENT PRIMARY KEY,
    task_id          INT NOT NULL,
    student_id       INT NOT NULL,
    submitted_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_path        VARCHAR(500),
    student_comment  TEXT,
    grade            DECIMAL(4,2) NULL
        COMMENT 'Nota 0-10 con 2 decimales exactos. NULL = sin calificar.',
    professor_comment TEXT,
    graded_at        TIMESTAMP NULL,
    FOREIGN KEY (task_id)    REFERENCES tasks(task_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(user_id),
    UNIQUE(task_id, student_id),
    -- V1: compuesto para findPendingByProfessor (task_id + grade IS NULL)
    INDEX idx_subs_task_grade (task_id, grade)
);

-- =============================================================
-- DATOS INICIALES (semilla)
-- =============================================================
-- Hashes BCrypt generados con coste 12.
-- Si se regeneran con GenerateHashes.java, reemplazar los 6 valores
-- de password_hash más abajo por la nueva salida.
-- Las contraseñas en texto plano listadas aquí son SOLO referencia
-- para desarrollo local; no deben usarse en producción.
-- =============================================================

-- Contraseñas en texto plano (solo para referencia del desarrollador):
-- admin   -> admin1234
-- prof1   -> Prof123!
-- prof2   -> Prof123!
-- alumno1 -> Alumno123!
-- alumno2 -> Alumno123!
-- alumno3 -> Alumno123!

INSERT INTO users (username, password_hash, first_name, last_name, email, role, degree, active) VALUES
('admin',   '$2a$12$2jsYHyflS84tKZLOUb7JW.34GGV.HABEJ44t96P5HuWLthMrllTiy', 'Administrador', 'Sistema',    'admin@campusvirtual.com',   'ADMIN',     NULL, TRUE),
('prof1',   '$2a$12$AOfKbjoPi.9biH6iXwqeOO5N3K1nfUC9sGFLBjNmuGuyaR1EoZfeS', 'María',         'García López','maria.garcia@uem.es',      'PROFESSOR', NULL, TRUE),
('prof2',   '$2a$12$YaDqKHKbtajKaGJ.Uo534.NFmj49O0kPtbn2A3SIHyUoVHpgGl9GO', 'Carlos',        'Fernández',   'carlos.fernandez@uem.es',  'PROFESSOR', NULL, TRUE),
('alumno1', '$2a$12$smHaBXWAmYNeDad27mKPnehGwEOSboJC1JlPsMgTbkd13E4odEwKG', 'Ana',           'Martínez',    'ana.martinez@uem.es',       'STUDENT',   'Ingeniería Informática', TRUE),
('alumno2', '$2a$12$5LMLPIupnu1eKXieguTShuOdDjVehzWJbcfdcQUCUIHYx/Hewm0u2', 'Pedro',         'Sánchez',     'pedro.sanchez@uem.es',      'STUDENT',   'Ingeniería Informática', TRUE),
('alumno3', '$2a$12$voGrGwkkLVIypZ.wJn21p.xMvugdhcO6OHxyD9OsW1YYHvz9slfNq', 'Laura',         'Rodríguez',   'laura.rodriguez@uem.es',    'STUDENT',   'Ingeniería del Software', TRUE);

-- Asignaturas asignadas a profesores
INSERT INTO courses (name, description, professor_id) VALUES
('Programación Orientada a Objetos', 'Principios de POO con Java 21', 2),
('Bases de Datos',                   'Diseño y administración de BBDD relacionales', 2),
('Estructuras de Datos',             'Algoritmos y estructuras de datos avanzadas', 3);

-- Matrículas de alumnos
INSERT INTO enrollments (student_id, course_id) VALUES
(4, 1), (4, 2), (4, 3),   -- alumno1 en las 3 asignaturas
(5, 1), (5, 3),             -- alumno2 en POO y Estructuras
(6, 2);                     -- alumno3 en BBDD

-- Unidades de ejemplo
INSERT INTO units (course_id, name, sort_order) VALUES
(1, 'Introducción a Java',     1),
(1, 'Clases y Objetos',        2),
(1, 'Herencia y Polimorfismo', 3),
(2, 'Modelo Entidad-Relación', 1),
(2, 'SQL Avanzado',            2),
(3, 'Listas y Pilas',          1),
(3, 'Árboles y Grafos',        2);

-- ─── AUDITORÍA ────────────────────────────────────────────────
-- Registro inmutable de acciones relevantes (RGPD/LOPDGDD).
-- actor_id NULL = acción de sistema o usuario anónimo.
CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    actor_id    INT         NULL
        COMMENT 'user_id del actor. NULL = sistema/anónimo.',
    action      VARCHAR(50) NOT NULL
        COMMENT 'Código de evento: LOGIN_SUCCESS, GRADE_CHANGE, USER_DELETE...',
    entity_type VARCHAR(50) NOT NULL,
    entity_id   INT         NOT NULL,
    payload     JSON        NULL
        COMMENT 'Diff old/new o contexto del evento.',
    ip          VARCHAR(45) NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (actor_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_audit_actor_time  (actor_id, created_at),
    INDEX idx_audit_entity      (entity_type, entity_id),
    INDEX idx_audit_action_time (action, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Registro inmutable de auditoría. NO borrar filas.';

-- ─── INTENTOS DE LOGIN ────────────────────────────────────────
-- Permite bloqueo automático tras N fallos (ventana deslizante).
CREATE TABLE IF NOT EXISTS login_attempts (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    success     BOOLEAN      NOT NULL,
    ip          VARCHAR(45)  NULL,
    user_agent  VARCHAR(500) NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_login_user_time (username, created_at),
    INDEX idx_login_ip_time   (ip, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Historial de autenticaciones para detección de fuerza bruta.';

-- ─── CURSOS ACADÉMICOS ────────────────────────────────────────
CREATE TABLE IF NOT EXISTS academic_years (
    id         INT         AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(20) NOT NULL UNIQUE COMMENT 'Ej: 2025-26.',
    start_date DATE        NOT NULL,
    end_date   DATE        NOT NULL,
    is_current BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_academic_current (is_current)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Cursos académicos del centro.';

-- ─── TITULACIONES ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS degrees (
    id         INT          AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(20)  NOT NULL UNIQUE COMMENT 'Ej: GII, GADE.',
    name       VARCHAR(200) NOT NULL,
    years      TINYINT      NOT NULL DEFAULT 4,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_degrees_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Catálogo de titulaciones ofertadas.';

-- ─── NOTIFICACIONES ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS notifications (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id    INT          NOT NULL,
    type       VARCHAR(50)  NOT NULL
        COMMENT 'Código: GRADE_RECEIVED, NEW_TASK, SUBMISSION_PENDING...',
    title      VARCHAR(200) NOT NULL,
    body       TEXT         NULL,
    link       VARCHAR(500) NULL,
    read_at    TIMESTAMP    NULL DEFAULT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, read_at),
    INDEX idx_notif_user_time (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='Bandeja de notificaciones del sistema.';

-- ─── DATOS FUNDACIONALES ──────────────────────────────────────
INSERT INTO academic_years (name, start_date, end_date, is_current) VALUES
('2025-26', '2025-09-01', '2026-06-30', TRUE),
('2026-27', '2026-09-01', '2027-06-30', FALSE);

INSERT INTO degrees (code, name, years, active) VALUES
('GII',  'Grado en Ingeniería Informática',                    4, TRUE),
('GIS',  'Grado en Ingeniería del Software',                   4, TRUE),
('GADE', 'Grado en Administración y Dirección de Empresas',   4, TRUE),
('GDI',  'Grado en Diseño e Innovación Digital',              4, TRUE);

-- Tareas de ejemplo
INSERT INTO tasks (unit_id, title, description, deadline, max_score) VALUES
(1, 'Ejercicio: Hola Mundo',           'Crear un programa que imprima Hola Mundo',           '2026-05-01', 10.0),
(2, 'Práctica: Clase Persona',          'Implementar una clase Persona con getters/setters',  '2026-05-15', 10.0),
(3, 'Proyecto: Sistema de Herencia',    'Diseñar jerarquía de clases para un sistema de Zoo', '2026-06-01', 10.0),
(4, 'Ejercicio: Diagrama E-R',          'Diseñar el diagrama E-R de una biblioteca',          '2026-05-10', 10.0),
(5, 'Práctica: Consultas SQL',          'Resolver 10 consultas SQL sobre la BD de ejemplo',   '2026-05-20', 10.0);
