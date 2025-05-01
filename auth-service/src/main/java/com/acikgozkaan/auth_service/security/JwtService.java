package com.acikgozkaan.auth_service.security;

import com.acikgozkaan.auth_service.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    private final long expirationSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(UUID userId, String email, Role role) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .claim("email", email)
                .claim("role", role.name())
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("Token expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported token: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid token structure: {}", ex.getMessage());
        } catch (SecurityException | IllegalArgumentException ex) {
            log.error("Token validation error: {}", ex.getMessage());
        }
        return false;
    }

    public String extractUserId(String token) {
        try {
            return jwtParser.parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new RuntimeException("User ID extraction failed", ex);
        }
    }

    public String extractRole(String token) {
        try {
            return jwtParser.parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new RuntimeException("Role extraction failed", ex);
        }
    }

}