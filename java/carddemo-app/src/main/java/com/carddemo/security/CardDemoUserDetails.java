package com.carddemo.security;

import com.carddemo.domain.SecurityUser;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * {@link UserDetails} adapter over the {@link SecurityUser} entity (copybook {@code CSUSR01Y}).
 *
 * <p>Exposes the authenticated principal's id, first/last name and {@code SEC-USR-TYPE}-derived
 * {@link CardDemoRole} so that later waves (CS-3 session/navigation) can read them straight off
 * the Spring Security principal via {@code SecurityContextHolder}.</p>
 *
 * <p><strong>Legacy-compat password handling:</strong> USRSEC stores 8-char <em>plaintext</em>
 * passwords (see the seed data loaded from {@code DUSRSECJ.jcl}). {@link #getPassword()}
 * therefore prefixes the stored value with the {@code {noop}} id so the delegating
 * {@code PasswordEncoder} (see {@link SecurityConfig}) matches it as-is, preserving the exact
 * behaviour of the {@code COSGN00C} {@code IF SEC-USR-PWD = WS-USER-PWD} comparison. This is a
 * deliberate migration-fidelity decision, not an endorsement of plaintext passwords — hashing
 * the credential store is tracked as a follow-up (see {@code docs/mapping/CS-2-security.md}).</p>
 */
public class CardDemoUserDetails implements UserDetails {

    private final String userId;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final CardDemoRole role;

    public CardDemoUserDetails(SecurityUser user) {
        this.userId = trim(user.getSecUsrId());
        this.password = trim(user.getSecUsrPwd());
        this.firstName = trim(user.getSecUsrFname());
        this.lastName = trim(user.getSecUsrLname());
        this.role = CardDemoRole.fromTypeCode(user.getSecUsrType());
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getAuthority()));
    }

    /** Plaintext password prefixed with {@code {noop}} for the delegating encoder — see class doc. */
    @Override
    public String getPassword() {
        return "{noop}" + password;
    }

    @Override
    public String getUsername() {
        return userId;
    }

    /** SEC-USR-ID PIC X(08). */
    public String getUserId() {
        return userId;
    }

    /** SEC-USR-FNAME PIC X(20). */
    public String getFirstName() {
        return firstName;
    }

    /** SEC-USR-LNAME PIC X(20). */
    public String getLastName() {
        return lastName;
    }

    /** Role derived from SEC-USR-TYPE ({@code 'A'} → ADMIN, otherwise USER). */
    public CardDemoRole getRole() {
        return role;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
