package com.acikgozkaan.user_service.integration.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final String LIBRARIAN_EMAIL = "admin@getir.com";
    private static final String LIBRARIAN_PASSWORD = "123456";

    private String obtainToken(String email, String password) throws Exception {
        var login = new LoginRequest(email, password);
        var json = objectMapper.writeValueAsString(login);

        var response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("token").asText();
    }

    private String createPatronAndGetToken(String email, String phone) throws Exception {
        var request = new RegisterRequest(email, "123456", "Name", "Surname", phone, "Address");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        return obtainToken(email, "123456");
    }

    @Test
    @DisplayName("Librarian should get user by id")
    void shouldGetUserByIdAsLibrarian() throws Exception {
        String token = obtainToken(LIBRARIAN_EMAIL, LIBRARIAN_PASSWORD);
        UUID dummyUserId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/users/" + dummyUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Request without token should be forbidden")
    void shouldReturnForbiddenWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Patron should not access librarian-only endpoint")
    void shouldReturnForbiddenForPatronAccess() throws Exception {
        String token = createPatronAndGetToken("patron@getir.com", "111111111");

        mockMvc.perform(get("/api/v1/users/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Patron should access check endpoint")
    void shouldCheckUserExistenceAsPatron() throws Exception {
        String token = createPatronAndGetToken("checkpatron@getir.com", "222222222");

        mockMvc.perform(get("/api/v1/users/" + UUID.randomUUID() + "/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Librarian should perform all user operations successfully")
    void shouldPerformAllUserEndpointsSuccessfully() throws Exception {
        String token = obtainToken(LIBRARIAN_EMAIL, LIBRARIAN_PASSWORD);

        // 1. Create a new user
        String email = "user_" + UUID.randomUUID() + "@getir.com";
        String phone = "555" + UUID.randomUUID().toString().substring(0, 7);
        RegisterRequest register = new RegisterRequest(email, "123456", "Ali", "Veli", phone, "Test Street");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        // 2. Get all users and extract the last user's ID
        String allUsersJson = mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var users = objectMapper.readTree(allUsersJson);
        var createdUser = users.get(users.size() - 1);
        UUID createdUserId = UUID.fromString(createdUser.get("id").asText());

        // 3. Get user by ID
        mockMvc.perform(get("/api/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        // 4. Update user
        var update = new com.acikgozkaan.user_service.dto.request.user.UpdateUserRequest(
                email, "newpass", "Updated", "User", phone, "New Address"
        );
        mockMvc.perform(put("/api/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNoContent());

        // 5. Delete user
        mockMvc.perform(delete("/api/v1/users/" + createdUserId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Patron should successfully check existing user")
    void shouldReturnOkWhenUserExists() throws Exception {
        // Patron register
        String uniqueEmail = "check_" + UUID.randomUUID() + "@getir.com";
        String uniquePhone = "777" + UUID.randomUUID().toString().substring(0, 7);

        var register = new RegisterRequest(uniqueEmail, "123456", "Name", "Surname", uniquePhone, "Check Street");

        var response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Login and get token
        String token = obtainToken(uniqueEmail, "123456");

        // Get user ID from user list
        var usersJson = mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + obtainToken(LIBRARIAN_EMAIL, LIBRARIAN_PASSWORD)))
                .andReturn().getResponse().getContentAsString();

        var users = objectMapper.readTree(usersJson);
        var createdUser = users.findValuesAsText("email").contains(uniqueEmail)
                ? users.findValue("id")
                : users.get(users.size() - 1).get("id"); // fallback

        // Patron checks existence
        mockMvc.perform(get("/api/v1/users/" + createdUser.asText() + "/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when user does not exist")
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        String token = createPatronAndGetToken(
                "check_notfound_" + UUID.randomUUID() + "@getir.com",
                "888" + UUID.randomUUID().toString().substring(0, 7)
        );

        UUID nonExistingId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/users/" + nonExistingId + "/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

}