package com.carddemo.user.service;

import com.carddemo.common.dto.CreateUserRequest;
import com.carddemo.common.dto.UpdateUserRequest;
import com.carddemo.common.dto.UserDto;
import com.carddemo.common.exception.UserAlreadyExistsException;
import com.carddemo.common.exception.UserNotFoundException;
import com.carddemo.user.entity.User;
import com.carddemo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.info("Fetching all users, page: {}", pageable.getPageNumber());
        return userRepository.findAll(pageable)
                .map(this::toUserDto);
    }

    public UserDto getUserById(String userId) {
        log.info("Fetching user by ID: {}", userId);
        User user = userRepository.findByUserId(userId.toUpperCase())
                .orElseThrow(() -> new UserNotFoundException(userId));
        return toUserDto(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating new user: {}", request.getUserId());

        String userId = request.getUserId().toUpperCase();
        if (userRepository.existsByUserId(userId)) {
            throw new UserAlreadyExistsException(userId);
        }

        User user = User.builder()
                .userId(userId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(request.getPassword().toUpperCase())
                .userType(request.getUserType().toUpperCase())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User {} created successfully", userId);
        return toUserDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(String userId, UpdateUserRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findByUserId(userId.toUpperCase())
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPassword() != null) {
            user.setPassword(request.getPassword().toUpperCase());
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType().toUpperCase());
        }

        User updatedUser = userRepository.save(user);
        log.info("User {} updated successfully", userId);
        return toUserDto(updatedUser);
    }

    @Transactional
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findByUserId(userId.toUpperCase())
                .orElseThrow(() -> new UserNotFoundException(userId));

        userRepository.delete(user);
        log.info("User {} deleted successfully", userId);
    }

    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        log.info("Searching users with term: {}", searchTerm);
        return userRepository.findByUserIdContainingIgnoreCase(searchTerm, pageable)
                .map(this::toUserDto);
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .build();
    }
}
