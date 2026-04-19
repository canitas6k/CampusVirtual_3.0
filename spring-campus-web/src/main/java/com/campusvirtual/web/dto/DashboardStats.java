package com.campusvirtual.web.dto;

/**
 * DTO con las métricas globales para el dashboard de administración.
 */
public class DashboardStats {

    private long totalStudents;
    private long activeStudents;
    private long totalProfessors;
    private long activeProfessors;
    private long totalCourses;
    private long pendingSubmissions;

    public DashboardStats(long totalStudents, long activeStudents,
                          long totalProfessors, long activeProfessors,
                          long totalCourses, long pendingSubmissions) {
        this.totalStudents = totalStudents;
        this.activeStudents = activeStudents;
        this.totalProfessors = totalProfessors;
        this.activeProfessors = activeProfessors;
        this.totalCourses = totalCourses;
        this.pendingSubmissions = pendingSubmissions;
    }

    public long getTotalStudents() { return totalStudents; }
    public long getActiveStudents() { return activeStudents; }
    public long getTotalProfessors() { return totalProfessors; }
    public long getActiveProfessors() { return activeProfessors; }
    public long getTotalCourses() { return totalCourses; }
    public long getPendingSubmissions() { return pendingSubmissions; }
}
