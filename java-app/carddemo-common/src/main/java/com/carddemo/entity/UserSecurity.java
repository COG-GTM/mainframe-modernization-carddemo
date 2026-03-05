package com.carddemo.entity;

import com.carddemo.enums.UserType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JPA entity for user security data.
 * Migrated from COBOL copybook: CSUSR01Y.cpy (SEC-USER-DATA)
 * VSAM file: USRSEC
 */
@Entity
@Table(name = "user_security")
public class UserSecurity {

    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    @NotBlank
    @Size(max = 8)
    private String userId;

    @Column(name = "first_name", length = 20)
    @Size(max = 20)
    private String firstName;

    @Column(name = "last_name", length = 20)
    @Size(max = 20)
    private String lastName;

    @Column(name = "password", length = 72, nullable = false)
    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", length = 5, nullable = false)
    private UserType userType;

    public UserSecurity() {
    }

    public UserSecurity(String userId, String firstName, String lastName, String password, UserType userType) {
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

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
