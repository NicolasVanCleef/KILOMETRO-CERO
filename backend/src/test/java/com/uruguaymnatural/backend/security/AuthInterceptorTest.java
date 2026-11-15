package com.uruguaymnatural.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.UnauthorizedException;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.UserRepository;

class AuthInterceptorTest {

    private static final String SECRET = "una-clave-de-prueba-bien-larga-para-los-tests-1234";

    private UserRepository userRepository;
    private JwtService jwtService;
    private AuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        jwtService = new JwtService(SECRET, 8);
        interceptor = new AuthInterceptor(jwtService, userRepository);
    }

    @Test
    void publicPathNeedsNoLogin() {
        assertTrue(call("/api/activities", null));
    }

    @Test
    void adminPathWithoutTokenIs401() {
        assertThrows(UnauthorizedException.class, () -> call("/api/admin/businesses", null));
    }

    @Test
    void adminPathWithTouristTokenIs403() {
        String token = tokenFor(user(3L, Role.TOURIST, true));

        assertThrows(ForbiddenException.class, () -> call("/api/admin/businesses", token));
    }

    @Test
    void adminPathWithAdminTokenPassesAndExposesTheUser() {
        String token = tokenFor(user(1L, Role.ADMIN, true));
        MockHttpServletRequest request = request("/api/admin/businesses", token);

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

        AuthUser authUser = (AuthUser) request.getAttribute(AuthUser.ATTRIBUTE);
        assertEquals(1L, authUser.id());
        assertEquals(Role.ADMIN, authUser.role());
    }

    @Test
    void tokenOfDisabledUserDoesNotWork() {
        String token = tokenFor(user(5L, Role.PROVIDER, false));

        assertThrows(UnauthorizedException.class, () -> call("/api/provider/business", token));
    }

    @Test
    void changedTokenDoesNotWork() {
        String token = tokenFor(user(1L, Role.ADMIN, true));

        // Replace the last characters of the signature with other ones.
        String changed = token.substring(0, token.length() - 6) + "AAAAAA";

        assertThrows(UnauthorizedException.class, () -> call("/api/admin/businesses", changed));
    }

    @Test
    void tokenSignedWithAnotherKeyDoesNotWork() {
        User admin = user(1L, Role.ADMIN, true);
        String falseToken = new JwtService("otra-clave-distinta-igual-de-larga-para-el-test-99", 8).createToken(admin);

        assertThrows(UnauthorizedException.class, () -> call("/api/admin/businesses", falseToken));
    }

    @Test
    void eachPrefixBelongsToOneRole() {
        assertEquals(Role.ADMIN, AuthInterceptor.requiredRole("/api/admin/tags"));
        assertEquals(Role.PROVIDER, AuthInterceptor.requiredRole("/api/provider/activities"));
        assertEquals(Role.TOURIST, AuthInterceptor.requiredRole("/api/tourist/bookings"));
        assertNull(AuthInterceptor.requiredRole("/api/public/businesses"));
    }

    private boolean call(String path, String token) {
        return interceptor.preHandle(request(path, token), new MockHttpServletResponse(), new Object());
    }

    private static MockHttpServletRequest request(String path, String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
        return request;
    }

    private User user(Long id, Role role, boolean enabled) {
        User user = new User("user" + id + "@mail.com", "hash", role, enabled);
        ReflectionTestUtils.setField(user, "id", id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        return user;
    }

    private String tokenFor(User user) {
        return jwtService.createToken(user);
    }
}
