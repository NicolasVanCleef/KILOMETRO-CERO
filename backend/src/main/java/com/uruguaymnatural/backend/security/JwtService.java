package com.uruguaymnatural.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.uruguaymnatural.backend.model.User;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Creates and reads the tokens (JWT) that identify a logged-in user.
 *
 * A token is a signed text that says "this is user N". The frontend sends it in every
 * request; since only the backend knows the secret key, nobody else can make a valid one.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration validity;

    public JwtService(@Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-hours}") long expirationHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validity = Duration.ofHours(expirationHours);
    }

    public String createToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(validity)))
                .signWith(key)
                .compact();
    }

    /** The user id inside the token, or empty if the token is false, changed or expired. */
    public Optional<Long> readUserId(String token) {
        try {
            String subject = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).getPayload().getSubject();
            return Optional.of(Long.valueOf(subject));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
