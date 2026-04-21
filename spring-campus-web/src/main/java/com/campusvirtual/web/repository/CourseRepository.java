package com.campusvirtual.web.repository;

import com.campusvirtual.web.entity.Course;
import com.campusvirtual.web.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio JPA para la entidad Course.
 */
public interface CourseRepository extends JpaRepository<Course, Integer> {

    /** Cursos activos de un profesor (excluye archivados). */
    List<Course> findByProfessorAndDeletedAtIsNull(User professor);

    /** Cursos activos con detalle de matrículas y profesor (panel admin). */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.enrollments LEFT JOIN FETCH c.professor " +
           "WHERE c.deletedAt IS NULL ORDER BY c.name")
    List<Course> findAllWithDetails();

    /** Todos los cursos incluidos archivados (solo para informes de histórico). */
    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.professor ORDER BY c.name")
    List<Course> findAllIncludingArchived();

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    long countEnrollmentsByCourseId(int courseId);

    @Query("SELECT c FROM Course c LEFT JOIN c.professor p " +
           "WHERE c.deletedAt IS NULL AND (" +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "(p IS NOT NULL AND LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :q, '%'))))" +
           " ORDER BY c.name")
    List<Course> searchCourses(@Param("q") String q);
}
