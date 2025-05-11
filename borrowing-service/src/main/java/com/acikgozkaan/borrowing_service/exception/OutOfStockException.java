package com.acikgozkaan.borrowing_service.exception;

import java.util.UUID;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(UUID bookId) {
        super("Book with ID " + bookId + " is out of stock.");
    }
}