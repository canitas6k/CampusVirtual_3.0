package com.campusvirtual.web.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla 'enrollments' (matrículas alumno-asignatura).
 */
@Entity
@Table(name = "enrollments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    @PrePersist
    private void prePersist() {
        enrolledAt = LocalDateTime.now();
    }

    // ── Getters y setters ─────────────────────────────────────────

    public Integer getId() { return id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }
}
