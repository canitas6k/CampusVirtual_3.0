package com.campusvirtual.web.repository;

import com.campusvirtual.web.entity.Course;
import com.campusvirtual.web.entity.Enrollment;
import com.campusvirtual.web.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Enrollment (matrículas).
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    List<Enrollment> findByStudent(User student);

    List<Enrollment> findByCourse(Course course);

    Optional<Enrollment> findByStudentAndCourse(User student, Course course);

    boolean existsByStudentAndCourse(User student, Course course);

    void deleteByStudentAndCourse(User student, Course course);
}
