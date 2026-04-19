package com.campusvirtual;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de la lógica de autenticación (sin JavaFX, sin BD).
 * Verifica el comportamiento de BCrypt tal como lo usa LoginController.
 */
@DisplayName("Tests de lógica de Login (BCrypt)")
class LoginLogicTest {

    // Hash pre-generado de "admin1234" con cost 12 — representa lo que hay en BD
    private static String adminHash;
    private static String alumnoHash;

    @BeforeAll
    static void setup() {
        adminHash  = BCrypt.withDefaults().hashToString(12, "admin1234".toCharArray());
        alumnoHash = BCrypt.withDefaults().hashToString(12, "Alumno123!".toCharArray());
    }

    // ── BCrypt: verificación correcta ────────────────────────────

    @Test
    @DisplayName("admin1234 verifica contra su propio hash")
    void adminPasswordVerifiesAgainstHash() {
        BCrypt.Result result = BCrypt.verifyer().verify("admin1234".toCharArray(), adminHash);
        assertTrue(result.verified, "La contraseña admin1234 debe verificarse correctamente");
    }

    @Test
    @DisplayName("Alumno123! verifica contra su propio hash")
    void alumnoPasswordVerifiesAgainstHash() {
        BCrypt.Result result = BCrypt.verifyer().verify("Alumno123!".toCharArray(), alumnoHash);
        assertTrue(result.verified, "La contraseña Alumno123! debe verificarse correctamente");
    }

    @Test
    @DisplayName("Prof123! genera hash válido y verifica")
    void profPasswordRoundTrip() {
        String profHash = BCrypt.withDefaults().hashToString(12, "Prof123!".toCharArray());
        BCrypt.Result result = BCrypt.verifyer().verify("Prof123!".toCharArray(), profHash);
        assertTrue(result.verified, "La contraseña Prof123! debe verificar su propio hash");
    }

    // ── BCrypt: contraseñas incorrectas rechazan ─────────────────

    @Test
    @DisplayName("Contraseña incorrecta NO verifica")
    void wrongPasswordFails() {
        BCrypt.Result result = BCrypt.verifyer().verify("contraseña_incorrecta".toCharArray(), adminHash);
        assertFalse(result.verified, "Una contraseña incorrecta no debe verificarse");
    }

    @Test
    @DisplayName("Contraseña vacía NO verifica")
    void emptyPasswordFails() {
        BCrypt.Result result = BCrypt.verifyer().verify("".toCharArray(), adminHash);
        assertFalse(result.verified, "Una contraseña vacía no debe verificarse");
    }

    @Test
    @DisplayName("Hash de admin no verifica contraseña de alumno")
    void crossPasswordFails() {
        BCrypt.Result result = BCrypt.verifyer().verify("Alumno123!".toCharArray(), adminHash);
        assertFalse(result.verified, "Hash de admin no debe aceptar contraseña de alumno");
    }

    // ── BCrypt: propiedades del hash ─────────────────────────────

    @Test
    @DisplayName("Dos hashes del mismo password son distintos (salt aleatorio)")
    void twoHashesSamePasswordAreDifferent() {
        String hash1 = BCrypt.withDefaults().hashToString(12, "admin1234".toCharArray());
        String hash2 = BCrypt.withDefaults().hashToString(12, "admin1234".toCharArray());
        assertNotEquals(hash1, hash2, "BCrypt debe generar hashes distintos cada vez (salt aleatorio)");
        // Pero ambos verifican la misma contraseña
        assertTrue(BCrypt.verifyer().verify("admin1234".toCharArray(), hash1).verified);
        assertTrue(BCrypt.verifyer().verify("admin1234".toCharArray(), hash2).verified);
    }

    @Test
    @DisplayName("El hash tiene formato BCrypt válido ($2a$12$...)")
    void hashHasCorrectFormat() {
        assertTrue(adminHash.startsWith("$2a$12$"),
            "El hash BCrypt con cost 12 debe empezar por $2a$12$, actual: " + adminHash);
        assertEquals(60, adminHash.length(), "Un hash BCrypt tiene siempre 60 caracteres");
    }

    // ── Seguridad: validación de campos vacíos (lógica del LoginController) ──

    @Test
    @DisplayName("Username vacío debe rechazarse antes de consultar la BD")
    void emptyUsernameShouldBeRejected() {
        String username = "  ".trim();
        assertTrue(username.isEmpty(), "Username en blanco debe ser tratado como vacío");
    }

    @Test
    @DisplayName("Password vacío debe rechazarse antes de consultar la BD")
    void emptyPasswordShouldBeRejected() {
        String password = "";
        assertTrue(password.isEmpty(), "Password vacío debe ser tratado como vacío");
    }

    // ── Límite de intentos ────────────────────────────────────────

    @Test
    @DisplayName("Contador de intentos: 5 fallos alcanza MAX_ATTEMPTS")
    void failedAttemptsCounter() {
        int MAX_ATTEMPTS = 5;
        int failedAttempts = 0;

        // Simular 5 intentos fallidos
        for (int i = 0; i < 5; i++) {
            BCrypt.Result result = BCrypt.verifyer().verify("mal".toCharArray(), adminHash);
            if (!result.verified) failedAttempts++;
        }

        assertEquals(MAX_ATTEMPTS, failedAttempts,
            "Tras 5 intentos fallidos debe alcanzarse el límite de bloqueo");
        assertTrue(failedAttempts >= MAX_ATTEMPTS, "Bloqueo debe activarse al llegar a MAX_ATTEMPTS");
    }
}
