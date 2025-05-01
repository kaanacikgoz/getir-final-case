package com.acikgozkaan.auth_service.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank
        @Size(max = 30)
        String name,

        @NotBlank
        @Size(max = 30)
        String surname,

        @NotBlank
        @Email
        @Size(max = 75)
        String email,

        @NotBlank
        @Size(min = 6, max = 48, message = "Password must be between 6 and 48 characters")
        String password,

        @NotBlank
        //@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be valid")
        String phone,

        @Size(max = 150)
        String address
) {}