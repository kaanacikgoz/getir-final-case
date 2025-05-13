package com.acikgozkaan.borrowing_service.exception;

import com.acikgozkaan.borrowing_service.dto.response.ErrorResponse;
import feign.FeignException;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BorrowingNotFoundException.class,
            BusinessRuleException.class,
            OutOfStockException.class
    })
    public ResponseEntity<ErrorResponse> handleCustomRuntimeExceptions(RuntimeException ex) {
        HttpStatus status = resolveStatus(ex);
        log.warn("Handled custom exception: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), status);
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
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid value")
                ));

        log.warn("Validation failed: {}", errors);
        return buildResponse("Validation failed", HttpStatus.BAD_REQUEST, errors);
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

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeign(FeignException ex) {
        log.error("Unhandled exception occurred", ex);
        return buildResponse("Internal server error: " + ex.getMessage(), HttpStatus.valueOf(ex.status()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        String message = Optional.ofNullable(ex.getReason())
                .orElse("An error occurred");
        log.warn("ResponseStatusException: {}", message);
        return buildResponse(message, HttpStatus.valueOf(ex.getStatusCode().value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        return buildResponse("Internal server error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        return buildResponse(message, status, Map.of());    }

    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status, Map<String, String> fieldErrors) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(message, status.value(), fieldErrors));    }

    private HttpStatus resolveStatus(RuntimeException ex) {
        if (ex instanceof BorrowingNotFoundException) return HttpStatus.NOT_FOUND;
        if (ex instanceof BusinessRuleException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof OutOfStockException) return HttpStatus.CONFLICT;
        return HttpStatus.BAD_REQUEST;
    }
}