package com.uruguaymnatural.backend.security;

import com.uruguaymnatural.backend.model.Role;

/**
 * The logged-in user of the current request. The interceptor builds it from the token and
 * the controllers receive it with {@code @RequestAttribute(AuthUser.ATTRIBUTE)}.
 */
public record AuthUser(Long id, String email, Role role) {

    public static final String ATTRIBUTE = "authUser";
}
