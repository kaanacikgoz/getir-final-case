package com.acikgozkaan.user_service.integration;

import com.acikgozkaan.user_service.dto.request.auth.LoginRequest;
import com.acikgozkaan.user_service.dto.request.auth.RegisterRequest;
import com.acikgozkaan.user_service.dto.request.user.UpdateUserRequest;
import com.fasterxml.jackson.databind.JsonNode;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String ADMIN_EMAIL = "admin@getir.com";
    private static final String PASSWORD = "123456";
    private static final String BASE_URL = "/api/v1/users";
    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";

    private RegisterRequest buildUniqueRequest() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return new RegisterRequest(
                "user_" + uuid + "@getir.com",
                PASSWORD,
                "Name", "Surname",
                "5" + uuid.replaceAll("[^0-9]", "1"),
                "Test Address");
    }

    private String registerAndGetToken(RegisterRequest request) throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        return obtainToken(request.email(), request.password());
    }

    private String obtainToken(String email, String password) throws Exception {
        var login = new LoginRequest(email, password);
        var response = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private UUID getLastUserId(String token) throws Exception {
        var response = mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        JsonNode users = objectMapper.readTree(response);
        return UUID.fromString(users.get(users.size() - 1).get("id").asText());
    }

    @Test
    @DisplayName("Librarian should list all users")
    void librarianCanListUsers() throws Exception {
        String token = obtainToken(ADMIN_EMAIL, PASSWORD);

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$", not(empty()))
                );
    }

    @Test
    @DisplayName("Librarian should get user by ID")
    void librarianCanGetUserById() throws Exception {
        String token = obtainToken(ADMIN_EMAIL, PASSWORD);
        RegisterRequest request = buildUniqueRequest();
        registerAndGetToken(request);
        UUID userId = getLastUserId(token);

        mockMvc.perform(get(BASE_URL + "/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.email", is(request.email()))
                );
    }

    @Test
    @DisplayName("Librarian should update user")
    void librarianCanUpdateUser() throws Exception {
        String token = obtainToken(ADMIN_EMAIL, PASSWORD);
        RegisterRequest request = buildUniqueRequest();
        registerAndGetToken(request);
        UUID userId = getLastUserId(token);

        UpdateUserRequest update = new UpdateUserRequest(
                request.email(), "newpass", "Updated", "Surname", request.phone(), "New Address");

        mockMvc.perform(put(BASE_URL + "/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.email").value(update.email()),
                        jsonPath("$.name").value(update.name())
                );
    }

    @Test
    @DisplayName("Librarian should delete user")
    void librarianCanDeleteUser() throws Exception {
        String token = obtainToken(ADMIN_EMAIL, PASSWORD);
        RegisterRequest request = buildUniqueRequest();
        registerAndGetToken(request);
        UUID userId = getLastUserId(token);

        mockMvc.perform(delete(BASE_URL + "/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Patron should check user existence")
    void patronCanCheckUserExistence() throws Exception {
        RegisterRequest request = buildUniqueRequest();
        String patronToken = registerAndGetToken(request);

        String librarianToken = obtainToken(ADMIN_EMAIL, PASSWORD);
        UUID userId = getLastUserId(librarianToken);

        mockMvc.perform(get(BASE_URL + "/" + userId + "/check")
                        .header("Authorization", "Bearer " + patronToken))
                .andExpect(status().isOk());
    }
}