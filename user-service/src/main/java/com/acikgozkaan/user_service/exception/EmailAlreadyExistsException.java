package com.acikgozkaan.user_service.exception;

import lombok.Getter;

@Getter
public class EmailAlreadyExistsException extends RuntimeException {
    private final String field = "email";

    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }

}