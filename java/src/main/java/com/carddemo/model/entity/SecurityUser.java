package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: CSUSR01Y (SEC-USER-DATA), VSAM file USRSEC, RECLN 80.
 */
@Entity
@Table(name = "sec_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUser {

    /** SEC-USR-ID PIC X(08). */
    @Id
    @Column(name = "usr_id", length = 8, nullable = false)
    private String userId;

    /** SEC-USR-FNAME PIC X(20). */
    @Column(name = "usr_fname", length = 20)
    private String firstName;

    /** SEC-USR-LNAME PIC X(20). */
    @Column(name = "usr_lname", length = 20)
    private String lastName;

    /** SEC-USR-PWD PIC X(08). */
    @Column(name = "usr_pwd", length = 8)
    private String password;

    /** SEC-USR-TYPE PIC X(01): 'A' admin, 'U' regular user. */
    @Column(name = "usr_type", length = 1)
    private String userType;

    public boolean isAdmin() {
        return "A".equalsIgnoreCase(userType);
    }
}
