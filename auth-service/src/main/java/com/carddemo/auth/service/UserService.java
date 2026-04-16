package com.carddemo.auth.service;

import com.carddemo.auth.dto.CreateUserRequest;
import com.carddemo.auth.dto.UpdateUserRequest;
import com.carddemo.auth.dto.UserDto;
import com.carddemo.auth.entity.UserEntity;
import com.carddemo.auth.exception.UserAlreadyExistsException;
import com.carddemo.auth.exception.UserNotFoundException;
import com.carddemo.auth.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User management service ported from COUSR00C-COUSR03C (User CRUD programs).
 *
 * COBOL programs:
 *   COUSR00C - User List with pagination (10/page)
 *   COUSR01C - User Add
 *   COUSR02C - User Update
 *   COUSR03C - User Delete
 *
 * All operations are admin-only (enforced at the controller/security layer).
 */
@Service
@Transactional
public class UserService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<UserDto> listUsers(int page, int size) {
        int pageSize = size > 0 ? size : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(page, pageSize);
        return userRepository.findAllByOrderByUserIdAsc(pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public UserDto getUser(String userId) {
        UserEntity entity = userRepository.findByUserIdIgnoreCase(userId.toUpperCase())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found. Try again ..."));
        return toDto(entity);
    }

    public UserDto createUser(CreateUserRequest request) {
        String normalizedId = request.userId().trim().toUpperCase();

        if (userRepository.findByUserIdIgnoreCase(normalizedId).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User ID '" + normalizedId + "' already exists");
        }

        UserEntity entity = new UserEntity(
                normalizedId,
                request.firstName().trim(),
                request.lastName().trim(),
                passwordEncoder.encode(request.password().trim().toUpperCase()),
                request.userType().toUpperCase()
        );

        UserEntity saved = userRepository.save(entity);
        return toDto(saved);
    }

    public UserDto updateUser(String userId, UpdateUserRequest request) {
        String normalizedId = userId.trim().toUpperCase();
        UserEntity entity = userRepository.findByUserIdIgnoreCase(normalizedId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found. Try again ..."));

        if (request.firstName() != null) {
            entity.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            entity.setLastName(request.lastName().trim());
        }
        if (request.password() != null) {
            entity.setPassword(passwordEncoder.encode(request.password().trim().toUpperCase()));
        }
        if (request.userType() != null) {
            entity.setUserType(request.userType().toUpperCase());
        }

        UserEntity saved = userRepository.save(entity);
        return toDto(saved);
    }

    public void deleteUser(String userId) {
        String normalizedId = userId.trim().toUpperCase();
        UserEntity entity = userRepository.findByUserIdIgnoreCase(normalizedId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found. Try again ..."));
        userRepository.delete(entity);
    }

    private UserDto toDto(UserEntity entity) {
        return new UserDto(
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getUserType()
        );
    }
}
