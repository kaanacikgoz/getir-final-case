package com.acikgozkaan.user_service.unit.mapper;

import com.acikgozkaan.user_service.dto.request.auth.RegisterRequest;
import com.acikgozkaan.user_service.dto.request.user.UpdateUserRequest;
import com.acikgozkaan.user_service.dto.response.user.UserResponse;
import com.acikgozkaan.user_service.entity.Role;
import com.acikgozkaan.user_service.entity.User;
import com.acikgozkaan.user_service.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private static final String EMAIL = "test@getir.com";
    private static final String PASSWORD = "123456";
    private static final String ENCODED_PASSWORD = "encodedPass";
    private static final String NAME = "John";
    private static final String SURNAME = "Doe";
    private static final String PHONE = "1234567890";
    private static final String ADDRESS = "Some Street, City";

    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userMapper = new UserMapper(passwordEncoder);
    }

    @Test
    @DisplayName("Should map RegisterRequest to User")
    void shouldMapRegisterRequestToUser() {
        RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD, NAME, SURNAME, PHONE, ADDRESS);
        Mockito.when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

        User user = userMapper.toUser(request, Role.PATRON);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(user.getRole()).isEqualTo(Role.PATRON);
        assertThat(user.getName()).isEqualTo(NAME);
        assertThat(user.getSurname()).isEqualTo(SURNAME);
        assertThat(user.getPhone()).isEqualTo(PHONE);
        assertThat(user.getAddress()).isEqualTo(ADDRESS);
    }

    @Test
    @DisplayName("Should map User to UserResponse")
    void shouldMapUserToUserResponse() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(EMAIL)
                .name(NAME)
                .surname(SURNAME)
                .phone(PHONE)
                .address(ADDRESS)
                .role(Role.PATRON)
                .build();

        UserResponse response = userMapper.toUserResponse(user);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo(EMAIL);
        assertThat(response.name()).isEqualTo(NAME);
        assertThat(response.surname()).isEqualTo(SURNAME);
        assertThat(response.phone()).isEqualTo(PHONE);
        assertThat(response.address()).isEqualTo(ADDRESS);
    }

    @Test
    @DisplayName("Should update User fields from UpdateUserRequest")
    void shouldUpdateUserFromUpdateRequest() {
        User user = User.builder()
                .email("old@getir.com")
                .password("oldPass")
                .name("OldName")
                .surname("OldSurname")
                .phone("0000000000")
                .address("Old Address")
                .build();

        UpdateUserRequest updateRequest = new UpdateUserRequest(
                EMAIL,
                PASSWORD,
                NAME,
                SURNAME,
                PHONE,
                ADDRESS
        );

        Mockito.when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

        userMapper.updateUserFromRequest(user, updateRequest);

        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(user.getName()).isEqualTo(NAME);
        assertThat(user.getSurname()).isEqualTo(SURNAME);
        assertThat(user.getPhone()).isEqualTo(PHONE);
        assertThat(user.getAddress()).isEqualTo(ADDRESS);
    }

}