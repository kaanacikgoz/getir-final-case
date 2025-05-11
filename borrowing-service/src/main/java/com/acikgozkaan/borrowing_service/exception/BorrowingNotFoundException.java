package com.acikgozkaan.borrowing_service.exception;

import java.util.UUID;

public class BorrowingNotFoundException extends RuntimeException{
    public BorrowingNotFoundException(UUID id) {
        super("Borrowing record not found with ID: " + id);
    }
}