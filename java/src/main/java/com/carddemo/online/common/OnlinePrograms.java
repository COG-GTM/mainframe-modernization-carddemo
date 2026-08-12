package com.carddemo.online.common;

import java.util.Map;

/**
 * Program names and the CICS transaction ids they run under (WS-PGMNAME / WS-TRANID of
 * COSGN00C, COMEN01C, COADM01C, COUSR00C, COUSR01C, COUSR02C and COUSR03C), used to fill
 * CDEMO-FROM-TRANID / CDEMO-TO-TRANID of copybook COCOM01Y during navigation.
 */
public final class OnlinePrograms {

    public static final String SIGNON = "COSGN00C";
    public static final String MAIN_MENU = "COMEN01C";
    public static final String ADMIN_MENU = "COADM01C";
    public static final String USER_LIST = "COUSR00C";
    public static final String USER_ADD = "COUSR01C";
    public static final String USER_UPDATE = "COUSR02C";
    public static final String USER_DELETE = "COUSR03C";

    public static final String TRANID_SIGNON = "CC00";
    public static final String TRANID_MAIN_MENU = "CM00";
    public static final String TRANID_ADMIN_MENU = "CA00";
    public static final String TRANID_USER_LIST = "CU00";
    public static final String TRANID_USER_ADD = "CU01";
    public static final String TRANID_USER_UPDATE = "CU02";
    public static final String TRANID_USER_DELETE = "CU03";

    private static final Map<String, String> TRANSACTION_IDS = Map.of(
            SIGNON, TRANID_SIGNON,
            MAIN_MENU, TRANID_MAIN_MENU,
            ADMIN_MENU, TRANID_ADMIN_MENU,
            USER_LIST, TRANID_USER_LIST,
            USER_ADD, TRANID_USER_ADD,
            USER_UPDATE, TRANID_USER_UPDATE,
            USER_DELETE, TRANID_USER_DELETE);

    /** Transaction id of a program, or {@code null} for programs migrated in other work streams. */
    public static String transactionIdOf(String programName) {
        return programName == null ? null : TRANSACTION_IDS.get(programName);
    }

    private OnlinePrograms() {
    }
}
