package com.campusvirtual.web.service;

import com.campusvirtual.web.dto.DashboardStats;
import com.campusvirtual.web.entity.Role;
import com.campusvirtual.web.repository.CourseRepository;
import com.campusvirtual.web.repository.SubmissionRepository;
import com.campusvirtual.web.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio que calcula las métricas globales del dashboard de administración.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepo;
    private final CourseRepository courseRepo;
    private final SubmissionRepository submissionRepo;

    public DashboardService(UserRepository userRepo,
                            CourseRepository courseRepo,
                            SubmissionRepository submissionRepo) {
        this.userRepo = userRepo;
        this.courseRepo = courseRepo;
        this.submissionRepo = submissionRepo;
    }

    public DashboardStats getStats() {
        long totalStudents     = userRepo.countByRole(Role.STUDENT);
        long activeStudents    = userRepo.countByRoleAndActive(Role.STUDENT, true);
        long totalProfessors   = userRepo.countByRole(Role.PROFESSOR);
        long activeProfessors  = userRepo.countByRoleAndActive(Role.PROFESSOR, true);
        long totalCourses      = courseRepo.count();
        long pendingSubmissions = submissionRepo.countPendingGrading();

        return new DashboardStats(
                totalStudents, activeStudents,
                totalProfessors, activeProfessors,
                totalCourses, pendingSubmissions
        );
    }
}
