package com.acikgozkaan.user_service.dto.response.user;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        String surname,
        String phone,
        String address
) {}