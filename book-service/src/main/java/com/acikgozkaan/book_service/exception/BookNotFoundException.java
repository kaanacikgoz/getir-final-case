package com.acikgozkaan.book_service.exception;

import java.util.UUID;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(UUID id) {
        super("Book not found with ID: " + id);
    }
}