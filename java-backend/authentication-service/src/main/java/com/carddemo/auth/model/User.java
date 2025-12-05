package com.carddemo.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User entity representing the USRSEC VSAM file structure from the mainframe.
 * 
 * Original COBOL Copybook: CSUSR01Y.cpy
 * 
 * COBOL Structure:
 *   01 SEC-USER-DATA.
 *     05 SEC-USR-ID                 PIC X(08).    -> userId (8 chars, primary key)
 *     05 SEC-USR-FNAME              PIC X(20).    -> firstName (20 chars)
 *     05 SEC-USR-LNAME              PIC X(20).    -> lastName (20 chars)
 *     05 SEC-USR-PWD                PIC X(08).    -> password (8 chars)
 *     05 SEC-USR-TYPE               PIC X(01).    -> userType ('A' = Admin, 'U' = User)
 *     05 SEC-USR-FILLER             PIC X(23).    -> reserved/filler
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * User ID - Primary key
     * Maps to SEC-USR-ID (PIC X(08))
     * Examples: USER0001, ADMIN001
     */
    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    private String userId;

    /**
     * First Name
     * Maps to SEC-USR-FNAME (PIC X(20))
     */
    @Column(name = "first_name", length = 20, nullable = false)
    private String firstName;

    /**
     * Last Name
     * Maps to SEC-USR-LNAME (PIC X(20))
     */
    @Column(name = "last_name", length = 20, nullable = false)
    private String lastName;

    /**
     * Password (will be stored as BCrypt hash in the new system)
     * Maps to SEC-USR-PWD (PIC X(08))
     * Note: Original mainframe stored plain text; new system uses BCrypt
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User Type - determines access level
     * Maps to SEC-USR-TYPE (PIC X(01))
     * 'A' = Admin (CDEMO-USRTYP-ADMIN) - routes to COADM01C
     * 'U' = User (CDEMO-USRTYP-USER) - routes to COMEN01C
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    /**
     * User type enumeration matching mainframe values
     */
    public enum UserType {
        ADMIN,  // 'A' in mainframe
        USER    // 'U' in mainframe
    }
}
