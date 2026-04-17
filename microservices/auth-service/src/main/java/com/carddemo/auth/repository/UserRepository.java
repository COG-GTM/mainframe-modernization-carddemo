package com.carddemo.auth.repository;

import com.carddemo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * Replaces CICS READ against the USRSEC VSAM file (keyed by SEC-USR-ID).
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsrId(String usrId);
}
