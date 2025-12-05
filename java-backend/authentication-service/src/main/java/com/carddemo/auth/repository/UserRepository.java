package com.carddemo.auth.repository;

import com.carddemo.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity operations.
 * 
 * Replaces VSAM file operations from COSGN00C.cbl:
 *   EXEC CICS READ
 *        DATASET   (WS-USRSEC-FILE)
 *        INTO      (SEC-USER-DATA)
 *        RIDFLD    (WS-USER-ID)
 *        KEYLENGTH (LENGTH OF WS-USER-ID)
 *   END-EXEC
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserId(String userId);

    Optional<User> findByUserIdAndIsActiveTrue(String userId);

    boolean existsByUserId(String userId);
}
