package com.campusvirtual.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller que muestra la página de login.
 * Spring Security gestiona el POST /login automáticamente.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "Credenciales incorrectas.");
        }
        if (logout != null) {
            model.addAttribute("logoutMsg", "Sesión cerrada correctamente.");
        }
        return "login";
    }
}
