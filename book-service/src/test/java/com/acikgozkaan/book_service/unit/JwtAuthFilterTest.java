package com.acikgozkaan.book_service.unit;

import com.acikgozkaan.book_service.security.JwtAuthFilter;
import com.acikgozkaan.book_service.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private TestableJwtAuthFilter jwtAuthFilter;

    static class TestableJwtAuthFilter extends JwtAuthFilter {
        public TestableJwtAuthFilter(JwtService jwtService) {
            super(jwtService);
        }

        @Override
        public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, jakarta.servlet.FilterChain chain)
                throws ServletException, IOException {
            super.doFilterInternal(request, response, chain);
        }
    }

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        jwtAuthFilter = new TestableJwtAuthFilter(jwtService);
    }

    @Test
    @DisplayName("Should authenticate and pass filter chain on valid token")
    void shouldAuthenticateWithValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid.token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = spy(new MockFilterChain());

        Claims claims = mock(Claims.class);
        when(jwtService.getClaims("valid.token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user@getir.com");
        when(claims.get("role", String.class)).thenReturn("LIBRARIAN");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should return 401 on expired token")
    void shouldReturnUnauthorizedOnExpiredToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer expired.token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = spy(new MockFilterChain());

        when(jwtService.getClaims("expired.token"))
                .thenThrow(new ExpiredJwtException(null, null, "Expired"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token expired");
    }

    @Test
    @DisplayName("Should return 401 on invalid token")
    void shouldReturnUnauthorizedOnInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = spy(new MockFilterChain());

        when(jwtService.getClaims("invalid.token"))
                .thenThrow(new JwtException("Invalid"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid token");
    }

    @Test
    @DisplayName("Should return 401 when claims are missing")
    void shouldReturnUnauthorizedIfClaimsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer missing.claims");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = spy(new MockFilterChain());

        Claims claims = mock(Claims.class);
        when(jwtService.getClaims("missing.claims")).thenReturn(claims);
        when(claims.getSubject()).thenReturn(null);
        when(claims.get("role", String.class)).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Missing required claims");
    }

    @Test
    @DisplayName("Should pass through if no Authorization header")
    void shouldPassThroughIfNoToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(); // No auth header
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = spy(new MockFilterChain());

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }
}