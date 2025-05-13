package com.acikgozkaan.borrowing_service.unit;

import com.acikgozkaan.borrowing_service.dto.response.ErrorResponse;
import com.acikgozkaan.borrowing_service.exception.BorrowingNotFoundException;
import com.acikgozkaan.borrowing_service.exception.BusinessRuleException;
import com.acikgozkaan.borrowing_service.exception.GlobalExceptionHandler;
import com.acikgozkaan.borrowing_service.exception.OutOfStockException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleBorrowingNotFoundException() {
        UUID id = UUID.randomUUID();
        BorrowingNotFoundException ex = new BorrowingNotFoundException(id);

        ResponseEntity<ErrorResponse> response = handler.handleCustomRuntimeExceptions(ex);

        assertEquals(404, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).message().contains(id.toString()));
        assertEquals(404, response.getBody().statusCode());
        assertTrue(response.getBody().fieldErrors().isEmpty());
    }

    @Test
    void shouldHandleOutOfStockException() {
        UUID bookId = UUID.randomUUID();
        OutOfStockException ex = new OutOfStockException(bookId);

        ResponseEntity<ErrorResponse> response = handler.handleCustomRuntimeExceptions(ex);

        assertEquals(409, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).message().contains(bookId.toString()));
        assertEquals(409, response.getBody().statusCode());
    }

    @Test
    void shouldHandleBusinessRuleException() {
        String message = "Business rule violated";
        BusinessRuleException ex = new BusinessRuleException(message);

        ResponseEntity<ErrorResponse> response = handler.handleCustomRuntimeExceptions(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(message, Objects.requireNonNull(response.getBody()).message());
        assertEquals(400, response.getBody().statusCode());
    }

    @Test
    void shouldHandleGenericException() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex);

        assertEquals(500, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).message().contains("Unexpected error"));
        assertEquals(500, response.getBody().statusCode());
        assertTrue(response.getBody().fieldErrors().isEmpty());
    }

    @Test
    void shouldHandleValidationErrors() {
        FieldError fieldError = new FieldError("objectName", "fieldName", "Field is invalid");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Validation failed", Objects.requireNonNull(response.getBody()).message());
        assertEquals("Field is invalid", response.getBody().fieldErrors().get("fieldName"));
    }

    @Test
    void shouldHandleResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Not found");

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatus(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Not found", Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldHandleFeignExceptionWithValidJsonBody() {
        String json = """
        {
          "message": "User not found with ID: test-id",
          "statusCode": 404,
          "fieldErrors": {}
        }
        """;

        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(404);
        when(feignEx.contentUTF8()).thenReturn(json);

        ResponseEntity<ErrorResponse> response = handler.handleFeign(feignEx);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("User not found with ID: test-id", response.getBody().message());
        assertTrue(response.getBody().fieldErrors().isEmpty());
    }

    @Test
    void shouldHandleFeignExceptionWithInvalidJsonBody() {
        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(503);
        when(feignEx.contentUTF8()).thenReturn("not a json");

        ResponseEntity<ErrorResponse> response = handler.handleFeign(feignEx);

        assertEquals(503, response.getStatusCode().value());
        assertEquals("Service call failed", response.getBody().message());
    }

    @Test
    void shouldHandleMissingRequestBody() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Missing body");

        ResponseEntity<ErrorResponse> response = handler.handleMissingRequestBody(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Request body is missing or invalid", Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldHandleAuthorizationDeniedException() {
        AuthorizationDeniedException ex = new AuthorizationDeniedException("Access denied");

        ResponseEntity<ErrorResponse> response = handler.handleAuth(ex);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("You are not authorized to perform this action", Objects.requireNonNull(response.getBody()).message());
    }

}