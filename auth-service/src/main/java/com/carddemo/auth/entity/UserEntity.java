package com.carddemo.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Maps to the COBOL SEC-USER-DATA copybook (CSUSR01Y.cpy).
 *
 * COBOL layout (80 bytes total):
 *   SEC-USR-ID      PIC X(08)  -- bytes 1-8
 *   SEC-USR-FNAME   PIC X(20)  -- bytes 9-28
 *   SEC-USR-LNAME   PIC X(20)  -- bytes 29-48
 *   SEC-USR-PWD     PIC X(08)  -- bytes 49-56
 *   SEC-USR-TYPE    PIC X(01)  -- byte 57  ('A'=admin, 'U'=user)
 *   SEC-USR-FILLER  PIC X(23)  -- bytes 58-80
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "usr_id", length = 8, nullable = false)
    private String userId;

    @Column(name = "usr_fname", length = 20, nullable = false)
    private String firstName;

    @Column(name = "usr_lname", length = 20, nullable = false)
    private String lastName;

    @Column(name = "usr_pwd", length = 72, nullable = false)
    private String password;

    @Column(name = "usr_type", length = 1, nullable = false)
    private String userType;

    public UserEntity() {
    }

    public UserEntity(String userId, String firstName, String lastName,
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

    public boolean isAdmin() {
        return "A".equalsIgnoreCase(userType);
    }
}
