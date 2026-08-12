package com.carddemo.online.user;

import lombok.Data;

/**
 * The COMMAREA extensions that COBOL programs COUSR00C, COUSR02C and COUSR03C append to copybook
 * COCOM01Y (CDEMO-CU00-INFO, CDEMO-CU02-INFO, CDEMO-CU03-INFO). The three layouts are identical
 * and describe the same browse, so a single object kept in the HTTP session next to
 * {@code CardDemoCommarea} replaces all of them.
 */
@Data
public class UserAdminState {

    public static final String SESSION_KEY = "CARDDEMO_USER_ADMIN_STATE";

    /** CDEMO-CU00-USRID-FIRST PIC X(08): user id shown on the first line of the page. */
    private String firstUserId;

    /** CDEMO-CU00-USRID-LAST PIC X(08): user id shown on the tenth line of the page. */
    private String lastUserId;

    /** CDEMO-CU00-PAGE-NUM PIC 9(08). */
    private int pageNumber;

    /** CDEMO-CU00-NEXT-PAGE-FLG PIC X(01): 'Y' = NEXT-PAGE-YES. */
    private boolean nextPage;

    /** CDEMO-CU00-USR-SEL-FLG PIC X(01): the 'U' or 'D' typed next to a listed user. */
    private String selectionFlag;

    /** CDEMO-CU00-USR-SELECTED PIC X(08). */
    private String selectedUserId;
}
