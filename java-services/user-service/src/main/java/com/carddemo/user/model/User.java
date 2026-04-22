package com.carddemo.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * User entity mapped from the COBOL USRSEC file layout (CSUSR01Y.cpy).
 *
 * COBOL layout (80 bytes):
 *   SEC-USR-ID     PIC X(08)  - User ID (primary key)
 *   SEC-USR-FNAME  PIC X(20)  - First name
 *   SEC-USR-LNAME  PIC X(20)  - Last name
 *   SEC-USR-PWD    PIC X(08)  - Password
 *   SEC-USR-TYPE   PIC X(01)  - User type ('A' = Admin, 'U' = User)
 *   SEC-USR-FILLER PIC X(23)  - Filler
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    private String userId;

    @Column(name = "first_name", length = 20)
    private String firstName;

    @Column(name = "last_name", length = 20)
    private String lastName;

    @Column(name = "password", length = 8, nullable = false)
    private String password;

    @Column(name = "user_type", length = 1, nullable = false)
    private String userType;

    public User() {
    }

    public User(String userId, String firstName, String lastName, String password, String userType) {
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
