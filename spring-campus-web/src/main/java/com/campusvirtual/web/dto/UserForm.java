package com.campusvirtual.web.dto;

import com.campusvirtual.web.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para el formulario de creación/edición de usuarios.
 */
public class UserForm {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 50, message = "Máximo 50 caracteres")
    private String username;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String email;

    @NotNull(message = "El rol es obligatorio")
    private Role role;

    @Size(max = 200)
    private String degree;

    // Contraseña solo obligatoria en creación (en edición puede dejarse vacía)
    private String password;

    // ── Getters y setters ─────────────────────────────────────────

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
