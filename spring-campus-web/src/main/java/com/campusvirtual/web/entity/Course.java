package com.campusvirtual.web.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que mapea la tabla 'courses'.
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private User professor;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Soft-delete: NULL = curso activo; non-NULL = archivado.
     * No usar DELETE sobre cursos — llamar a archivado en servicio.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Última modificación del registro. MySQL actualiza este campo
     * automáticamente en cada UPDATE vía ON UPDATE CURRENT_TIMESTAMP.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Unit> units = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
    }

    // ── Getters y setters ─────────────────────────────────────────

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getProfessor() { return professor; }
    public void setProfessor(User professor) { this.professor = professor; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public boolean isActive() { return deletedAt == null; }

    public List<Enrollment> getEnrollments() { return enrollments; }

    public List<Unit> getUnits() { return units; }

    public int getEnrollmentCount() { return enrollments.size(); }
}
