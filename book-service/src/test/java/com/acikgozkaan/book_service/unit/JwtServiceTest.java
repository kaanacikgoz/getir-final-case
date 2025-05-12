package com.acikgozkaan.book_service.unit;

import com.acikgozkaan.book_service.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private String token;

    @BeforeEach
    void setUp() {
        String secret = "testsecretkey12345678901234567890123456789012";
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtService = new JwtService(secret);

        token = Jwts.builder()
                .subject("123e4567-e89b-12d3-a456-426614174000")
                .claim("role", "PATRON")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Should parse claims from token correctly")
    void shouldParseClaims() {
        Claims claims = jwtService.getClaims(token);

        assertThat(claims.getSubject()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(claims.get("role", String.class)).isEqualTo("PATRON");
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Should return false for malformed token")
    void shouldReturnFalseForMalformedToken() {
        String malformedToken = "not.a.valid.jwt.token";
        boolean result = jwtService.isTokenValid(malformedToken);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired token")
    void shouldReturnFalseForExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject("expired-user-id")
                .claim("role", "PATRON")
                .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 20))
                .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 10))
                .signWith(Keys.hmacShaKeyFor("testsecretkey12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8)))
                .compact();

        boolean result = jwtService.isTokenValid(expiredToken);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for unsupported token")
    void shouldReturnFalseForUnsupportedToken() {
        String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiIxMjMifQ.";
        boolean result = jwtService.isTokenValid(unsupportedToken);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for empty token")
    void shouldReturnFalseForEmptyToken() {
        String emptyToken = "";
        boolean result = jwtService.isTokenValid(emptyToken);
        assertThat(result).isFalse();
    }

}