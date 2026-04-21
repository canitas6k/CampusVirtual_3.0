package com.campusvirtual.web.controller;

import com.campusvirtual.web.dto.UserForm;
import com.campusvirtual.web.entity.Role;
import com.campusvirtual.web.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Controller de gestión de usuarios (solo ADMIN).
 * Alta, baja lógica, reactivación y listado.
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ── Listado ───────────────────────────────────────────────────

    @GetMapping
    public String listUsers(@RequestParam(required = false) String search,
                            Model model, Principal principal) {
        model.addAttribute("users", userService.search(search));
        model.addAttribute("roles", Role.values());
        model.addAttribute("currentUsername", principal.getName());
        model.addAttribute("search", search != null ? search : "");
        return "users/list";
    }

    // ── Formulario de creación ────────────────────────────────────

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("form", new UserForm());
        model.addAttribute("roles", Role.values());
        return "users/form";
    }

    @PostMapping("/new")
    public String createUser(@Valid @ModelAttribute("form") UserForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        // Validar username único
        if (!result.hasErrors() && userService.usernameExists(form.getUsername())) {
            result.rejectValue("username", "duplicate", "El nombre de usuario ya existe");
        }
        // Validar contraseña obligatoria en creación
        if (!result.hasErrors() && (form.getPassword() == null || form.getPassword().isBlank())) {
            result.rejectValue("password", "required", "La contraseña es obligatoria");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/form";
        }

        userService.create(form);
        redirect.addFlashAttribute("successMsg", "Usuario creado correctamente.");
        return "redirect:/admin/users";
    }

    // ── Baja lógica ───────────────────────────────────────────────

    @PostMapping("/{id}/deactivate")
    public String deactivateUser(@PathVariable int id, RedirectAttributes redirect) {
        userService.deactivate(id);
        redirect.addFlashAttribute("successMsg", "Usuario desactivado.");
        return "redirect:/admin/users";
    }

    // ── Reactivación ──────────────────────────────────────────────

    @PostMapping("/{id}/activate")
    public String activateUser(@PathVariable int id, RedirectAttributes redirect) {
        userService.activate(id);
        redirect.addFlashAttribute("successMsg", "Usuario reactivado.");
        return "redirect:/admin/users";
    }

    // ── Eliminación permanente ────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable int id,
                             Principal principal,
                             RedirectAttributes redirect) {
        try {
            userService.delete(id, principal.getName());
            redirect.addFlashAttribute("successMsg", "Usuario eliminado permanentemente.");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
