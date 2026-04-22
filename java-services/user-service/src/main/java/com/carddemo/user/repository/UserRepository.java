package com.carddemo.user.repository;

import com.carddemo.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entity.
 * Replaces direct VSAM USRSEC file I/O operations from the COBOL programs:
 *   - STARTBR/READNEXT/READPREV/ENDBR (COUSR00C - sequential browsing)
 *   - READ with RIDFLD (COUSR02C/COUSR03C - keyed lookup)
 *   - WRITE (COUSR01C - add record)
 *   - REWRITE (COUSR02C - update record)
 *   - DELETE (COUSR03C - delete record)
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
