package com.campusvirtual.web.controller;

import com.campusvirtual.web.service.DashboardService;
import com.campusvirtual.web.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

/**
 * Controller del dashboard principal.
 * Accesible por cualquier usuario autenticado — muestra métricas según el rol.
 */
@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("stats", dashboardService.getStats());
        // Carga el usuario actual para mostrar nombre en la cabecera
        userService.findByUsername(principal.getName())
                .ifPresent(u -> model.addAttribute("currentUser", u));
        return "dashboard";
    }
}
