package com.selvam.userservice.service;

import com.selvam.userservice.dto.request.UserRequest;
import com.selvam.userservice.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(UserRequest userRequest);

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserRequest userRequest);

    void deleteUser(Long id);
}
