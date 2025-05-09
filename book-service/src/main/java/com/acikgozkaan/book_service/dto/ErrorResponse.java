package com.acikgozkaan.book_service.dto;

import java.util.Map;

public record ErrorResponse(
        String message,
        int statusCode,
        Map<String, String> fieldErrors
) {}