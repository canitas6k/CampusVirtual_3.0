package com.campusvirtual.web.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad JPA que mapea la tabla 'submissions' (entregas de alumnos).
 */
@Entity
@Table(name = "submissions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "student_id"}))
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "student_comment", columnDefinition = "TEXT")
    private String studentComment;

    @Column(name = "grade", precision = 4, scale = 2)
    private Double grade;

    @Column(name = "professor_comment", columnDefinition = "TEXT")
    private String professorComment;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @PrePersist
    private void prePersist() {
        submittedAt = LocalDateTime.now();
    }

    // ── Getters y setters ─────────────────────────────────────────

    public Integer getId() { return id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getStudentComment() { return studentComment; }
    public void setStudentComment(String studentComment) { this.studentComment = studentComment; }

    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }

    public String getProfessorComment() { return professorComment; }
    public void setProfessorComment(String professorComment) { this.professorComment = professorComment; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    public boolean isGraded() { return grade != null; }
}
