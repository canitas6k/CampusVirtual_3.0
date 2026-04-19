package com.campusvirtual;

import com.campusvirtual.config.DatabaseConfig;
import com.campusvirtual.dao.UserDao;
import com.campusvirtual.model.User;
import com.campusvirtual.model.enums.AccountType;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración: conexión a MySQL y operaciones DAO.
 * Requiere MySQL corriendo con campus_virtual cargada y hashes BCrypt actualizados.
 */
@DisplayName("Tests de Conexión y DAO (integración con BD)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseConnectionTest {

    private static boolean dbAvailable = false;
    private static UserDao userDao;

    @BeforeAll
    static void checkDatabase() {
        try {
            Connection conn = DatabaseConfig.getConnection();
            dbAvailable = conn != null && !conn.isClosed();
            if (dbAvailable) {
                userDao = new UserDao();
                System.out.println("✅ Conexión a MySQL establecida — tests de integración activos");
            }
        } catch (Exception e) {
            System.out.println("⚠️  MySQL no disponible: " + e.getMessage());
            System.out.println("   Solo se ejecutan los tests unitarios (LoginLogicTest).");
        }
    }

    // ── Conexión ─────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Conexión a campus_virtual establecida")
    void connectionIsOpen() {
        assumeDbAvailable();
        try {
            Connection conn = DatabaseConfig.getConnection();
            assertNotNull(conn, "La conexión no debe ser null");
            assertFalse(conn.isClosed(), "La conexión debe estar abierta");
        } catch (Exception e) {
            fail("Error al verificar conexión: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Las 7 tablas existen en campus_virtual")
    void allTablesExist() {
        assumeDbAvailable();
        String[] expectedTables = {"users","courses","enrollments","units","files","tasks","submissions"};
        try (Statement st = DatabaseConfig.getConnection().createStatement()) {
            for (String table : expectedTables) {
                try (ResultSet rs = st.executeQuery("SELECT 1 FROM " + table + " LIMIT 1")) {
                    // Si no lanza excepción, la tabla existe
                    assertNotNull(rs, "La tabla '" + table + "' debe existir");
                }
            }
        } catch (Exception e) {
            fail("Error al verificar tablas: " + e.getMessage());
        }
    }

    // ── UserDao: findByUsername ───────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("findByUsername recupera correctamente al primer ADMIN activo de la BD")
    void findAdminByUsername() {
        assumeDbAvailable();
        Optional<User> any = userDao.findByRole(AccountType.ADMIN).stream()
                .filter(User::isActive).findFirst();
        org.junit.jupiter.api.Assumptions.assumeTrue(any.isPresent(),
                "No hay ADMIN activo en BD — test omitido");
        Optional<User> opt = userDao.findByUsername(any.get().getUsername());
        assertTrue(opt.isPresent(), "El ADMIN debe poder recuperarse por username");
        User admin = opt.get();
        assertEquals(AccountType.ADMIN, admin.getRole());
        assertTrue(admin.isActive());
        assertNotNull(admin.getPasswordHash());
        assertNotEquals("PENDING_HASH", admin.getPasswordHash(),
                "El hash no debe ser PENDING_HASH — ejecuta GenerateHashes/ApplyHashes");
    }

    @Test
    @Order(4)
    @DisplayName("findByUsername recupera correctamente a un PROFESSOR activo de la BD")
    void findProfByUsername() {
        assumeDbAvailable();
        Optional<User> any = userDao.findByRole(AccountType.PROFESSOR).stream()
                .filter(User::isActive).findFirst();
        org.junit.jupiter.api.Assumptions.assumeTrue(any.isPresent(),
                "No hay PROFESSOR activo en BD — test omitido");
        Optional<User> opt = userDao.findByUsername(any.get().getUsername());
        assertTrue(opt.isPresent());
        assertEquals(AccountType.PROFESSOR, opt.get().getRole());
        assertTrue(opt.get().isActive());
    }

    @Test
    @Order(5)
    @DisplayName("findByUsername recupera correctamente a un STUDENT activo de la BD")
    void findAlumnoByUsername() {
        assumeDbAvailable();
        Optional<User> any = userDao.findByRole(AccountType.STUDENT).stream()
                .filter(User::isActive).findFirst();
        org.junit.jupiter.api.Assumptions.assumeTrue(any.isPresent(),
                "No hay STUDENT activo en BD — test omitido");
        Optional<User> opt = userDao.findByUsername(any.get().getUsername());
        assertTrue(opt.isPresent());
        assertEquals(AccountType.STUDENT, opt.get().getRole());
        assertTrue(opt.get().isActive());
    }

    @Test
    @Order(6)
    @DisplayName("findByUsername de usuario inexistente devuelve Optional.empty")
    void findNonExistentReturnsEmpty() {
        assumeDbAvailable();
        Optional<User> opt = userDao.findByUsername("usuario_que_no_existe_xyz");
        assertFalse(opt.isPresent(), "Usuario inexistente debe devolver Optional.empty");
    }

    // ── UserDao: findAll ──────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("findAll() devuelve al menos un usuario")
    void findAllReturnsAtLeastSeedUsers() {
        assumeDbAvailable();
        List<User> users = userDao.findAll();
        assertNotNull(users, "La lista no debe ser null");
        assertTrue(users.size() >= 1,
            "Debe haber al menos 1 usuario en BD, encontrados: " + users.size());
    }

    @Test
    @Order(8)
    @DisplayName("findAll() incluye los 3 roles distintos")
    void findAllContainsAllRoles() {
        assumeDbAvailable();
        List<User> users = userDao.findAll();
        boolean hasAdmin    = users.stream().anyMatch(u -> u.getRole() == AccountType.ADMIN);
        boolean hasProf     = users.stream().anyMatch(u -> u.getRole() == AccountType.PROFESSOR);
        boolean hasStudent  = users.stream().anyMatch(u -> u.getRole() == AccountType.STUDENT);
        assertTrue(hasAdmin,   "Debe existir al menos un ADMIN");
        assertTrue(hasProf,    "Debe existir al menos un PROFESSOR");
        assertTrue(hasStudent, "Debe existir al menos un STUDENT");
    }

    // ── UserDao: findByRole ───────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("findByRole(STUDENT) devuelve solo alumnos")
    void findByRoleStudentReturnsStudents() {
        assumeDbAvailable();
        List<User> students = userDao.findByRole(AccountType.STUDENT);
        org.junit.jupiter.api.Assumptions.assumeTrue(!students.isEmpty(),
            "No hay STUDENT en BD — test omitido");
        students.forEach(u ->
            assertEquals(AccountType.STUDENT, u.getRole(), "Todos deben ser STUDENT"));
    }

    @Test
    @Order(10)
    @DisplayName("findByRole(PROFESSOR) devuelve solo profesores")
    void findByRoleProfessorReturnsProfessors() {
        assumeDbAvailable();
        List<User> profs = userDao.findByRole(AccountType.PROFESSOR);
        org.junit.jupiter.api.Assumptions.assumeTrue(!profs.isEmpty(),
            "No hay PROFESSOR en BD — test omitido");
        profs.forEach(u ->
            assertEquals(AccountType.PROFESSOR, u.getRole(), "Todos deben ser PROFESSOR"));
    }

    // ── Hash BCrypt en BD ─────────────────────────────────────────

    @Test
    @Order(11)
    @DisplayName("El hash de un ADMIN en BD tiene formato BCrypt válido")
    void adminHashInDbHasBcryptFormat() {
        assumeDbAvailable();
        Optional<User> any = userDao.findByRole(AccountType.ADMIN).stream()
                .filter(User::isActive).findFirst();
        org.junit.jupiter.api.Assumptions.assumeTrue(any.isPresent(),
                "No hay ADMIN activo en BD — test omitido");
        String hash = any.get().getPasswordHash();
        assertNotNull(hash, "El hash no debe ser null");
        assertTrue(hash.startsWith("$2"), "El hash debe empezar por $2 (BCrypt), actual: " + hash);
        assertEquals(60, hash.length(), "Un hash BCrypt tiene siempre 60 caracteres");
    }

    @Test
    @Order(12)
    @DisplayName("Todos los usuarios activos tienen hash BCrypt válido (no PENDING_HASH)")
    void allActiveUsersHaveValidBcryptHash() {
        assumeDbAvailable();
        List<User> users = userDao.findAll();
        org.junit.jupiter.api.Assumptions.assumeTrue(!users.isEmpty(),
            "No hay usuarios en BD — test omitido");
        for (User u : users) {
            if (!u.isActive()) continue;
            String h = u.getPasswordHash();
            assertNotNull(h, "El usuario " + u.getUsername() + " no tiene hash");
            assertNotEquals("PENDING_HASH", h,
                "El usuario " + u.getUsername() + " tiene PENDING_HASH — ejecuta GenerateHashes/ApplyHashes");
            assertTrue(h.startsWith("$2"),
                "El hash de " + u.getUsername() + " debe empezar por $2 (BCrypt)");
            assertEquals(60, h.length(),
                "El hash de " + u.getUsername() + " debe medir 60 caracteres");
        }
    }

    // ── Helper ────────────────────────────────────────────────────

    private void assumeDbAvailable() {
        org.junit.jupiter.api.Assumptions.assumeTrue(dbAvailable,
            "MySQL no disponible — saltando test de integración");
    }
}
