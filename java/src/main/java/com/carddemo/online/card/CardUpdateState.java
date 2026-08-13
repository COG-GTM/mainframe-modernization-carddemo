package com.carddemo.online.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDUPC — WS-THIS-PROGCOMMAREA: CCUP-CHANGE-ACTION together with
 * CCUP-OLD-DETAILS and CCUP-NEW-DETAILS. It rides on the COMMAREA between turns of
 * CCUP and drives the confirm-then-commit flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardUpdateState {

    public static final String SESSION_KEY = "COCRDUPC_PROGRAM_COMMAREA";

    /** 88 CCUP-DETAILS-NOT-FETCHED VALUES LOW-VALUES, SPACES. */
    public static final String DETAILS_NOT_FETCHED = " ";
    /** 88 CCUP-SHOW-DETAILS VALUE 'S'. */
    public static final String SHOW_DETAILS = "S";
    /** 88 CCUP-CHANGES-NOT-OK VALUE 'E'. */
    public static final String CHANGES_NOT_OK = "E";
    /** 88 CCUP-CHANGES-OK-NOT-CONFIRMED VALUE 'N'. */
    public static final String CHANGES_OK_NOT_CONFIRMED = "N";
    /** 88 CCUP-CHANGES-OKAYED-AND-DONE VALUE 'C'. */
    public static final String CHANGES_OKAYED_AND_DONE = "C";
    /** 88 CCUP-CHANGES-OKAYED-LOCK-ERROR VALUE 'L'. */
    public static final String CHANGES_OKAYED_LOCK_ERROR = "L";
    /** 88 CCUP-CHANGES-OKAYED-BUT-FAILED VALUE 'F'. */
    public static final String CHANGES_OKAYED_BUT_FAILED = "F";

    private String changeAction = DETAILS_NOT_FETCHED;
    private CardUpdateData oldDetails;
    private CardUpdateData newDetails;

    public boolean isDetailsNotFetched() {
        return changeAction == null || DETAILS_NOT_FETCHED.equals(changeAction) || changeAction.isBlank();
    }

    /** 88 CCUP-CHANGES-FAILED VALUES 'L', 'F'. */
    public boolean isChangesFailed() {
        return CHANGES_OKAYED_LOCK_ERROR.equals(changeAction) || CHANGES_OKAYED_BUT_FAILED.equals(changeAction);
    }
}
