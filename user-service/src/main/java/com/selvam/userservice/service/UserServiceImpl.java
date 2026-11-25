package com.selvam.userservice.service;

import com.selvam.userservice.dto.request.UserRequest;
import com.selvam.userservice.dto.response.UserResponse;
import com.selvam.userservice.entity.User;
import com.selvam.userservice.exceptions.ResourceNotFoundException;
import com.selvam.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
//        log.info("Creating User: {}", userRequest);

        User createdUser = User.builder()
                .name(userRequest.name())
                .surname(userRequest.surname())
                .email(userRequest.email())
                .address(userRequest.address())
                .alerting(userRequest.alerting())
                .energyAlertingThreshold(userRequest.energyAlertingThreshold())
                .build();
        final User savedUser = userRepository.save(createdUser);
        return userResponse(savedUser);

    }

    @Override
    public UserResponse getUserById(Long id) {
//        log.info("Getting user by id: {}", id);
        return userRepository.findById(id)
                .map(this::userResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }


    @Override
    public UserResponse updateUser(Long id, UserRequest userRequest) {
//        log.info("Updating user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setName(userRequest.name());
        user.setSurname(userRequest.surname());
        user.setEmail(userRequest.email());
        user.setAddress(userRequest.address());
        user.setAlerting(userRequest.alerting());
        user.setEnergyAlertingThreshold(userRequest.energyAlertingThreshold());
        User updatedUser = userRepository.save(user);
        return userResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
//        log.info("Deleting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    private UserResponse userResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .address(user.getAddress())
                .alerting(user.isAlerting())
                .energyAlertingThreshold(user.getEnergyAlertingThreshold())
                .build();
    }
}
