package com.campusvirtual.web.service;

import com.campusvirtual.web.dto.UserForm;
import com.campusvirtual.web.entity.User;
import com.campusvirtual.web.repository.SubmissionRepository;
import com.campusvirtual.web.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de usuarios (alta, baja lógica, listado).
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepo;
    private final SubmissionRepository submissionRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo,
                       SubmissionRepository submissionRepo,
                       PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.submissionRepo = submissionRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepo.findAllOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(int id) {
        return userRepo.findById(id);
    }

    /**
     * Crea un nuevo usuario con contraseña hasheada con BCrypt.
     */
    public User create(UserForm form) {
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria al crear un usuario");
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());
        user.setDegree(form.getDegree());
        user.setActive(true);
        return userRepo.save(user);
    }

    /**
     * Baja lógica: cambia active = false (no elimina el registro).
     */
    public void deactivate(int userId) {
        userRepo.findById(userId).ifPresent(u -> {
            u.setActive(false);
            userRepo.save(u);
        });
    }

    /**
     * Reactiva un usuario previamente desactivado.
     */
    public void activate(int userId) {
        userRepo.findById(userId).ifPresent(u -> {
            u.setActive(true);
            userRepo.save(u);
        });
    }

    /**
     * Elimina permanentemente un usuario de la base de datos.
     *
     * <p>Restricciones de seguridad (defensa en profundidad — el template también
     * oculta el botón cuando alguna de estas condiciones se cumple):
     * <ul>
     *   <li>No se puede eliminar al propio usuario autenticado.</li>
     *   <li>No se puede eliminar un usuario con entregas académicas registradas
     *       (preservación del historial académico).</li>
     * </ul>
     *
     * <p>Las matrículas se eliminan automáticamente por la FK ON DELETE CASCADE.
     * Los cursos impartidos quedan con professor_id = NULL (FK ON DELETE SET NULL).
     *
     * @throws IllegalStateException si no se cumplen las restricciones.
     */
    public void delete(int userId, String currentUsername) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userId));

        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalStateException("No puedes eliminar tu propia cuenta.");
        }
        if (submissionRepo.existsByStudent_Id(userId)) {
            throw new IllegalStateException(
                "El usuario tiene entregas académicas registradas y no puede eliminarse.");
        }

        userRepo.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }
}
