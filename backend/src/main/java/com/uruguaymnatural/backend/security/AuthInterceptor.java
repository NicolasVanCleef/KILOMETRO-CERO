package com.uruguaymnatural.backend.security;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.UnauthorizedException;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Runs before every /api request and checks who is calling (RNF-16).
 *
 * The first part of the path says which role it is for:
 * /api/admin/... only administrators, /api/provider/... only providers,
 * /api/tourist/... only tourists and /api/account/... any logged-in user.
 * Everything else is public (the portal, the registration forms, the public API).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthInterceptor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // The browser's permission check (CORS preflight) carries no token.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Optional<AuthUser> user = readUser(request.getHeader("Authorization"));
        user.ifPresent(authUser -> request.setAttribute(AuthUser.ATTRIBUTE, authUser));

        String path = request.getRequestURI();
        boolean anyRole = path.startsWith("/api/account/");
        Role required = requiredRole(path);
        if (required == null && !anyRole) {
            return true;
        }
        if (user.isEmpty()) {
            throw new UnauthorizedException("Tenés que iniciar sesión.");
        }
        if (required != null && user.get().role() != required) {
            throw new ForbiddenException("Tu usuario no tiene permiso para hacer esto.");
        }
        return true;
    }

    static Role requiredRole(String path) {
        if (path.startsWith("/api/admin/")) {
            return Role.ADMIN;
        }
        if (path.startsWith("/api/provider/")) {
            return Role.PROVIDER;
        }
        if (path.startsWith("/api/tourist/")) {
            return Role.TOURIST;
        }
        return null;
    }

    /**
     * Reads the "Authorization: Bearer token" header. The user is looked up in the database
     * on every request, so a disabled or deleted account stops working right away.
     */
    private Optional<AuthUser> readUser(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return Optional.empty();
        }
        return jwtService.readUserId(header.substring("Bearer ".length()))
                .flatMap(userRepository::findById)
                .filter(User::isEnabled)
                .map(user -> new AuthUser(user.getId(), user.getEmail(), user.getRole()));
    }
}
