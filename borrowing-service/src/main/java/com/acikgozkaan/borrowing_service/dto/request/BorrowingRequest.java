package com.acikgozkaan.borrowing_service.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BorrowingRequest(

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Book ID is required")
        UUID bookId

) {}