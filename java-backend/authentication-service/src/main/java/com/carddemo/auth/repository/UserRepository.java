package com.carddemo.auth.repository;

import com.carddemo.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity
 * 
 * Replaces the VSAM file operations from the mainframe:
 * - READ DATASET (WS-USRSEC-FILE) - findById()
 * - STARTBR/READNEXT/ENDBR - findAll() with pagination
 * 
 * Original COBOL file access in COSGN00C:
 *   EXEC CICS READ
 *        DATASET   (WS-USRSEC-FILE)
 *        INTO      (SEC-USER-DATA)
 *        RIDFLD    (WS-USER-ID)
 *        KEYLENGTH (LENGTH OF WS-USER-ID)
 *   END-EXEC
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by user ID (case-insensitive)
     * Mainframe converts to uppercase: FUNCTION UPPER-CASE(USERIDI OF COSGN0AI)
     */
    Optional<User> findByUserIdIgnoreCase(String userId);

    /**
     * Check if user exists by user ID
     */
    boolean existsByUserIdIgnoreCase(String userId);
}
