package com.acikgozkaan.borrowing_service.dto.response;

import java.util.Map;

public record ErrorResponse(
        String message,
        int statusCode,
        Map<String, String> fieldErrors
) {}