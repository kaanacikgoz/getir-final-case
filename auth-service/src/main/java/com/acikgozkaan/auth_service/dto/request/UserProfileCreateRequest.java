package com.acikgozkaan.auth_service.dto.request;

import java.util.UUID;

public record UserProfileCreateRequest(
        UUID id,
        String name,
        String surname,
        String phone,
        String address
) {}