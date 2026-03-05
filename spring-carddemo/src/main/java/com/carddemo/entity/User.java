package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity derived from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA).
 *
 * <pre>
 * 01 SEC-USER-DATA.
 *   05 SEC-USR-ID      PIC X(08).
 *   05 SEC-USR-FNAME   PIC X(20).
 *   05 SEC-USR-LNAME   PIC X(20).
 *   05 SEC-USR-PWD     PIC X(08).
 *   05 SEC-USR-TYPE    PIC X(01).
 * </pre>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", length = 8, nullable = false)
    @Size(max = 8)
    @NotNull
    private String userId;

    @Column(name = "first_name", length = 20)
    @Size(max = 20)
    private String firstName;

    @Column(name = "last_name", length = 20)
    @Size(max = 20)
    private String lastName;

    @Column(name = "password", length = 8)
    @Size(max = 8)
    private String password;

    @Column(name = "user_type", length = 1)
    @Size(max = 1)
    private String userType;
}
