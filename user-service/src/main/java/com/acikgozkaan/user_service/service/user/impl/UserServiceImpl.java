package com.acikgozkaan.user_service.service.user.impl;

import com.acikgozkaan.user_service.dto.request.user.UpdateUserRequest;
import com.acikgozkaan.user_service.dto.response.user.UserResponse;
import com.acikgozkaan.user_service.entity.User;
import com.acikgozkaan.user_service.exception.EmailAlreadyExistsException;
import com.acikgozkaan.user_service.exception.PhoneAlreadyExistsException;
import com.acikgozkaan.user_service.exception.UserNotFoundException;
import com.acikgozkaan.user_service.mapper.UserMapper;
import com.acikgozkaan.user_service.repository.UserRepository;
import com.acikgozkaan.user_service.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getUserById(UUID id) {
        return userMapper.toUserResponse(findUserById(id));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        validateEmailAndPhoneUniqueness(id, request.email(), request.phone());

        User user = findUserById(id);
        userMapper.updateUserFromRequest(user, request);
        User updated = userRepository.save(user);

        return userMapper.toUserResponse(updated);
    }

    @Override
    public void deleteUser(UUID id) {
        findUserById(id);
        userRepository.deleteById(id);
    }

    @Override
    public void checkExistenceById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void validateEmailAndPhoneUniqueness(UUID userId, String email, String phone) {
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new EmailAlreadyExistsException(email);
        }

        if (userRepository.existsByPhoneAndIdNot(phone, userId)) {
            throw new PhoneAlreadyExistsException(phone);
        }
    }

}