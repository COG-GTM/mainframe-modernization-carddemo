package com.carddemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO class that mirrors the CARDDEMO-COMMAREA from COCOM01Y.cpy.
 * Used to manage session state, replacing the CICS COMMAREA concept.
 *
 * <pre>
 * 01 CARDDEMO-COMMAREA.
 *    05 CDEMO-GENERAL-INFO.
 *       10 CDEMO-FROM-TRANID        PIC X(04).
 *       10 CDEMO-FROM-PROGRAM       PIC X(08).
 *       10 CDEMO-TO-TRANID          PIC X(04).
 *       10 CDEMO-TO-PROGRAM         PIC X(08).
 *       10 CDEMO-USER-ID            PIC X(08).
 *       10 CDEMO-USER-TYPE          PIC X(01).
 *       10 CDEMO-PGM-CONTEXT        PIC 9(01).
 *    05 CDEMO-CUSTOMER-INFO.
 *       10 CDEMO-CUST-ID            PIC 9(09).
 *       10 CDEMO-CUST-FNAME         PIC X(25).
 *       10 CDEMO-CUST-MNAME         PIC X(25).
 *       10 CDEMO-CUST-LNAME         PIC X(25).
 *    05 CDEMO-ACCOUNT-INFO.
 *       10 CDEMO-ACCT-ID            PIC 9(11).
 *       10 CDEMO-ACCT-STATUS        PIC X(01).
 *    05 CDEMO-CARD-INFO.
 *       10 CDEMO-CARD-NUM           PIC 9(16).
 *    05 CDEMO-MORE-INFO.
 *       10 CDEMO-LAST-MAP           PIC X(7).
 *       10 CDEMO-LAST-MAPSET        PIC X(7).
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDemoSession {

    // General info
    private String fromTranId;
    private String fromProgram;
    private String toTranId;
    private String toProgram;
    private String userId;
    private String userType;
    private Integer pgmContext;

    // Customer info
    private Long customerId;
    private String customerFirstName;
    private String customerMiddleName;
    private String customerLastName;

    // Account info
    private Long accountId;
    private String accountStatus;

    // Card info
    private String cardNumber;

    // Navigation info
    private String lastMap;
    private String lastMapSet;
}
