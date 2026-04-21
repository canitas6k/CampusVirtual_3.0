package com.campusvirtual.web.service;

import com.campusvirtual.web.entity.*;
import com.campusvirtual.web.repository.CourseRepository;
import com.campusvirtual.web.repository.EnrollmentRepository;
import com.campusvirtual.web.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de cursos y matrículas.
 */
@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final UserRepository userRepo;

    public CourseService(CourseRepository courseRepo,
                         EnrollmentRepository enrollmentRepo,
                         UserRepository userRepo) {
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    public List<Course> findAll() {
        return courseRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Course> findById(int id) {
        return courseRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> findAllProfessors() {
        return userRepo.findByRole(Role.PROFESSOR);
    }

    @Transactional(readOnly = true)
    public List<User> findAllStudents() {
        return userRepo.findByRole(Role.STUDENT);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> findEnrollmentsByCourse(Course course) {
        return enrollmentRepo.findByCourse(course);
    }

    /**
     * Crea un nuevo curso y lo asigna al profesor indicado.
     */
    public Course create(String name, String description, Integer professorId) {
        Course course = new Course();
        course.setName(name);
        course.setDescription(description);
        if (professorId != null) {
            userRepo.findById(professorId).ifPresent(course::setProfessor);
        }
        return courseRepo.save(course);
    }

    /**
     * Matricula un alumno en un curso (sin duplicados).
     */
    public void enroll(int studentId, int courseId) {
        User student = userRepo.findById(studentId).orElseThrow();
        Course course = courseRepo.findById(courseId).orElseThrow();
        if (!enrollmentRepo.existsByStudentAndCourse(student, course)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollmentRepo.save(enrollment);
        }
    }

    /**
     * Desmatricula un alumno de un curso.
     */
    public void unenroll(int studentId, int courseId) {
        User student = userRepo.findById(studentId).orElseThrow();
        Course course = courseRepo.findById(courseId).orElseThrow();
        enrollmentRepo.deleteByStudentAndCourse(student, course);
    }

    @Transactional(readOnly = true)
    public List<Course> search(String q) {
        if (q == null || q.isBlank()) return findAll();
        return courseRepo.searchCourses(q.trim());
    }
}
