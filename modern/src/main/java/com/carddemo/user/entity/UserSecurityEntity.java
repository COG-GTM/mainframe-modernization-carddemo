package com.carddemo.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity representing a user security record.
 * <p>
 * Migrated from: CSUSR01Y.cpy (USRSEC VSAM file layout)
 * Original COBOL copybook:
 * <pre>
 *   01 SEC-USER-DATA.
 *     05 SEC-USR-ID      PIC X(08).
 *     05 SEC-USR-FNAME   PIC X(20).
 *     05 SEC-USR-LNAME   PIC X(20).
 *     05 SEC-USR-PWD     PIC X(08).
 *     05 SEC-USR-TYPE    PIC X(01).
 *        88 SEC-USR-TYPE-ADMIN VALUE 'A'.
 *        88 SEC-USR-TYPE-USER  VALUE 'U'.
 *     05 SEC-USR-FILLER  PIC X(23).
 * </pre>
 * VSAM file: USRSEC (KSDS, key = SEC-USR-ID)
 */
@Entity
@Table(name = "user_security")
public class UserSecurityEntity {

    @Id
    @Column(name = "usr_id", length = 8, nullable = false)
    private String userId;

    @Column(name = "usr_fname", length = 20, nullable = false)
    private String firstName;

    @Column(name = "usr_lname", length = 20, nullable = false)
    private String lastName;

    @Column(name = "usr_pwd", length = 8, nullable = false)
    private String password;

    @Column(name = "usr_type", length = 1, nullable = false)
    private String userType;

    protected UserSecurityEntity() {
        // JPA requires a no-arg constructor
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

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
