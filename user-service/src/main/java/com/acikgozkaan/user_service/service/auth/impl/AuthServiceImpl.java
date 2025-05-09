package com.acikgozkaan.user_service.service.auth.impl;

import com.acikgozkaan.user_service.dto.request.auth.LoginRequest;
import com.acikgozkaan.user_service.dto.request.auth.RegisterRequest;
import com.acikgozkaan.user_service.dto.response.auth.LoginResponse;
import com.acikgozkaan.user_service.dto.response.auth.RegisterResponse;
import com.acikgozkaan.user_service.entity.Role;
import com.acikgozkaan.user_service.entity.User;
import com.acikgozkaan.user_service.exception.InvalidCredentialsException;
import com.acikgozkaan.user_service.exception.ValidationException;
import com.acikgozkaan.user_service.mapper.UserMapper;
import com.acikgozkaan.user_service.repository.UserRepository;
import com.acikgozkaan.user_service.security.user.CustomUserDetails;
import com.acikgozkaan.user_service.security.jwt.JwtService;
import com.acikgozkaan.user_service.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public RegisterResponse registerWithRole(RegisterRequest request, Role role) {
        validateEmailAndPhone(request.email(), request.phone());

        User user = userMapper.toUser(request, role);
        userRepository.save(user);

        return new RegisterResponse("User successfully registered as " + role.name());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticate(request.email(), request.password());
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

            String token = jwtService.generateToken(
                    userDetails.getId().toString(),
                    userDetails.getRole()
            );

            return new LoginResponse(token);

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new InvalidCredentialsException();
        }
    }

    private void validateEmailAndPhone(String email, String phone) {
        Map<String, String> errors = new HashMap<>();

        if (userRepository.existsByEmail(email)) {
            errors.put("email", "Email already exists: " + email);
        }

        if (userRepository.existsByPhone(phone)) {
            errors.put("phone", "Phone already exists: " + phone);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private Authentication authenticate(String email, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }

}