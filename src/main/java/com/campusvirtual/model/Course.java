package com.campusvirtual.model;

/**
 * Modelo que representa una asignatura/curso.
 * Incluye el nombre del profesor para visualización en la UI.
 */
public class Course {
    private final int id;
    private final String name;
    private final String description;
    private final int professorId;
    private final String professorName;

    public Course(int id, String name, String description, int professorId, String professorName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.professorId = professorId;
        this.professorName = professorName;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getProfessorId() { return professorId; }
    public String getProfessorName() { return professorName; }

    @Override
    public String toString() {
        return name;
    }
}
