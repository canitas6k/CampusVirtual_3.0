package com.campusvirtual.model;

/**
 * Modelo que representa una unidad/tema dentro de una asignatura.
 */
public class Unit {
    private final int id;
    private final int courseId;
    private final String name;
    private final int sortOrder;

    public Unit(int id, int courseId, String name, int sortOrder) {
        this.id = id;
        this.courseId = courseId;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    public int getId() { return id; }
    public int getCourseId() { return courseId; }
    public String getName() { return name; }
    public int getSortOrder() { return sortOrder; }

    @Override
    public String toString() { return name; }
}
