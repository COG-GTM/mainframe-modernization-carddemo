package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** CSUSR01Y - user security record (80 bytes). */
@Entity
@Table(name = "sec_user")
public class SecUser {

    public static final String TYPE_ADMIN = "A";

    @Id
    @Column(name = "sec_usr_id", length = 8, nullable = false)
    private String id;

    @Column(name = "sec_usr_fname", length = 20)
    private String firstName;

    @Column(name = "sec_usr_lname", length = 20)
    private String lastName;

    @Column(name = "sec_usr_pwd", length = 8)
    private String password;

    @Column(name = "sec_usr_type", length = 1)
    private String userType;

    public boolean isAdmin() {
        return TYPE_ADMIN.equalsIgnoreCase(userType);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
