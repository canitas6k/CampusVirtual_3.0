package com.campusvirtual.web.config;

import com.campusvirtual.web.entity.User;
import com.campusvirtual.web.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación de UserDetailsService que carga usuarios desde MySQL.
 * Usa el campo password_hash (BCrypt) ya existente en la base de datos.
 */
@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public SecurityUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Cuenta desactivada: " + username);
        }

        // El rol se mapea como ROLE_STUDENT, ROLE_PROFESSOR, ROLE_ADMIN
        String authority = "ROLE_" + user.getRole().name();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .build();
    }
}
