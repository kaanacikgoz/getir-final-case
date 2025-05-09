package com.acikgozkaan.user_service.service.user;

import com.acikgozkaan.user_service.dto.request.user.UpdateUserRequest;
import com.acikgozkaan.user_service.dto.response.user.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getUserById(UUID id);
    List<UserResponse> getAllUsers();
    void updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
}