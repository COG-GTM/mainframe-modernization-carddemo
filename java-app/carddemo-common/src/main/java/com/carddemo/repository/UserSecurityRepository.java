package com.carddemo.repository;

import com.carddemo.entity.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserSecurity entity.
 * Replaces CICS READ on USRSEC VSAM file.
 */
@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, String> {
    Optional<UserSecurity> findByUserId(String userId);
}
