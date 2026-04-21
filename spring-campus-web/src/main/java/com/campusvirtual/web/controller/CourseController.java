package com.campusvirtual.web.controller;

import com.campusvirtual.web.entity.Course;
import com.campusvirtual.web.service.CourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller de gestión de cursos y matrículas (solo ADMIN).
 */
@Controller
@RequestMapping("/admin/courses")
@PreAuthorize("hasRole('ADMIN')")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // ── Listado ───────────────────────────────────────────────────

    @GetMapping
    public String listCourses(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("courses", courseService.search(search));
        model.addAttribute("search", search != null ? search : "");
        return "courses/list";
    }

    // ── Detalle + matrículas ───────────────────────────────────────

    @GetMapping("/{id}")
    public String courseDetail(@PathVariable int id, Model model) {
        Course course = courseService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        model.addAttribute("course", course);
        model.addAttribute("enrollments", courseService.findEnrollmentsByCourse(course));
        model.addAttribute("allStudents", courseService.findAllStudents());
        return "courses/detail";
    }

    // ── Formulario de creación ────────────────────────────────────

    @GetMapping("/new")
    public String newCourseForm(Model model) {
        model.addAttribute("professors", courseService.findAllProfessors());
        return "courses/form";
    }

    @PostMapping("/new")
    public String createCourse(@RequestParam String name,
                               @RequestParam(required = false) String description,
                               @RequestParam(required = false) Integer professorId,
                               RedirectAttributes redirect) {
        if (name == null || name.isBlank()) {
            redirect.addFlashAttribute("errorMsg", "El nombre del curso es obligatorio.");
            return "redirect:/admin/courses/new";
        }
        courseService.create(name, description, professorId);
        redirect.addFlashAttribute("successMsg", "Curso creado correctamente.");
        return "redirect:/admin/courses";
    }

    // ── Matricular alumno ──────────────────────────────────────────

    @PostMapping("/{id}/enroll")
    public String enrollStudent(@PathVariable int id,
                                @RequestParam int studentId,
                                RedirectAttributes redirect) {
        courseService.enroll(studentId, id);
        redirect.addFlashAttribute("successMsg", "Alumno matriculado correctamente.");
        return "redirect:/admin/courses/" + id;
    }

    // ── Desmatricular alumno ───────────────────────────────────────

    @PostMapping("/{id}/unenroll")
    public String unenrollStudent(@PathVariable int id,
                                  @RequestParam int studentId,
                                  RedirectAttributes redirect) {
        courseService.unenroll(studentId, id);
        redirect.addFlashAttribute("successMsg", "Alumno desmatriculado.");
        return "redirect:/admin/courses/" + id;
    }
}
