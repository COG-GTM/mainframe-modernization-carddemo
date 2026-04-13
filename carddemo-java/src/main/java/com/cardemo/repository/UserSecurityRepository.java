package com.cardemo.repository;

import com.cardemo.entity.UserSecurity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link UserSecurity}.
 * Mirrors COBOL access patterns: keyed READ, sequential READ (STARTBR/READNEXT/READPREV),
 * WRITE, REWRITE, DELETE.
 * Programs: COSGN00C (signon), COUSR00C (list), COUSR01C (add), COUSR02C (update), COUSR03C (delete).
 */
@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurity, String> {

    List<UserSecurity> findBySecUsrType(String userType);
}
