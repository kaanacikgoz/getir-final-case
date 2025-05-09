package com.acikgozkaan.user_service.dto.request.auth;

import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "Please provide a valid email address")
        @Size(max = 75, message = "Email cannot exceed 75 characters")
        String email,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Name cannot be empty")
        @Size(max = 30, message = "Name cannot exceed 30 characters")
        String name,

        @NotBlank(message = "Surname cannot be empty")
        @Size(max = 30, message = "Surname cannot exceed 30 characters")
        String surname,

        @NotBlank(message = "Phone number cannot be empty")
        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        //@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
        String phone,

        @Size(max = 150, message = "Address cannot exceed 150 characters")
        String address
) {}