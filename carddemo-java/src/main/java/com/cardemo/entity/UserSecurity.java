package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA).
 * Total COBOL record length: 80 bytes.
 * Seed data file: usrsec.txt.
 * Stores user authentication and authorization data (equivalent of RACF).
 */
@Entity
@Table(name = "user_security")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurity {

    /** SEC-USR-ID — PIC X(08), bytes [0:8]. User ID (primary key). */
    @Id
    @Column(name = "sec_usr_id", length = 8)
    private String secUsrId;

    /** SEC-USR-FNAME — PIC X(20), bytes [8:28]. First name. */
    @Column(name = "sec_usr_fname", length = 20)
    private String secUsrFname;

    /** SEC-USR-LNAME — PIC X(20), bytes [28:48]. Last name. */
    @Column(name = "sec_usr_lname", length = 20)
    private String secUsrLname;

    /** SEC-USR-PWD — PIC X(08), bytes [48:56]. Password. */
    @Column(name = "sec_usr_pwd", length = 8)
    private String secUsrPwd;

    /** SEC-USR-TYPE — PIC X(01), bytes [56:57]. User type (A=Admin, U=User). */
    @Column(name = "sec_usr_type", length = 1)
    private String secUsrType;

    // SEC-USR-FILLER — PIC X(23), bytes [57:80]. Padding — not mapped.
}
