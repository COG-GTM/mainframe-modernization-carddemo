package com.carddemo.user.repository;

import com.carddemo.user.entity.UserSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for user security records.
 * <p>
 * Migrated from: CICS file control operations on USRSEC VSAM KSDS file
 * CICS operations replaced:
 *   - EXEC CICS READ    FILE('USRSEC') -> findById()
 *   - EXEC CICS WRITE   FILE('USRSEC') -> save()
 *   - EXEC CICS REWRITE FILE('USRSEC') -> save() (update)
 *   - EXEC CICS DELETE  FILE('USRSEC') -> deleteById()
 *   - EXEC CICS STARTBR/READNEXT       -> findAll(Pageable)
 */
@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurityEntity, String> {
}
