package com.acikgozkaan.book_service.dto;

import java.util.UUID;

public record BookStockEvent(UUID bookId, String title, int stock) {}