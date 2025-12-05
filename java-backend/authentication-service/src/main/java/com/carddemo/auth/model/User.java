package com.carddemo.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User entity representing the USRSEC VSAM file record structure.
 * 
 * Maps to CSUSR01Y.cpy copybook:
 *   01 SEC-USER-DATA.
 *     05 SEC-USR-ID                 PIC X(08).
 *     05 SEC-USR-FNAME              PIC X(20).
 *     05 SEC-USR-LNAME              PIC X(20).
 *     05 SEC-USR-PWD                PIC X(08).
 *     05 SEC-USR-TYPE               PIC X(01).
 *     05 SEC-USR-FILLER             PIC X(23).
 * 
 * User types:
 *   'A' - Admin user (e.g., ADMIN001)
 *   'U' - Regular user (e.g., USER0001)
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    private String userId;

    @Column(name = "first_name", length = 20, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 20, nullable = false)
    private String lastName;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "user_type", length = 1, nullable = false)
    private String userType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "is_active")
    private Boolean isActive;

    public boolean isAdmin() {
        return "A".equalsIgnoreCase(userType);
    }

    public boolean isRegularUser() {
        return "U".equalsIgnoreCase(userType);
    }
}
