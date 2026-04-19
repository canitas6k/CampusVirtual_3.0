package com.campusvirtual.model;

import java.time.LocalDate;

/**
 * Modelo que representa una tarea asignada dentro de una unidad.
 */
public class Task {
    private final int id;
    private final int unitId;
    private final String title;
    private final String description;
    private final LocalDate deadline;
    private final double maxScore;
    private String courseName;  // campo auxiliar para mostrar en la lista global de tareas
    private String unitName;    // campo auxiliar

    public Task(int id, int unitId, String title, String description, LocalDate deadline, double maxScore) {
        this.id = id;
        this.unitId = unitId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.maxScore = maxScore;
    }

    // Getters
    public int getId() { return id; }
    public int getUnitId() { return unitId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDeadline() { return deadline; }
    public double getMaxScore() { return maxScore; }
    public String getCourseName() { return courseName; }
    public String getUnitName() { return unitName; }

    // Setters auxiliares para campos de visualización
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public String getDeadlineText() {
        return deadline != null ? deadline.toString() : "Sin fecha";
    }
}
