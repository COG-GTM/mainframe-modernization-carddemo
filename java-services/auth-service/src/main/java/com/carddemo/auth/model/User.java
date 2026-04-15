package com.carddemo.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User entity mapped from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA).
 *
 * COBOL layout:
 *   05 SEC-USR-ID     PIC X(08)
 *   05 SEC-USR-FNAME  PIC X(20)
 *   05 SEC-USR-LNAME  PIC X(20)
 *   05 SEC-USR-PWD    PIC X(08)
 *   05 SEC-USR-TYPE   PIC X(01) — 'A' (admin) or 'U' (regular user)
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
