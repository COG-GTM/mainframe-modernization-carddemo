package com.carddemo.useradmin.repository;

import com.carddemo.useradmin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entity.
 * Replaces CICS VSAM file operations (READ, WRITE, REWRITE, DELETE)
 * on the USRSEC dataset.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
