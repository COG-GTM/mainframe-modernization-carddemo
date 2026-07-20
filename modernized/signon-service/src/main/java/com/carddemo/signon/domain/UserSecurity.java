package com.carddemo.signon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Cloud datastore replacement for a record of the {@code USRSEC} VSAM KSDS.
 *
 * <p>Field lengths and order are taken verbatim from copybook
 * {@code CSUSR01Y} (record length 80):
 * <pre>
 *   01 SEC-USER-DATA.
 *     05 SEC-USR-ID     PIC X(08).   &lt;-- KSDS key (KEYS(8,0))
 *     05 SEC-USR-FNAME  PIC X(20).
 *     05 SEC-USR-LNAME  PIC X(20).
 *     05 SEC-USR-PWD    PIC X(08).
 *     05 SEC-USR-TYPE   PIC X(01).
 *     05 SEC-USR-FILLER PIC X(23).
 * </pre>
 * The 23-byte trailing filler is padding in the fixed-length record and carries
 * no business meaning, so it is not persisted as a column; it is preserved by
 * the fixed-width {@link UsrsecRecordMapper} when reading/writing raw records.
 */
@Entity
@Table(name = "usrsec")
public class UserSecurity {

    @Id
    @Column(name = "sec_usr_id", length = 8, nullable = false)
    private String userId;

    @Column(name = "sec_usr_fname", length = 20)
    private String firstName;

    @Column(name = "sec_usr_lname", length = 20)
    private String lastName;

    @Column(name = "sec_usr_pwd", length = 8)
    private String password;

    @Column(name = "sec_usr_type", length = 1)
    private String userType;

    protected UserSecurity() {
        // for JPA
    }

    public UserSecurity(String userId, String firstName, String lastName,
                        String password, String userType) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }

    public String getUserType() {
        return userType;
    }

    /** @return the typed interpretation of {@code SEC-USR-TYPE}. */
    public UserType type() {
        return UserType.fromCode(userType);
    }

    /** Mirrors the {@code CDEMO-USRTYP-ADMIN} 88-level check in {@code COSGN00C}. */
    public boolean isAdmin() {
        return type() == UserType.ADMIN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserSecurity that)) {
            return false;
        }
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
