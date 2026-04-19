package com.campusvirtual.model;

import com.campusvirtual.model.enums.AccountType;

/**
 * Modelo que representa un usuario del sistema (alumno, profesor o admin).
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String email;
    private AccountType role;
    private String degree;
    private boolean active;

    public User(int id, String username, String passwordHash, String firstName,
                String lastName, String email, AccountType role, String degree, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.degree = degree;
        this.active = active;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public AccountType getRole() { return role; }
    public String getDegree() { return degree; }
    public boolean isActive() { return active; }
    public String getActiveText() { return active ? "Activo" : "Inactivo"; }
    public String getRoleText() { return role.name(); }
}
