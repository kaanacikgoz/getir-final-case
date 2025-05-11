package com.acikgozkaan.user_service.integration;

import com.acikgozkaan.user_service.dto.request.auth.LoginRequest;
import com.acikgozkaan.user_service.dto.request.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String REGISTER_LIBRARIAN_URL = "/api/v1/auth/register-librarian";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String LIBRARIAN_EMAIL = "admin@getir.com";
    private static final String LIBRARIAN_PASSWORD = "123456";

    private RegisterRequest buildUniqueRequest() {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "user_" + uniqueId + "@getir.com";
        String phone = "999" + uniqueId.replaceAll("[^0-9]", "1");
        return new RegisterRequest(email, "123456", "Name", "Surname", phone, "Test Street");
    }

    private String obtainToken(String email, String password) throws Exception {
        LoginRequest login = new LoginRequest(email, password);
        String json = objectMapper.writeValueAsString(login);

        String response = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("Patron registration should succeed")
    void shouldRegisterPatronSuccessfully() throws Exception {
        RegisterRequest request = buildUniqueRequest();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message", is("User successfully registered as PATRON"))
                );
    }

    @Test
    @DisplayName("Librarian should register new librarian successfully")
    void shouldRegisterLibrarianSuccessfully() throws Exception {
        String token = obtainToken(LIBRARIAN_EMAIL, LIBRARIAN_PASSWORD);

        RegisterRequest request = buildUniqueRequest();

        mockMvc.perform(post(REGISTER_LIBRARIAN_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.message", is("User successfully registered as LIBRARIAN"))
                );
    }

    @Test
    @DisplayName("Valid login should return token")
    void shouldReturnTokenOnValidLogin() throws Exception {
        RegisterRequest request = buildUniqueRequest();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest(request.email(), "123456");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.token", not(emptyOrNullString()))
                );
    }

    @Test
    @DisplayName("Request without token should be forbidden for librarian registration")
    void shouldReturnForbiddenForLibrarianRegistrationWithoutToken() throws Exception {
        RegisterRequest request = buildUniqueRequest();

        mockMvc.perform(post(REGISTER_LIBRARIAN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Patron token should not allow librarian registration")
    void shouldReturnUnauthorizedForPatronRoleOnLibrarianRegistration() throws Exception {
        RegisterRequest patron = buildUniqueRequest();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patron)))
                .andExpect(status().isOk());

        String token = obtainToken(patron.email(), "123456");

        RegisterRequest attempt = buildUniqueRequest();

        mockMvc.perform(post(REGISTER_LIBRARIAN_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attempt)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Empty email on librarian registration should fail validation")
    void shouldFailValidationWhenEmailIsEmptyForLibrarian() throws Exception {
        String token = obtainToken(LIBRARIAN_EMAIL, LIBRARIAN_PASSWORD);

        RegisterRequest invalid = new RegisterRequest(
                "", "123456", "Name", "Surname", "12345678", "Street"
        );

        mockMvc.perform(post(REGISTER_LIBRARIAN_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.message", is("Validation failed")),
                        jsonPath("$.fieldErrors.email", notNullValue())
                );
    }

    @Test
    @DisplayName("Should fail registration when email already exists")
    void shouldFailWhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = buildUniqueRequest();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        RegisterRequest duplicate = new RegisterRequest(
                request.email(), "123456", "Name", "Surname", "2020202020", "Test Street"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.message").value("Validation failed"),
                        jsonPath("$.fieldErrors.email").value(containsString("already exists"))
                );
    }

    @Test
    @DisplayName("Should fail registration when phone already exists")
    void shouldFailWhenPhoneAlreadyExists() throws Exception {
        RegisterRequest request = buildUniqueRequest();

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        RegisterRequest duplicate = new RegisterRequest(
                "another_" + UUID.randomUUID() + "@getir.com",
                "123456", "Name", "Surname", request.phone(), "Test Street"
        );

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.message").value("Validation failed"),
                        jsonPath("$.fieldErrors.phone").value(containsString("already exists"))
                );
    }

    @Test
    @DisplayName("Should fail registration due to invalid email format")
    void shouldFailOnInvalidEmailFormat() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "invalid-email", "123456", "Name", "Surname", "6060606060", "Test Street");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    @DisplayName("Should fail registration due to short password")
    void shouldFailOnShortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "shortpass" + UUID.randomUUID() + "@getir.com", "123", "Name", "Surname", "7070707070", "Test Street");

        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.fieldErrors.password").exists());
    }
}