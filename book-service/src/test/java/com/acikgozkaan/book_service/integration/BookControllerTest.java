package com.acikgozkaan.book_service.integration;

import com.acikgozkaan.book_service.dto.BookRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String LIBRARIAN_EMAIL = "admin@getir.com";
    private static final String PATRON_EMAIL = "patron@getir.com";
    private static final String LIBRARIAN = "LIBRARIAN";
    private static final String PATRON = "PATRON";
    private static final String API_V1_BOOKS = "/api/v1/books";

    @Test
    @DisplayName("Librarian should create a book successfully")
    @WithMockUser(username = LIBRARIAN_EMAIL, roles = {LIBRARIAN})
    void shouldCreateBookAsLibrarian() throws Exception {
        String isbn = UUID.randomUUID().toString().substring(0, 13);
        BookRequest request = new BookRequest(
                "Integration Test Book",
                "Test Author",
                isbn,
                2023,
                Genre.TECHNOLOGY,
                5
        );

        mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isCreated(),
                        jsonPath("$.title", is("Integration Test Book")),
                        jsonPath("$.author", is("Test Author")),
                        jsonPath("$.isbn", is(isbn)),
                        jsonPath("$.publicationYear", is(2023)),
                        jsonPath("$.genre", is("TECHNOLOGY")),
                        jsonPath("$.stock", is(5))
                );
    }

    @Test
    @DisplayName("Patron should not create a book")
    @WithMockUser(username = PATRON_EMAIL, roles = {PATRON})
    void shouldNotCreateBookAsPatron() throws Exception {
        BookRequest request = new BookRequest(
                "Integration Test Book",
                "Test Author",
                UUID.randomUUID().toString().substring(0, 13),
                2023,
                Genre.TECHNOLOGY,
                5
        );

        mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isUnauthorized(),
                        jsonPath("$.message", is("You are not authorized to perform this action")),
                        jsonPath("$.statusCode", is(401)),
                        jsonPath("$.fieldErrors", is(Map.of()))
                );
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldGetAllBooksAsLibrarian() throws Exception {
        mockMvc.perform(get(API_V1_BOOKS))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void shouldGetAllBooksAsPatron() throws Exception {
        mockMvc.perform(get(API_V1_BOOKS))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldGetBookById() throws Exception {
        MvcResult result = mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        UUID createdId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

        mockMvc.perform(get(API_V1_BOOKS + "/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId.toString()));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldUpdateBook() throws Exception {
        MvcResult result = mockMvc.perform(post(API_V1_BOOKS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        UUID bookId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        BookRequest updated = new BookRequest("Updated", "New Author", "1234567890123", 2020, Genre.HISTORY, 8);

        mockMvc.perform(put(API_V1_BOOKS + "/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void patronShouldNotUpdateBook() throws Exception {
        mockMvc.perform(put(API_V1_BOOKS + "/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void shouldDeleteBook() throws Exception {
        UUID bookId = UUID.fromString(objectMapper.readTree(
                mockMvc.perform(post(API_V1_BOOKS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(sampleBookRequest())))
                        .andReturn().getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(delete(API_V1_BOOKS + "/" + bookId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "PATRON")
    void shouldSearchBooks() throws Exception {
        mockMvc.perform(get(API_V1_BOOKS + "/search")
                        .param("title", "Test"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").exists()
                );
    }

    private BookRequest sampleBookRequest() {
        return new BookRequest(
                "Test Book",
                "Test Author",
                UUID.randomUUID().toString().substring(0, 13),
                2024,
                Genre.SCIENCE,
                4
        );
    }

}