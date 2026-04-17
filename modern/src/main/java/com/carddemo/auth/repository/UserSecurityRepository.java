package com.carddemo.auth.repository;

import com.carddemo.auth.entity.UserSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the USRSEC (user security) file.
 *
 * Migrated from: COSGN00C.cbl, paragraph READ-USER-SEC-FILE (lines 209-257)
 * Original CICS command:
 *   EXEC CICS READ DATASET('USRSEC') INTO(SEC-USER-DATA)
 *                  RIDFLD(WS-USER-ID) KEYLENGTH(LENGTH OF WS-USER-ID)
 * VSAM file: USRSEC (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS)
 */
@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurityEntity, String> {
}
