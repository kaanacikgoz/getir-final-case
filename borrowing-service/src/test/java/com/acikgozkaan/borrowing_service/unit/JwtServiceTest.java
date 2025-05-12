package com.acikgozkaan.borrowing_service.unit;

import com.acikgozkaan.borrowing_service.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "my-very-secret-key-which-should-be-long-enough-for-hmac";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret);
    }

    private String generateToken(long expirationMillis) {
        return Jwts.builder()
                .subject("user123")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void testValidToken() {
        String token = generateToken(10000);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void testExpiredToken() throws InterruptedException {
        String token = generateToken(1);
        Thread.sleep(10);
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void testGetClaims() {
        String token = generateToken(10000);
        Claims claims = jwtService.getClaims(token);
        assertEquals("user123", claims.getSubject());
    }
}