package com.acikgozkaan.book_service.exception;

public class IsbnAlreadyExistsException extends RuntimeException {
    public IsbnAlreadyExistsException(String isbn) {
        super("ISBN already exists: " + isbn);
    }
}