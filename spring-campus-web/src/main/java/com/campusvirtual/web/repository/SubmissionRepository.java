package com.campusvirtual.web.repository;

import com.campusvirtual.web.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repositorio JPA para la entidad Submission (entregas).
 */
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

    // Entregas sin calificar (para dashboard de métricas)
    @Query("SELECT s FROM Submission s WHERE s.grade IS NULL ORDER BY s.submittedAt DESC")
    List<Submission> findPendingGrading();

    long countByGradeIsNull();

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.grade IS NULL")
    long countPendingGrading();

    // Comprueba si un usuario tiene entregas académicas registradas
    boolean existsByStudent_Id(int studentId);
}
