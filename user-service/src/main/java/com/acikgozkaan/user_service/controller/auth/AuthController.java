package com.acikgozkaan.user_service.controller.auth;

import com.acikgozkaan.user_service.dto.request.auth.LoginRequest;
import com.acikgozkaan.user_service.dto.request.auth.RegisterRequest;
import com.acikgozkaan.user_service.dto.response.auth.LoginResponse;
import com.acikgozkaan.user_service.dto.response.auth.RegisterResponse;
import com.acikgozkaan.user_service.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.acikgozkaan.user_service.entity.Role.LIBRARIAN;
import static com.acikgozkaan.user_service.entity.Role.PATRON;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerWithRole(request, PATRON));
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping("/register-librarian")
    public ResponseEntity<RegisterResponse> registerLibrarian(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerWithRole(request, LIBRARIAN));
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

}