package com.acikgozkaan.user_service.service.auth;

import com.acikgozkaan.user_service.dto.request.auth.LoginRequest;
import com.acikgozkaan.user_service.dto.request.auth.RegisterRequest;
import com.acikgozkaan.user_service.dto.response.auth.LoginResponse;
import com.acikgozkaan.user_service.dto.response.auth.RegisterResponse;
import com.acikgozkaan.user_service.entity.Role;

public interface AuthService {

    RegisterResponse registerWithRole(RegisterRequest request, Role role);
    LoginResponse login(LoginRequest request);
}