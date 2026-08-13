package com.carddemo.online.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COACTUPC — WS-THIS-PROGCOMMAREA, i.e. ACUP-CHANGE-ACTION plus the
 * ACUP-OLD-DETAILS / ACUP-NEW-DETAILS areas that travel on the COMMAREA between
 * pseudo-conversational turns of transaction CAUP.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateState {

    /** Key under which this program area is kept in the HTTP session. */
    public static final String SESSION_KEY = "COACTUPC_PROGRAM_COMMAREA";

    /** 88 ACUP-DETAILS-NOT-FETCHED (LOW-VALUES / SPACES). */
    public static final String DETAILS_NOT_FETCHED = " ";
    /** 88 ACUP-SHOW-DETAILS. */
    public static final String SHOW_DETAILS = "S";
    /** 88 ACUP-CHANGES-NOT-OK. */
    public static final String CHANGES_NOT_OK = "E";
    /** 88 ACUP-CHANGES-OK-NOT-CONFIRMED. */
    public static final String CHANGES_OK_NOT_CONFIRMED = "N";
    /** 88 ACUP-CHANGES-OKAYED-AND-DONE. */
    public static final String CHANGES_OKAYED_AND_DONE = "C";
    /** 88 ACUP-CHANGES-OKAYED-LOCK-ERROR. */
    public static final String CHANGES_OKAYED_LOCK_ERROR = "L";
    /** 88 ACUP-CHANGES-OKAYED-BUT-FAILED. */
    public static final String CHANGES_OKAYED_BUT_FAILED = "F";

    private String changeAction = DETAILS_NOT_FETCHED;
    private AccountUpdateData oldDetails;
    private AccountUpdateData newDetails;

    public boolean isDetailsNotFetched() {
        return changeAction == null || DETAILS_NOT_FETCHED.equals(changeAction);
    }

    /** 88 ACUP-CHANGES-FAILED (values 'L' and 'F'). */
    public boolean isChangesFailed() {
        return CHANGES_OKAYED_LOCK_ERROR.equals(changeAction) || CHANGES_OKAYED_BUT_FAILED.equals(changeAction);
    }
}
