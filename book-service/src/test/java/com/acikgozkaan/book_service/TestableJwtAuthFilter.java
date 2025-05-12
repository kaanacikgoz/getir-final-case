package com.acikgozkaan.book_service;

import com.acikgozkaan.book_service.security.JwtAuthFilter;
import com.acikgozkaan.book_service.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;

public class TestableJwtAuthFilter extends JwtAuthFilter {
    public TestableJwtAuthFilter(JwtService jwtService) {
        super(jwtService);
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request,
                                 @NonNull HttpServletResponse response,
                                 @NonNull FilterChain chain)
            throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }
}