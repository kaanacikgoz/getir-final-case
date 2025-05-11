package com.acikgozkaan.user_service.unit.service;

import com.acikgozkaan.user_service.dto.request.auth.LoginRequest;
import com.acikgozkaan.user_service.dto.request.auth.RegisterRequest;
import com.acikgozkaan.user_service.dto.response.auth.LoginResponse;
import com.acikgozkaan.user_service.dto.response.auth.RegisterResponse;
import com.acikgozkaan.user_service.entity.Role;
import com.acikgozkaan.user_service.entity.User;
import com.acikgozkaan.user_service.exception.InvalidCredentialsException;
import com.acikgozkaan.user_service.exception.ValidationException;
import com.acikgozkaan.user_service.mapper.UserMapper;
import com.acikgozkaan.user_service.repository.UserRepository;
import com.acikgozkaan.user_service.security.jwt.JwtService;
import com.acikgozkaan.user_service.security.user.CustomUserDetails;
import com.acikgozkaan.user_service.service.auth.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String EMAIL = "test@getir.com";
    private static final String PASSWORD = "123456";
    private static final String PHONE = "123123123";
    private static final String NAME = "Name";
    private static final String SURNAME = "Surname";
    private static final String ADDRESS = "Address";

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthServiceImpl authService;

    private RegisterRequest createRegisterRequest() {
        return new RegisterRequest(EMAIL, PASSWORD, NAME, SURNAME, PHONE, ADDRESS);
    }

    @Test
    @DisplayName("Should register patron successfully")
    void shouldRegisterPatronSuccessfully() {
        RegisterRequest request = createRegisterRequest();
        User user = new User();

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByPhone(PHONE)).thenReturn(false);
        when(userMapper.toUser(request, Role.PATRON)).thenReturn(user);

        RegisterResponse response = authService.registerWithRole(request, Role.PATRON);

        assertThat(response.message()).isEqualTo("User successfully registered as PATRON");
        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository).existsByPhone(PHONE);
        verify(userMapper).toUser(request, Role.PATRON);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should register librarian successfully")
    void shouldRegisterLibrarianSuccessfully() {
        RegisterRequest request = createRegisterRequest();
        User user = new User();

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByPhone(PHONE)).thenReturn(false);
        when(userMapper.toUser(request, Role.LIBRARIAN)).thenReturn(user);

        RegisterResponse response = authService.registerWithRole(request, Role.LIBRARIAN);

        assertThat(response.message()).isEqualTo("User successfully registered as LIBRARIAN");
        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository).existsByPhone(PHONE);
        verify(userMapper).toUser(request, Role.LIBRARIAN);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw validation exception when email exists")
    void shouldThrowValidationExceptionWhenEmailExists() {
        RegisterRequest request = createRegisterRequest();
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> authService.registerWithRole(request, Role.PATRON));

        assertThat(exception.getFieldErrors()).containsKey("email");
        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository).existsByPhone(PHONE);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw validation exception when phone exists")
    void shouldThrowValidationExceptionWhenPhoneExists() {
        RegisterRequest request = createRegisterRequest();

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByPhone(PHONE)).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> authService.registerWithRole(request, Role.PATRON));

        assertThat(exception.getFieldErrors()).containsKey("phone");
        verify(userRepository).existsByEmail(EMAIL);
        verify(userRepository).existsByPhone(PHONE);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return token on valid login")
    void shouldReturnTokenOnValidLogin() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email(EMAIL)
                .password(PASSWORD)
                .name(NAME)
                .surname(SURNAME)
                .phone(PHONE)
                .address(ADDRESS)
                .role(Role.PATRON)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = mock(Authentication.class);

        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(userId.toString(), Role.PATRON.name())).thenReturn("mockJwt");

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("mockJwt");
        verify(authenticationManager).authenticate(any());
        verify(jwtService).generateToken(userId.toString(), Role.PATRON.name());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException on bad login")
    void shouldThrowInvalidCredentialsOnBadLogin() {
        LoginRequest request = new LoginRequest(EMAIL, "wrongPass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
        verify(authenticationManager).authenticate(any());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when user not found")
    void shouldThrowInvalidCredentialsWhenUserNotFound() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        when(authenticationManager.authenticate(any()))
                .thenThrow(new UsernameNotFoundException("Not found"));

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
        verify(authenticationManager).authenticate(any());
    }

}