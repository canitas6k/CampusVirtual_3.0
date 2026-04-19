package com.campusvirtual.web.security;

import com.campusvirtual.web.entity.LoginAttempt;
import com.campusvirtual.web.repository.LoginAttemptRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Listener de eventos de Spring Security.
 * Registra automáticamente en login_attempts todo intento de autenticación
 * realizado desde el panel web, sin modificar los controladores existentes.
 *
 * No bloquea el flujo: si falla el registro el login continúa igualmente.
 */
@Component
public class AuthAuditListener {

    private final LoginAttemptRepository loginAttemptRepo;

    public AuthAuditListener(LoginAttemptRepository loginAttemptRepo) {
        this.loginAttemptRepo = loginAttemptRepo;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        record(event.getAuthentication().getName(), true);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        record(event.getAuthentication().getName(), false);
    }

    private void record(String username, boolean success) {
        try {
            LoginAttempt attempt = new LoginAttempt();
            attempt.setUsername(username);
            attempt.setSuccess(success);
            attempt.setIp(getClientIp());
            attempt.setUserAgent(getUserAgent());
            loginAttemptRepo.save(attempt);
        } catch (Exception e) {
            System.err.println("AuthAuditListener: no se pudo registrar intento — " + e.getMessage());
        }
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String forwarded = req.getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank())
                    ? forwarded.split(",")[0].trim()
                    : req.getRemoteAddr();
        } catch (Exception e) { return null; }
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            return attrs.getRequest().getHeader("User-Agent");
        } catch (Exception e) { return null; }
    }
}
