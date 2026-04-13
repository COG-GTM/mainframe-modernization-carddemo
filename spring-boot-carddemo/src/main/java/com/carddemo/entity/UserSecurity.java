package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA, RECLN 80).
 * User security record for authentication and authorization.
 */
@Entity
@Table(name = "user_security")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurity {

    /** SEC-USR-ID — PIC X(08). Primary key, user identifier. */
    @Id
    @Column(name = "sec_usr_id", length = 8)
    private String secUsrId;

    /** SEC-USR-FNAME — PIC X(20). User first name. */
    @Column(name = "sec_usr_fname", length = 20)
    private String secUsrFname;

    /** SEC-USR-LNAME — PIC X(20). User last name. */
    @Column(name = "sec_usr_lname", length = 20)
    private String secUsrLname;

    /** SEC-USR-PWD — PIC X(08). User password. */
    @Column(name = "sec_usr_pwd", length = 8)
    private String secUsrPwd;

    /** SEC-USR-TYPE — PIC X(01). User type (A=Admin, U=User). */
    @Column(name = "sec_usr_type", length = 1)
    private String secUsrType;
}
