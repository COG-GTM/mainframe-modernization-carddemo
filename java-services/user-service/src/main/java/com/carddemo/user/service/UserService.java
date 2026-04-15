package com.carddemo.user.service;

import com.carddemo.user.dto.UserCreateRequest;
import com.carddemo.user.dto.UserDto;
import com.carddemo.user.dto.UserListResponse;
import com.carddemo.user.dto.UserUpdateRequest;
import com.carddemo.user.exception.DuplicateResourceException;
import com.carddemo.user.exception.ResourceNotFoundException;
import com.carddemo.user.model.User;
import com.carddemo.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Service layer implementing the user administration business logic
 * migrated from COBOL programs COUSR00C through COUSR03C.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * List all users with pagination.
     * Migrated from COUSR00C which reads USRSEC file sequentially
     * and displays 10 users per page with PF7/PF8 navigation.
     */
    public UserListResponse listUsers(int page, int size) {
        Page<User> userPage = userRepository.findAll(
                PageRequest.of(page, size, Sort.by("userId").ascending()));

        return new UserListResponse(
                userPage.getContent().stream()
                        .map(this::toDto)
                        .toList(),
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements()
        );
    }

    /**
     * Get a single user by ID.
     * Migrated from the READ operation in COUSR02C/COUSR03C
     * which reads USRSEC by RIDFLD (SEC-USR-ID).
     */
    public UserDto getUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User ID NOT found: " + userId));
        return toDto(user);
    }

    /**
     * Create a new user.
     * Migrated from COUSR01C which validates input fields and
     * writes to USRSEC file. Handles DFHRESP(DUPREC) for duplicates.
     * Converts userId to uppercase per COBOL convention.
     */
    public UserDto createUser(UserCreateRequest request) {
        String upperUserId = request.getUserId().toUpperCase();

        if (userRepository.existsById(upperUserId)) {
            throw new DuplicateResourceException(
                    "User ID already exists: " + upperUserId);
        }

        User user = new User();
        user.setUserId(upperUserId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(request.getPassword());
        user.setUserType(request.getUserType().toUpperCase());

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    /**
     * Update an existing user.
     * Migrated from COUSR02C which reads the user for update,
     * compares each field for modifications, and rewrites the record.
     * Only updates fields that are provided (non-null).
     */
    public UserDto updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User ID NOT found: " + userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPassword() != null) {
            user.setPassword(request.getPassword());
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType().toUpperCase());
        }

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    /**
     * Delete a user by ID.
     * Migrated from COUSR03C which reads the user for update
     * and then deletes from USRSEC file.
     */
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User ID NOT found: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserType()
        );
    }
}
