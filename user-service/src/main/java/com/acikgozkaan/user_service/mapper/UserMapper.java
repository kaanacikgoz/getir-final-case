package com.acikgozkaan.user_service.mapper;

import com.acikgozkaan.user_service.dto.request.auth.RegisterRequest;
import com.acikgozkaan.user_service.dto.request.user.UpdateUserRequest;
import com.acikgozkaan.user_service.dto.response.user.UserResponse;
import com.acikgozkaan.user_service.entity.Role;
import com.acikgozkaan.user_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User toUser(RegisterRequest request, Role role) {
        return User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .name(request.name())
                .surname(request.surname())
                .phone(request.phone())
                .address(request.address())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getPhone(),
                user.getAddress()
        );
    }

    public void updateUserFromRequest(User user, UpdateUserRequest request) {
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setPhone(request.phone());
        user.setAddress(request.address());
    }

}