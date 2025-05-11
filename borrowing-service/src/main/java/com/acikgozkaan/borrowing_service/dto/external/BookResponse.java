package com.acikgozkaan.borrowing_service.dto.external;

import java.util.UUID;

public record BookResponse(
        UUID id,
        String title,
        int stock
) {}