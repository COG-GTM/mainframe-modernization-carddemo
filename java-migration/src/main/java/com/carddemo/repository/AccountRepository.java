package com.carddemo.repository;

import com.carddemo.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Account} entities.
 * <p>
 * Replaces VSAM KSDS indexed file access used in the original COBOL programs
 * ({@code CBACT01C}, {@code CBTRN02C}, {@code COACTVWC}).
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find accounts by active status ('Y' or 'N').
     */
    List<Account> findByActiveStatus(String activeStatus);

    /**
     * Find accounts belonging to a specific group.
     */
    List<Account> findByGroupId(String groupId);
}
