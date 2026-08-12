package com.carddemo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: COCOM01Y (CARDDEMO-COMMAREA).
 *
 * <p>In CICS this structure was passed between pseudo-conversational programs through the
 * COMMAREA. Here it is carried in the HTTP session so navigation state survives requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDemoCommarea {

    public static final String SESSION_KEY = "CARDDEMO_COMMAREA";

    public static final String USER_TYPE_ADMIN = "A";
    public static final String USER_TYPE_USER = "U";

    /** CDEMO-FROM-TRANID PIC X(04). */
    private String fromTransactionId;

    /** CDEMO-FROM-PROGRAM PIC X(08). */
    private String fromProgram;

    /** CDEMO-TO-TRANID PIC X(04). */
    private String toTransactionId;

    /** CDEMO-TO-PROGRAM PIC X(08). */
    private String toProgram;

    /** CDEMO-USER-ID PIC X(08). */
    private String userId;

    /** CDEMO-USER-TYPE PIC X(01): 'A' (CDEMO-USRTYP-ADMIN) or 'U' (CDEMO-USRTYP-USER). */
    private String userType;

    /** CDEMO-PGM-CONTEXT PIC 9(01): 0 = enter, 1 = re-enter. */
    private Integer programContext;

    /** CDEMO-CUST-ID PIC 9(09). */
    private Long customerId;

    /** CDEMO-CUST-FNAME PIC X(25). */
    private String customerFirstName;

    /** CDEMO-CUST-MNAME PIC X(25). */
    private String customerMiddleName;

    /** CDEMO-CUST-LNAME PIC X(25). */
    private String customerLastName;

    /** CDEMO-ACCT-ID PIC 9(11). */
    private Long accountId;

    /** CDEMO-ACCT-STATUS PIC X(01). */
    private String accountStatus;

    /** CDEMO-CARD-NUM PIC 9(16). */
    private String cardNumber;

    /** CDEMO-LAST-MAP PIC X(7). */
    private String lastMap;

    /** CDEMO-LAST-MAPSET PIC X(7). */
    private String lastMapset;

    public boolean isAdmin() {
        return USER_TYPE_ADMIN.equalsIgnoreCase(userType);
    }
}
