package com.acikgozkaan.auth_service.service.impl;

import com.acikgozkaan.auth_service.client.UserServiceClient;
import com.acikgozkaan.auth_service.dto.request.LoginRequest;
import com.acikgozkaan.auth_service.dto.request.RegisterRequest;
import com.acikgozkaan.auth_service.dto.response.LoginResponse;
import com.acikgozkaan.auth_service.entity.Role;
import com.acikgozkaan.auth_service.entity.UserCredential;
import com.acikgozkaan.auth_service.repository.UserCredentialRepository;
import com.acikgozkaan.auth_service.security.JwtService;
import com.acikgozkaan.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserServiceClient userServiceClient;

    @Override
    //@Transactional
    public void register(RegisterRequest request) {
        if (userCredentialRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email is already in use");
        }

        UserCredential user = UserCredential.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.PATRON)
                .build();

        userCredentialRepository.save(user);

        /*
        userServiceClient.createUserProfile(
                new UserProfileCreateRequest(
                        user.getId(),
                        request.name(),
                        request.surname(),
                        request.phone(),
                        request.address()
                )
        );

         */
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        var user = userCredentialRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return new LoginResponse(token);
    }
}