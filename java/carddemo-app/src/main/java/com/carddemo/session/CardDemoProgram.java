package com.carddemo.session;

/**
 * Registry of the online CardDemo transactions/programs, taken from the "Application
 * Inventory &gt; Online" table in the root {@code README.md}. Each constant binds a
 * four-character CICS {@code TRANSID}, the COBOL program name ({@code XCTL PROGRAM(..)}
 * target), the BMS map and a human-readable function name, plus an {@link AccessLevel}
 * describing which signed-on user type may reach it.
 *
 * <p>WAVE 3 online-session waves (CS-4..CS-9) register their {@code ScreenHandler} against
 * the matching constant here rather than inventing their own identifiers.</p>
 */
public enum CardDemoProgram {

    SIGNON("CC00", "COSGN00C", "COSGN00", "Signon Screen", AccessLevel.PUBLIC),
    MAIN_MENU("CM00", "COMEN01C", "COMEN01", "Main Menu", AccessLevel.USER),
    ADMIN_MENU("CA00", "COADM01C", "COADM01", "Admin Menu", AccessLevel.ADMIN),

    ACCOUNT_VIEW("CAVW", "COACTVWC", "COACTVW", "Account View", AccessLevel.USER),
    ACCOUNT_UPDATE("CAUP", "COACTUPC", "COACTUP", "Account Update", AccessLevel.USER),

    CARD_LIST("CCLI", "COCRDLIC", "COCRDLI", "Credit Card List", AccessLevel.USER),
    CARD_VIEW("CCDL", "COCRDSLC", "COCRDSL", "Credit Card View", AccessLevel.USER),
    CARD_UPDATE("CCUP", "COCRDUPC", "COCRDUP", "Credit Card Update", AccessLevel.USER),

    TRANSACTION_LIST("CT00", "COTRN00C", "COTRN00", "Transaction List", AccessLevel.USER),
    TRANSACTION_VIEW("CT01", "COTRN01C", "COTRN01", "Transaction View", AccessLevel.USER),
    TRANSACTION_ADD("CT02", "COTRN02C", "COTRN02", "Transaction Add", AccessLevel.USER),

    REPORTS("CR00", "CORPT00C", "CORPT00", "Transaction Reports", AccessLevel.USER),
    BILL_PAYMENT("CB00", "COBIL00C", "COBIL00", "Bill Payment", AccessLevel.USER),

    USER_LIST("CU00", "COUSR00C", "COUSR00", "List Users", AccessLevel.ADMIN),
    USER_ADD("CU01", "COUSR01C", "COUSR01", "Add User", AccessLevel.ADMIN),
    USER_UPDATE("CU02", "COUSR02C", "COUSR02", "Update User", AccessLevel.ADMIN),
    USER_DELETE("CU03", "COUSR03C", "COUSR03", "Delete User", AccessLevel.ADMIN);

    /** Which signed-on user type may reach a program. */
    public enum AccessLevel {
        /** Reachable without being signed on (the sign-on screen itself). */
        PUBLIC,
        /** Regular-user (and admin) functions. */
        USER,
        /** Admin-only functions. */
        ADMIN
    }

    private final String tranId;
    private final String programName;
    private final String bmsMap;
    private final String function;
    private final AccessLevel accessLevel;

    CardDemoProgram(String tranId, String programName, String bmsMap, String function,
            AccessLevel accessLevel) {
        this.tranId = tranId;
        this.programName = programName;
        this.bmsMap = bmsMap;
        this.function = function;
        this.accessLevel = accessLevel;
    }

    /** CICS TRANSID, e.g. {@code CC00}. */
    public String tranId() {
        return tranId;
    }

    /** COBOL program name / {@code XCTL} target, e.g. {@code COSGN00C}. */
    public String programName() {
        return programName;
    }

    /** BMS map name, e.g. {@code COSGN00}. */
    public String bmsMap() {
        return bmsMap;
    }

    /** Human-readable function label from the README inventory. */
    public String function() {
        return function;
    }

    public AccessLevel accessLevel() {
        return accessLevel;
    }

    /** Whether the given user type may reach this program. */
    public boolean isAccessibleBy(UserType userType) {
        return switch (accessLevel) {
            case PUBLIC -> true;
            case USER -> userType == UserType.USER || userType == UserType.ADMIN;
            case ADMIN -> userType == UserType.ADMIN;
        };
    }
}
