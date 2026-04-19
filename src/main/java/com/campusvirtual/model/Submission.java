package com.campusvirtual.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa una entrega de tarea realizada por un alumno.
 * Incluye campos de calificación del profesor.
 */
public class Submission {
    private final int id;
    private final int taskId;
    private final int studentId;
    private final LocalDateTime submittedAt;
    private final String filePath;
    private final String studentComment;
    private Double grade;
    private String professorComment;
    private LocalDateTime gradedAt;

    // Campos auxiliares para visualización
    private String studentName;
    private String taskTitle;
    private String courseName;

    public Submission(int id, int taskId, int studentId, LocalDateTime submittedAt,
                      String filePath, String studentComment, Double grade,
                      String professorComment, LocalDateTime gradedAt) {
        this.id = id;
        this.taskId = taskId;
        this.studentId = studentId;
        this.submittedAt = submittedAt;
        this.filePath = filePath;
        this.studentComment = studentComment;
        this.grade = grade;
        this.professorComment = professorComment;
        this.gradedAt = gradedAt;
    }

    // Getters
    public int getId() { return id; }
    public int getTaskId() { return taskId; }
    public int getStudentId() { return studentId; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public String getFilePath() { return filePath; }
    public String getStudentComment() { return studentComment; }
    public Double getGrade() { return grade; }
    public String getProfessorComment() { return professorComment; }
    public LocalDateTime getGradedAt() { return gradedAt; }
    public String getStudentName() { return studentName; }
    public String getTaskTitle() { return taskTitle; }
    public String getCourseName() { return courseName; }
    public boolean isGraded() { return grade != null; }

    public String getGradeText() {
        return grade != null ? String.format("%.1f", grade) : "Pendiente";
    }

    public String getSubmittedAtText() {
        return submittedAt != null ? submittedAt.toLocalDate().toString() : "-";
    }

    // Setters auxiliares
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setGrade(Double grade) { this.grade = grade; }
    public void setProfessorComment(String professorComment) { this.professorComment = professorComment; }
}
