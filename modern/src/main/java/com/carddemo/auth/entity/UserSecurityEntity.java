package com.carddemo.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapping the USRSEC VSAM KSDS record layout.
 *
 * Migrated from: CSUSR01Y.cpy (copybook defining SEC-USER-DATA)
 * VSAM file: USRSEC (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS)
 * Original record layout:
 *   01 SEC-USER-DATA.
 *     05 SEC-USR-ID      PIC X(08).  -> userId   (PK, VARCHAR 8)
 *     05 SEC-USR-FNAME   PIC X(20).  -> firstName (VARCHAR 20)
 *     05 SEC-USR-LNAME   PIC X(20).  -> lastName  (VARCHAR 20)
 *     05 SEC-USR-PWD     PIC X(08).  -> password  (VARCHAR 8)
 *     05 SEC-USR-TYPE    PIC X(01).  -> userType  (CHAR 1, 'A' or 'U')
 *     05 SEC-USR-FILLER  PIC X(23).  -> (dropped - padding only)
 */
@Entity
@Table(name = "user_security")
public class UserSecurityEntity {

    @Id
    @Column(name = "usr_id", nullable = false, length = 8)
    private String userId;

    @Column(name = "usr_fname", nullable = false, length = 20)
    private String firstName;

    @Column(name = "usr_lname", nullable = false, length = 20)
    private String lastName;

    // TODO: Migrate to BCrypt hashing. Currently stores plaintext to match
    // legacy VSAM USRSEC file behavior where SEC-USR-PWD is PIC X(08).
    @Column(name = "usr_pwd", nullable = false, length = 8)
    private String password;

    @Column(name = "usr_type", nullable = false, length = 1)
    private String userType;

    protected UserSecurityEntity() {
    }

    public UserSecurityEntity(String userId, String firstName, String lastName,
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

    public boolean isAdmin() {
        return "A".equals(userType);
    }
}
