package com.carddemo.user.repository;

import com.carddemo.user.model.User;
import com.carddemo.user.model.User.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 * 
 * Replaces the VSAM file operations from the mainframe COUSR programs:
 * 
 * COUSR00C (List Users):
 * - STARTBR/READNEXT/READPREV/ENDBR -> findAll() with pagination
 * - Browse operations with RIDFLD -> findByUserIdGreaterThanEqual()
 * 
 * COUSR01C (Add User):
 * - WRITE DATASET -> save()
 * - DFHRESP(DUPKEY) check -> existsByUserIdIgnoreCase()
 * 
 * COUSR02C (Update User):
 * - READ DATASET UPDATE -> findById()
 * - REWRITE DATASET -> save()
 * 
 * COUSR03C (Delete User):
 * - READ DATASET UPDATE -> findById()
 * - DELETE DATASET -> deleteById()
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by user ID (case-insensitive)
     * Mainframe converts to uppercase before lookup
     */
    Optional<User> findByUserIdIgnoreCase(String userId);

    /**
     * Check if user exists by user ID (case-insensitive)
     * Used to check for DFHRESP(DUPKEY) condition before insert
     */
    boolean existsByUserIdIgnoreCase(String userId);

    /**
     * Find users by type
     * Useful for filtering admin vs regular users
     */
    List<User> findByUserType(UserType userType);

    /**
     * Find users with pagination, ordered by user ID
     * Replaces STARTBR/READNEXT browse operations
     */
    Page<User> findAllByOrderByUserIdAsc(Pageable pageable);

    /**
     * Find users starting from a specific user ID (for browse operations)
     * Replaces STARTBR with RIDFLD positioning
     */
    Page<User> findByUserIdGreaterThanEqualOrderByUserIdAsc(String userId, Pageable pageable);
}
