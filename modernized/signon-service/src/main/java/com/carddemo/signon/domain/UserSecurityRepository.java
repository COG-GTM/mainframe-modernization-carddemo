package com.carddemo.signon.domain;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link UserSecurity}, replacing the keyed
 * {@code EXEC CICS READ DATASET('USRSEC') RIDFLD(WS-USER-ID)} lookup in
 * {@code COSGN00C}. The primary key is {@code SEC-USR-ID}, matching the VSAM
 * KSDS key definition {@code KEYS(8,0)}.
 */
public interface UserSecurityRepository extends JpaRepository<UserSecurity, String> {
}
