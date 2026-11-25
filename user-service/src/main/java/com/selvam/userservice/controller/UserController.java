package com.selvam.userservice.controller;

import com.selvam.userservice.dto.ApiResponseDto;
import com.selvam.userservice.dto.request.UserRequest;
import com.selvam.userservice.dto.response.UserResponse;
import com.selvam.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<UserResponse>> createUser(@RequestBody UserRequest request) {
        UserResponse createdUser = userService.createUser(request);
        ApiResponseDto<UserResponse> body = new ApiResponseDto<>(
                "User created successfully",
                createdUser,
                HttpStatus.CREATED
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("User retrieved successfully",
                userResponse));

    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponse>> updateUser(@PathVariable Long id,
                                                     @RequestBody UserRequest request) {
        UserResponse userResponse = userService.updateUser(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("User updated successfully", userResponse, HttpStatus.OK));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(@PathVariable Long id) {
        {
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponseDto<>("User deleted successfully", null, HttpStatus.NO_CONTENT));

        }
    }
}



