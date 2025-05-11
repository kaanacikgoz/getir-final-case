package com.acikgozkaan.user_service.unit.service;

import com.acikgozkaan.user_service.dto.request.user.UpdateUserRequest;
import com.acikgozkaan.user_service.dto.response.user.UserResponse;
import com.acikgozkaan.user_service.entity.User;
import com.acikgozkaan.user_service.exception.EmailAlreadyExistsException;
import com.acikgozkaan.user_service.exception.PhoneAlreadyExistsException;
import com.acikgozkaan.user_service.exception.UserNotFoundException;
import com.acikgozkaan.user_service.mapper.UserMapper;
import com.acikgozkaan.user_service.repository.UserRepository;
import com.acikgozkaan.user_service.service.user.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UUID userId;
    private User user;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@getir.com")
                .password("encoded-pass")
                .name("Name")
                .surname("Surname")
                .phone("123456")
                .address("Address")
                .build();

        updateRequest = new UpdateUserRequest(
                "updated@getir.com", "newPass", "New", "Surname", "111111", "New Address"
        );
    }

    @Test
    @DisplayName("Should get user by id successfully")
    void shouldGetUserById() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(new UserResponse(userId, user.getEmail(), user.getName(), user.getSurname(), user.getPhone(), user.getAddress()));

        UserResponse response = userService.getUserById(userId);

        assertThat(response).isNotNull();
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw when user not found by id")
    void shouldThrowWhenUserNotFoundById() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(new UserResponse(userId, user.getEmail(), user.getName(), user.getSurname(), user.getPhone(), user.getAddress()));

        List<UserResponse> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        when(userRepository.existsByEmailAndIdNot(updateRequest.email(), userId)).thenReturn(false);
        when(userRepository.existsByPhoneAndIdNot(updateRequest.phone(), userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(updateRequest.password())).thenReturn("encoded");

        userService.updateUser(userId, updateRequest);

        assertThat(user.getEmail()).isEqualTo(updateRequest.email());
        assertThat(user.getPassword()).isEqualTo("encoded");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException on update")
    void shouldThrowEmailExistsOnUpdate() {
        when(userRepository.existsByEmailAndIdNot(updateRequest.email(), userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should throw PhoneAlreadyExistsException on update")
    void shouldThrowPhoneExistsOnUpdate() {
        when(userRepository.existsByEmailAndIdNot(updateRequest.email(), userId)).thenReturn(false);
        when(userRepository.existsByPhoneAndIdNot(updateRequest.phone(), userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(PhoneAlreadyExistsException.class);
    }

    @Test
    @DisplayName("Should delete user if exists")
    void shouldDeleteUserIfExists() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException on delete")
    void shouldThrowOnDeleteWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should pass checkExistenceById")
    void shouldPassCheckExistence() {
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.checkExistenceById(userId);

        verify(userRepository).existsById(userId);
    }

    @Test
    @DisplayName("Should throw on checkExistenceById if user not found")
    void shouldFailCheckExistence() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.checkExistenceById(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}