package com.acikgozkaan.user_service.exception;

import com.acikgozkaan.user_service.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Email already exists: {}", ex.getMessage());
        Map<String, String> fieldError = Map.of(ex.getField(), ex.getMessage());
        return buildResponse("Validation failed", HttpStatus.CONFLICT, fieldError);
    }

    @ExceptionHandler(PhoneAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePhoneAlreadyExists(PhoneAlreadyExistsException ex) {
        log.warn("Phone already exists: {}", ex.getMessage());
        Map<String, String> fieldError = Map.of(ex.getField(), ex.getMessage());
        return buildResponse("Validation failed", HttpStatus.CONFLICT, fieldError);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.warn("Custom validation failed: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, ex.getFieldErrors());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestBody(HttpMessageNotReadableException ex) {
        log.warn("Missing or invalid request body: {}", ex.getMessage());
        return buildResponse("Request body is missing or invalid", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthorizationDeniedException ex) {
        log.warn("Authorization denied: {}", ex.getMessage());
        return buildResponse("You are not authorized to perform this action", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch: {}", ex.getMessage());

        if (ex.getRequiredType() != null && ex.getRequiredType().equals(UUID.class)) {
            Map<String, String> error = Map.of(ex.getName(), "Invalid UUID format: " + ex.getValue());
            return buildResponse("Validation failed", HttpStatus.BAD_REQUEST, error);
        }

        Map<String, String> error = Map.of(ex.getName(), "Invalid type: " + ex.getValue());
        return buildResponse("Type mismatch error", HttpStatus.BAD_REQUEST, error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Method argument validation failed: {}", errors);
        return buildResponse("Validation failed", HttpStatus.BAD_REQUEST, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception occurred: ", ex);
        return buildResponse("Internal Server Error: ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        return buildResponse(message, status, Map.of());
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status, Map<String, String> fieldErrors) {
        return new ResponseEntity<>(new ErrorResponse(message, status.value(), fieldErrors), status);
    }

}