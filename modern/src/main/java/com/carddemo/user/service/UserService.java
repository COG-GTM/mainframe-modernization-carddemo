package com.carddemo.user.service;

import com.carddemo.user.dto.CreateUserRequest;
import com.carddemo.user.dto.UpdateUserRequest;
import com.carddemo.user.dto.UserResponse;
import com.carddemo.user.entity.UserSecurityEntity;
import com.carddemo.user.exception.DuplicateUserException;
import com.carddemo.user.exception.UserNotFoundException;
import com.carddemo.user.repository.UserSecurityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic layer for User Administration CRUD operations.
 * <p>
 * Migrated from: COUSR00C.cbl (User List), COUSR01C.cbl (User Add),
 *                COUSR02C.cbl (User Update), COUSR03C.cbl (User Delete)
 * <p>
 * CICS operations replaced:
 *   - STARTBR/READNEXT/READPREV on USRSEC -> listUsers() with Spring Data pagination
 *   - READ on USRSEC                       -> getUser()
 *   - WRITE to USRSEC                      -> createUser()
 *   - REWRITE on USRSEC                    -> updateUser()
 *   - DELETE on USRSEC                     -> deleteUser()
 * <p>
 * Validation rules preserved from COBOL programs:
 *   - User ID: max 8 chars, not blank, unique on create
 *   - First/Last name: max 20 chars, required
 *   - Password: max 8 chars, required
 *   - User type: must be 'A' or 'U' (level-88 conditions SEC-USR-TYPE-ADMIN/SEC-USR-TYPE-USER)
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserSecurityRepository userRepository;

    public UserService(UserSecurityRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * List users with pagination support.
     * <p>
     * Migrated from: COUSR00C.cbl (User List — CU00)
     * CICS operations: STARTBR/READNEXT/READPREV on USRSEC
     * Original displays 10 users per page with PF7/PF8 for paging.
     */
    public Page<UserResponse> listUsers(int page, int size, String sortBy, String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Get a single user by their ID.
     * <p>
     * Migrated from: COUSR02C.cbl / COUSR03C.cbl — initial READ by key
     * CICS operation: EXEC CICS READ FILE('USRSEC') INTO(SEC-USER-DATA)
     *                 RIDFLD(SEC-USR-ID) RESP(WS-RESP-CD)
     *
     * @throws UserNotFoundException if user not found (maps to CICS RESP NOTFND)
     */
    public UserResponse getUser(String userId) {
        UserSecurityEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return toResponse(entity);
    }

    /**
     * Create a new user.
     * <p>
     * Migrated from: COUSR01C.cbl (User Add — CU01)
     * CICS operation: EXEC CICS WRITE FILE('USRSEC')
     *                 FROM(SEC-USER-DATA) RIDFLD(SEC-USR-ID)
     * <p>
     * Validation from COUSR01C:
     *   - Checks SEC-USR-ID not empty, not duplicate
     *   - Checks SEC-USR-FNAME, SEC-USR-LNAME required
     *   - Checks SEC-USR-PWD required
     *   - Checks SEC-USR-TYPE must be 'A' or 'U'
     *
     * @throws DuplicateUserException if user ID already exists (maps to CICS RESP DUPREC)
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String normalizedId = request.userId().toUpperCase().trim();

        if (userRepository.existsById(normalizedId)) {
            throw new DuplicateUserException(normalizedId);
        }

        UserSecurityEntity entity = new UserSecurityEntity(
                normalizedId,
                request.firstName().trim(),
                request.lastName().trim(),
                request.password(),
                request.userType().toUpperCase().trim()
        );

        UserSecurityEntity saved = userRepository.save(entity);
        return toResponse(saved);
    }

    /**
     * Update an existing user.
     * <p>
     * Migrated from: COUSR02C.cbl (User Update — CU02)
     * CICS operations:
     *   1. EXEC CICS READ FILE('USRSEC') ... UPDATE
     *   2. (validate changes)
     *   3. EXEC CICS REWRITE FILE('USRSEC') FROM(SEC-USER-DATA)
     * Navigation: PF3 returns to CDEMO-FROM-PROGRAM (typically COUSR00C or COADM01C)
     *
     * @throws UserNotFoundException if user not found (maps to CICS RESP NOTFND)
     */
    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        UserSecurityEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        entity.setFirstName(request.firstName().trim());
        entity.setLastName(request.lastName().trim());
        entity.setPassword(request.password());
        entity.setUserType(request.userType().toUpperCase().trim());

        UserSecurityEntity saved = userRepository.save(entity);
        return toResponse(saved);
    }

    /**
     * Delete a user.
     * <p>
     * Migrated from: COUSR03C.cbl (User Delete — CU03)
     * CICS operations:
     *   1. EXEC CICS READ FILE('USRSEC') ... (verify exists)
     *   2. EXEC CICS DELETE FILE('USRSEC') RIDFLD(SEC-USR-ID)
     * Navigation: PF3 returns to CDEMO-FROM-PROGRAM (typically COUSR00C or COADM01C)
     *
     * @throws UserNotFoundException if user not found (maps to CICS RESP NOTFND)
     */
    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }

    private UserResponse toResponse(UserSecurityEntity entity) {
        return new UserResponse(
                entity.getUserId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getUserType()
        );
    }
}
