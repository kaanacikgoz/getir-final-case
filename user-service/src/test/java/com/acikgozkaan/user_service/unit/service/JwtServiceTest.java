package com.acikgozkaan.user_service.unit.service;

import com.acikgozkaan.user_service.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String SECRET = "my-test-secret-key-which-is-very-long-1234567890";
    private static final long ONE_HOUR = 1000 * 60 * 60;
    private static final long ONE_MS = 1;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, ONE_HOUR);
    }

    @Test
    void shouldGenerateAndParseValidTokenForPatron() {
        String token = jwtService.generateToken("user123", "PATRON");

        assertThat(token).isNotNull();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo("user123");
        assertThat(jwtService.extractUserRole(token)).isEqualTo("PATRON");
        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void shouldGenerateAndParseValidTokenForLibrarian() {
        String token = jwtService.generateToken("admin123", "LIBRARIAN");

        assertThat(token).isNotNull();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUserId(token)).isEqualTo("admin123");
        assertThat(jwtService.extractUserRole(token)).isEqualTo("LIBRARIAN");
        assertThat(jwtService.isTokenExpired(token)).isFalse();
    }

    @Test
    void shouldDetectInvalidToken() {
        assertThat(jwtService.isTokenValid("this.is.not.a.valid.token")).isFalse();
    }

    @Test
    void shouldDetectExpiredToken() throws InterruptedException {
        JwtService shortExpiryService = new JwtService(SECRET, ONE_MS);

        String token = shortExpiryService.generateToken("user123", "PATRON");
        Thread.sleep(10);

        assertThat(shortExpiryService.isTokenExpired(token)).isTrue();
    }

    @Test
    void shouldLogAndReturnFalseWhenTokenIsExpired() throws InterruptedException {
        JwtService shortExpiryService = new JwtService(SECRET, ONE_MS);

        String token = shortExpiryService.generateToken("user123", "PATRON");
        Thread.sleep(10);

        assertThat(shortExpiryService.isTokenValid(token)).isFalse();
    }

    @Test
    void shouldReturnFalseForUnsupportedJwtToken() {
        assertThat(jwtService.isTokenValid("eyJhbGciOiJub25lIn0.eyJzdWIiOiJ1c2VyIn0.")).isFalse();
    }

    @Test
    void shouldReturnFalseForEmptyToken() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }
}