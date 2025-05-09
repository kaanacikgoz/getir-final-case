package com.acikgozkaan.user_service.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Please provide a valid email address")
        @Size(max = 75, message = "Email cannot exceed 75 characters")
        String email,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password

) {}