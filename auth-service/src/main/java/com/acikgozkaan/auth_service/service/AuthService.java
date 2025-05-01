package com.acikgozkaan.auth_service.service;

import com.acikgozkaan.auth_service.dto.request.LoginRequest;
import com.acikgozkaan.auth_service.dto.request.RegisterRequest;
import com.acikgozkaan.auth_service.dto.response.LoginResponse;

public interface AuthService {

    void register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
