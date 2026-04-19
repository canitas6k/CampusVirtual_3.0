package com.campusvirtual.web.repository;

import com.campusvirtual.web.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * Repositorio JPA para login_attempts.
 */
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    /** Cuenta fallos de un usuario en una ventana de tiempo. */
    @Query("SELECT COUNT(a) FROM LoginAttempt a " +
           "WHERE a.username = :username AND a.success = false " +
           "AND a.createdAt > :since")
    long countFailedSince(@Param("username") String username,
                          @Param("since") LocalDateTime since);

    /** Cuenta todos los intentos (éxito+fallo) en la última hora (para dashboard). */
    long countByCreatedAtAfter(LocalDateTime since);
}
