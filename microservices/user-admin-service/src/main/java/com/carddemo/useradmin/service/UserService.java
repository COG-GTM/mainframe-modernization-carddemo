package com.carddemo.useradmin.service;

import com.carddemo.useradmin.dto.CreateUserRequest;
import com.carddemo.useradmin.dto.UpdateUserRequest;
import com.carddemo.useradmin.dto.UserResponse;
import com.carddemo.useradmin.entity.User;
import com.carddemo.useradmin.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for user administration.
 * Modernized from COBOL programs:
 *   COUSR00C - User List   (listUsers)
 *   COUSR01C - User Add    (createUser)
 *   COUSR02C - User Update (updateUser)
 *   COUSR03C - User Delete (deleteUser)
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * List all users with pagination.
     * Replaces COUSR00C STARTBR/READNEXT browse loop on USRSEC VSAM file.
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * Create a new user with BCrypt-hashed password.
     * Replaces COUSR01C WRITE operation on USRSEC.
     * Returns 409 Conflict if user ID already exists (DFHRESP(DUPREC)).
     */
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsById(request.getUserId())) {
            throw new UserAlreadyExistsException(
                    "User ID already exists: " + request.getUserId());
        }

        User user = new User();
        user.setUsrId(request.getUserId());
        user.setUsrFname(request.getFirstName());
        user.setUsrLname(request.getLastName());
        user.setUsrPwd(passwordEncoder.encode(request.getPassword()));
        user.setUsrType(request.getUserType().toUpperCase());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    /**
     * Update an existing user.
     * Replaces COUSR02C READ + REWRITE operation on USRSEC.
     * Password is optional — only hashed and updated if provided.
     */
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "User ID not found: " + userId));

        if (request.getFirstName() != null) {
            user.setUsrFname(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setUsrLname(request.getLastName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setUsrPwd(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getUserType() != null) {
            user.setUsrType(request.getUserType().toUpperCase());
        }

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    /**
     * Delete a user by ID.
     * Replaces COUSR03C READ + DELETE operation on USRSEC.
     */
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User ID not found: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUsrId(),
                user.getUsrFname(),
                user.getUsrLname(),
                user.getUsrType()
        );
    }

    // --- Exception classes ---

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }
}
