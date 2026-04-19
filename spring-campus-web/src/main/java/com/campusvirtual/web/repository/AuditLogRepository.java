package com.campusvirtual.web.repository;

import com.campusvirtual.web.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio JPA para audit_log.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** Últimas N entradas del log (para panel de administración). */
    @Query("SELECT a FROM AuditLog a ORDER BY a.createdAt DESC")
    List<AuditLog> findLatest(org.springframework.data.domain.Pageable pageable);

    /** Entradas del log filtradas por actor. */
    List<AuditLog> findByActor_IdOrderByCreatedAtDesc(Integer actorId);

    /** Entradas del log filtradas por tipo de acción. */
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    /** Entradas del log filtradas por entidad. */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :type AND a.entityId = :id " +
           "ORDER BY a.createdAt DESC")
    List<AuditLog> findByEntity(@Param("type") String entityType, @Param("id") Integer entityId);
}
