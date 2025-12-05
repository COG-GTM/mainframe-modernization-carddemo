package com.carddemo.user.service;

import com.carddemo.user.dto.CreateUserRequest;
import com.carddemo.user.dto.UpdateUserRequest;
import com.carddemo.user.dto.UserDto;
import com.carddemo.user.dto.UserListResponse;
import com.carddemo.user.model.User;
import com.carddemo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * User Service
 * 
 * Implements the business logic from COUSR00C, COUSR01C, COUSR02C, COUSR03C COBOL programs.
 * 
 * Original COBOL programs and their operations:
 * - COUSR00C: List users with pagination (STARTBR/READNEXT/READPREV/ENDBR)
 * - COUSR01C: Add new user (WRITE DATASET)
 * - COUSR02C: Update existing user (READ UPDATE/REWRITE)
 * - COUSR03C: Delete user (READ UPDATE/DELETE)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Default page size matching mainframe (USER-REC OCCURS 10 TIMES)
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * List all users with pagination
     * 
     * Replaces COUSR00C PROCESS-PAGE-FORWARD and PROCESS-PAGE-BACKWARD paragraphs
     * 
     * @param page page number (0-based)
     * @param size page size (default 10 to match mainframe)
     * @return paginated list of users
     */
    @Transactional(readOnly = true)
    public UserListResponse listUsers(int page, int size) {
        if (size <= 0) {
            size = DEFAULT_PAGE_SIZE;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAllByOrderByUserIdAsc(pageable);

        return UserListResponse.builder()
                .users(userPage.getContent().stream()
                        .map(this::toDto)
                        .toList())
                .currentPage(page + 1) // Convert to 1-based for display (like mainframe)
                .totalPages(userPage.getTotalPages())
                .totalUsers(userPage.getTotalElements())
                .hasNextPage(userPage.hasNext())
                .hasPreviousPage(userPage.hasPrevious())
                .build();
    }

    /**
     * Get user by ID
     * 
     * Replaces READ DATASET operation from COUSR02C/COUSR03C
     * 
     * @param userId user ID to find
     * @return user DTO
     * @throws UserNotFoundException if user not found (DFHRESP(NOTFND))
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(String userId) {
        User user = userRepository.findByUserIdIgnoreCase(userId.toUpperCase())
                .orElseThrow(() -> new UserNotFoundException("User ID NOT found..."));
        return toDto(user);
    }

    /**
     * Create new user
     * 
     * Replaces COUSR01C WRITE-USER-SEC-FILE paragraph
     * 
     * Original COBOL flow:
     * 1. Validate all fields are not empty
     * 2. WRITE DATASET with user data
     * 3. Check for DFHRESP(DUPKEY) or DFHRESP(DUPREC)
     * 4. Display success message: 'User {id} has been added ...'
     * 
     * @param request user creation request
     * @return created user DTO
     * @throws UserAlreadyExistsException if user ID already exists
     */
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        String userId = request.getUserId().toUpperCase();

        // Check for duplicate - equivalent to DFHRESP(DUPKEY) check
        if (userRepository.existsByUserIdIgnoreCase(userId)) {
            throw new UserAlreadyExistsException("User ID already exist...");
        }

        User user = User.builder()
                .userId(userId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .userType(request.getUserType())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User {} has been added", userId);

        return toDto(savedUser);
    }

    /**
     * Update existing user
     * 
     * Replaces COUSR02C UPDATE-USER-SEC-FILE paragraph
     * 
     * Original COBOL flow:
     * 1. READ DATASET with UPDATE option
     * 2. Compare each field to detect changes (USR-MODIFIED-YES flag)
     * 3. REWRITE DATASET if modified
     * 4. Display success message: 'User {id} has been updated ...'
     * 
     * @param userId user ID to update
     * @param request update request with fields to modify
     * @return updated user DTO
     * @throws UserNotFoundException if user not found
     * @throws NoModificationException if no fields were modified
     */
    @Transactional
    public UserDto updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findByUserIdIgnoreCase(userId.toUpperCase())
                .orElseThrow(() -> new UserNotFoundException("User ID NOT found..."));

        boolean modified = false;

        // Check and update each field (like mainframe's field-by-field comparison)
        if (StringUtils.hasText(request.getFirstName()) && 
                !request.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(request.getFirstName());
            modified = true;
        }

        if (StringUtils.hasText(request.getLastName()) && 
                !request.getLastName().equals(user.getLastName())) {
            user.setLastName(request.getLastName());
            modified = true;
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            modified = true;
        }

        if (request.getUserType() != null && 
                !request.getUserType().equals(user.getUserType())) {
            user.setUserType(request.getUserType());
            modified = true;
        }

        if (!modified) {
            throw new NoModificationException("Please modify to update ...");
        }

        User savedUser = userRepository.save(user);
        log.info("User {} has been updated", userId);

        return toDto(savedUser);
    }

    /**
     * Delete user
     * 
     * Replaces COUSR03C DELETE-USER-SEC-FILE paragraph
     * 
     * Original COBOL flow:
     * 1. READ DATASET with UPDATE option
     * 2. DELETE DATASET
     * 3. Display success message: 'User {id} has been deleted ...'
     * 
     * @param userId user ID to delete
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findByUserIdIgnoreCase(userId.toUpperCase())
                .orElseThrow(() -> new UserNotFoundException("User ID NOT found..."));

        userRepository.delete(user);
        log.info("User {} has been deleted", userId);
    }

    /**
     * Convert User entity to DTO
     */
    private UserDto toDto(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType())
                .build();
    }

    /**
     * Exception for user not found (DFHRESP(NOTFND))
     */
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Exception for duplicate user (DFHRESP(DUPKEY))
     */
    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    /**
     * Exception for no modification detected
     */
    public static class NoModificationException extends RuntimeException {
        public NoModificationException(String message) {
            super(message);
        }
    }
}
