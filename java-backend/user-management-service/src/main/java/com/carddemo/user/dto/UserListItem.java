package com.carddemo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user list items.
 * 
 * Maps to COUSR00C.cbl user list display:
 *   01 WS-USER-DATA.
 *     02 USER-REC OCCURS 10 TIMES.
 *       05 USER-SEL                   PIC X(01).
 *       05 FILLER                     PIC X(02).
 *       05 USER-ID                    PIC X(08).
 *       05 FILLER                     PIC X(02).
 *       05 USER-NAME                  PIC X(25).
 *       05 FILLER                     PIC X(02).
 *       05 USER-TYPE                  PIC X(08).
 * 
 * The original COBOL displays 10 users per page with
 * selection options: U (Update) or D (Delete)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListItem {

    private String userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String userType;
    private Boolean isActive;
}
