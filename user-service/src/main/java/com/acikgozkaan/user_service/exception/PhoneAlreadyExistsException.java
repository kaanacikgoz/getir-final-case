package com.acikgozkaan.user_service.exception;

import lombok.Getter;

@Getter
public class PhoneAlreadyExistsException extends RuntimeException {
    private final String field;

    public PhoneAlreadyExistsException(String phone) {
        super("Phone already exists: " + phone);
        this.field = "phone";
    }

}