package com.uruguaymnatural.backend.dto;

import com.uruguaymnatural.backend.model.Role;

/**
 * The logged-in user as the frontend sees it. Never includes the password.
 *
 * @param displayName name to greet the user with: the tourist's first name, the business's
 *                    legal name or "Administrador"
 */
public record UserDto(Long id, String email, Role role, String displayName) {
}
