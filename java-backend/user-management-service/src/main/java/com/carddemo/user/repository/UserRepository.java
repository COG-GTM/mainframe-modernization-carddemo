package com.carddemo.user.repository;

import com.carddemo.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity operations.
 * 
 * Replaces VSAM file operations from COUSR00C.cbl:
 * 
 * STARTBR-USER-SEC-FILE:
 *   EXEC CICS STARTBR
 *        DATASET   (WS-USRSEC-FILE)
 *        RIDFLD    (SEC-USR-ID)
 *   END-EXEC
 * 
 * READNEXT-USER-SEC-FILE:
 *   EXEC CICS READNEXT
 *        DATASET   (WS-USRSEC-FILE)
 *        INTO      (SEC-USER-DATA)
 *        RIDFLD    (SEC-USR-ID)
 *   END-EXEC
 * 
 * The original COBOL reads records sequentially for pagination.
 * JPA provides built-in pagination support.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserId(String userId);

    Optional<User> findByUserIdAndIsActiveTrue(String userId);

    boolean existsByUserId(String userId);

    Page<User> findAllByIsActiveTrue(Pageable pageable);

    Page<User> findByUserIdStartingWithAndIsActiveTrue(String userIdPrefix, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
           "(LOWER(u.userId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    List<User> findByUserType(String userType);

    long countByUserType(String userType);

    long countByIsActiveTrue();
}
