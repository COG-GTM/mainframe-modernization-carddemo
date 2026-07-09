package com.carddemo.security;

import com.carddemo.repository.SecurityUserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * {@link UserDetailsService} backed by the USRSEC store ({@link SecurityUserRepository}).
 *
 * <p>Replaces the {@code EXEC CICS READ DATASET('USRSEC')} lookup in {@code COSGN00C}. The
 * user id is upper-cased before lookup, mirroring
 * {@code MOVE FUNCTION UPPER-CASE(USERIDI) TO WS-USER-ID} in the COBOL. A missing record maps
 * to {@link UsernameNotFoundException} (the COBOL {@code WHEN 13} "user not found" branch).</p>
 */
@Service
public class CardDemoUserDetailsService implements UserDetailsService {

    private final SecurityUserRepository securityUserRepository;

    public CardDemoUserDetailsService(SecurityUserRepository securityUserRepository) {
        this.securityUserRepository = securityUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String userId = username == null ? "" : username.trim().toUpperCase(Locale.ROOT);
        return securityUserRepository.findById(userId)
                .map(CardDemoUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }
}
