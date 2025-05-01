package com.acikgozkaan.api_gateway.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final SecretKey secretKey;

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange,
                             @NonNull WebFilterChain chain) {
        final var request = exchange.getRequest();
        final var path = request.getPath().toString();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        return extractToken(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .map(token -> applyRoleHeaders(exchange, token))
                .map(chain::filter)
                .orElseGet(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/");
    }

    private Optional<String> extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }

    private ServerWebExchange applyRoleHeaders(ServerWebExchange exchange, String token) {
        try {
            var claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String role = claims.get("role", String.class);
            System.out.println("[GATEWAY] Token verified. Role = " + role);
            return exchange.mutate()
                    .request(builder -> {
                        if ("LIBRARIAN".equalsIgnoreCase(role)) {
                            builder.header("X-Librarian-Role", "true");
                            System.out.println("[GATEWAY] X-Librarian-Role header added");
                        } else if ("PATRON".equalsIgnoreCase(role)) {
                            builder.header("X-Patron-Role", "true");
                            System.out.println("[GATEWAY] X-Patron-Role header added");
                        }
                    })
                    .build();

        } catch (JwtException e) {
            System.out.println("[GATEWAY] Token invalid: " + e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }
}