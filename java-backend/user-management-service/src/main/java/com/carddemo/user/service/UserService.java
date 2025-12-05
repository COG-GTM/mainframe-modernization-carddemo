package com.carddemo.user.service;

import com.carddemo.common.dto.PagedResponse;
import com.carddemo.common.exception.DuplicateResourceException;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import com.carddemo.user.dto.*;
import com.carddemo.user.model.User;
import com.carddemo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User management service implementing CRUD operations.
 * 
 * Replaces COBOL programs:
 * 
 * COUSR00C.cbl - List users:
 *   - PROCESS-PAGE-FORWARD: Reads next 10 records
 *   - PROCESS-PAGE-BACKWARD: Reads previous 10 records
 *   - POPULATE-USER-DATA: Populates screen fields
 * 
 * COUSR01C.cbl - Add user:
 *   - WRITE-USER-SEC-FILE: Writes new record to USRSEC
 *   - Handles DUPKEY/DUPREC response codes
 * 
 * COUSR02C.cbl - Update user:
 *   - READ-USER-SEC-FILE: Reads record for update
 *   - UPDATE-USER-SEC-FILE: Rewrites modified record
 * 
 * COUSR03C.cbl - Delete user:
 *   - READ-USER-SEC-FILE: Reads record for deletion
 *   - DELETE-USER-SEC-FILE: Deletes record from USRSEC
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public PagedResponse<UserListItem> listUsers(int page, int size, String search) {
        log.info("Listing users - page: {}, size: {}, search: {}", page, size, search);

        Pageable pageable = PageRequest.of(page, size, Sort.by("userId").ascending());
        Page<User> userPage;

        if (search != null && !search.trim().isEmpty()) {
            userPage = userRepository.searchUsers(search.trim(), pageable);
        } else {
            userPage = userRepository.findAllByIsActiveTrue(pageable);
        }

        List<UserListItem> items = userPage.getContent().stream()
                .map(this::toUserListItem)
                .collect(Collectors.toList());

        return PagedResponse.of(items, page, size, userPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(String userId) {
        log.info("Getting user by ID: {}", userId);

        User user = userRepository.findByUserIdAndIsActiveTrue(userId.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new ResourceNotFoundException("User", userId);
                });

        return toUserDto(user);
    }

    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        log.info("Creating user: {}", request.getUserId());

        String userId = request.getUserId().toUpperCase();

        if (userRepository.existsByUserId(userId)) {
            log.warn("User ID already exists: {}", userId);
            throw new DuplicateResourceException("User", userId);
        }

        User user = User.builder()
                .userId(userId)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .userType(request.getUserType().toUpperCase())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("User {} has been added", userId);

        return toUserDto(user);
    }

    @Transactional
    public UserDto updateUser(String userId, UpdateUserRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findByUserIdAndIsActiveTrue(userId.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("User not found for update: {}", userId);
                    return new ResourceNotFoundException("User", userId);
                });

        boolean modified = false;

        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            if (!request.getFirstName().trim().equals(user.getFirstName())) {
                user.setFirstName(request.getFirstName().trim());
                modified = true;
            }
        }

        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            if (!request.getLastName().trim().equals(user.getLastName())) {
                user.setLastName(request.getLastName().trim());
                modified = true;
            }
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            modified = true;
        }

        if (request.getUserType() != null && !request.getUserType().isEmpty()) {
            String newType = request.getUserType().toUpperCase();
            if (!newType.equals(user.getUserType())) {
                user.setUserType(newType);
                modified = true;
            }
        }

        if (!modified) {
            log.info("No changes detected for user: {}", userId);
            throw new ValidationException("Please modify to update");
        }

        user = userRepository.save(user);
        log.info("User {} has been updated", userId);

        return toUserDto(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findByUserIdAndIsActiveTrue(userId.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("User not found for deletion: {}", userId);
                    return new ResourceNotFoundException("User", userId);
                });

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User {} has been deleted", userId);
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    private UserListItem toUserListItem(User user) {
        return UserListItem.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .userType(user.getUserType())
                .isActive(user.getIsActive())
                .build();
    }
}
