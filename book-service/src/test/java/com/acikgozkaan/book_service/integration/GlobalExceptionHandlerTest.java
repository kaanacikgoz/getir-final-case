package com.acikgozkaan.book_service.integration;

import com.acikgozkaan.book_service.dto.request.BookRequest;
import com.acikgozkaan.book_service.entity.Genre;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String API_V1_BOOKS = "/api/v1/books";

    @Test
    @DisplayName("Should return 404 when book not found")
    @WithMockUser(roles = "LIBRARIAN")
    void shouldReturnNotFoundForMissingBook() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get(API_V1_BOOKS + "/" + randomId))
                .andExpect(status().isNotFound())
                .andExpectAll(
                        jsonPath("$.message").value("Book not found with ID: "+randomId),
                        jsonPath("$.statusCode").value(404));
    }

    @Test
    @DisplayName("Should return 409 when duplicate ISBN on create")
    @WithMockUser(roles = "LIBRARIAN")
    void shouldReturnConflictForDuplicateIsbn() throws Exception {
        String isbn = UUID.randomUUID().toString().substring(0, 13);

        BookRequest request = new BookRequest("Title", "Author", isbn, 2023, Genre.TECHNOLOGY, 5);

        mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("ISBN already exists")));
    }

    @Test
    @DisplayName("Should return 400 for invalid UUID format")
    @WithMockUser(roles = "LIBRARIAN")
    void shouldReturnBadRequestForInvalidUUID() throws Exception {
        mockMvc.perform(get(API_V1_BOOKS + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors.id", containsString("Invalid UUID format")));
    }

    @Test
    @DisplayName("Should return 400 for validation errors")
    @WithMockUser(roles = "LIBRARIAN")
    void shouldReturnBadRequestForInvalidRequestBody() throws Exception {
        BookRequest invalidRequest = new BookRequest("", "", "", 0, null, -1);

        mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.fieldErrors").isMap());
    }

    @Test
    @DisplayName("Should return 403 when forbidden access occurs")
    void shouldReturnUnauthorized() throws Exception {
        BookRequest request = new BookRequest("Title", "Author", "1234567890123", 2023, Genre.HISTORY, 3);

        mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 400 when request body is missing or malformed")
    @WithMockUser(roles = "LIBRARIAN")
    void shouldReturnBadRequestForMissingBody() throws Exception {
        mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required request body is missing or invalid"));
    }

    @Test
    @DisplayName("Should return 409 when book stock is zero")
    @WithMockUser(roles = {"PATRON", "LIBRARIAN"})
    void shouldReturnConflictWhenOutOfStock() throws Exception {
        BookRequest request = new BookRequest("ZeroStock", "Author", UUID.randomUUID().toString().substring(0,13), 2022, Genre.SCIENCE, 0);

        String body = mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID bookId = UUID.fromString(objectMapper.readTree(body).get("id").asText());

        mockMvc.perform(put(API_V1_BOOKS + "/" + bookId + "/decrease-stock"))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.message", is("Book with ID "+bookId+" is out of stock."))
                );
    }

    @Test
    @DisplayName("Should return 400 for type mismatch on integer param")
    @WithMockUser(roles = "LIBRARIAN")
    void shouldReturnBadRequestForNonUUIDTypeMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/books/search")
                        .param("genre", "wrong-type"))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message", containsString("Type mismatch error"))
                );
    }

}