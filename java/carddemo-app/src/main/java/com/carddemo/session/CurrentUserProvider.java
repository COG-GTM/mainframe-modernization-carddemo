package com.carddemo.session;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reads the authenticated principal populated by the Spring Security layer (owned by CS-2)
 * and derives the CardDemo {@link UserType}. This intentionally only <em>reads</em> the
 * {@link SecurityContextHolder}; it never configures authentication.
 *
 * <p>The COBOL sign-on ({@code COSGN00C}) moves the security-file user id and type into
 * {@code CDEMO-USER-ID} / {@code CDEMO-USER-TYPE}; here the equivalent values come from the
 * authenticated principal's name and its granted authorities (an {@code ADMIN}/
 * {@code ROLE_ADMIN} authority maps to {@link UserType#ADMIN}, otherwise
 * {@link UserType#USER}).</p>
 */
@Component
public class CurrentUserProvider {

    /** The authenticated user id, if any (the security principal's name). */
    public Optional<String> userId() {
        return authentication()
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getName);
    }

    /** The authenticated user's CardDemo type derived from granted authorities. */
    public Optional<UserType> userType() {
        return authentication()
            .filter(Authentication::isAuthenticated)
            .map(this::toUserType);
    }

    private UserType toUserType(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role != null && role.toUpperCase().contains("ADMIN")) {
                return UserType.ADMIN;
            }
        }
        return UserType.USER;
    }

    private Optional<Authentication> authentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }
}
