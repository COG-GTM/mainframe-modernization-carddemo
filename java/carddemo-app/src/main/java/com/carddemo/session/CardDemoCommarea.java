package com.carddemo.session;

/**
 * Java mirror of the CICS COMMAREA defined in copybook {@code app/cpy/COCOM01Y.cpy}
 * ({@code 01 CARDDEMO-COMMAREA}).
 *
 * <p>In the legacy pseudo-conversational application every online program passes this
 * structure between terminal interactions via {@code EXEC CICS RETURN ... COMMAREA(..)} /
 * {@code XCTL ... COMMAREA(..)}. This DTO is stored in the HTTP session (see
 * {@link CommareaSessionStore}) so it survives across stateless REST calls exactly as the
 * COMMAREA survives across pseudo-conversational turns.</p>
 *
 * <p>The COBOL {@code 05}-level groups are reproduced as nested classes so the mapping is
 * field-for-field. COBOL→Java type rules (see {@code java/README.md}): fixed-width numeric
 * identifiers with leading zeros map to {@link String}; single-character flags map to
 * {@link String}/enum accessors.</p>
 */
public class CardDemoCommarea {

    private final GeneralInfo generalInfo = new GeneralInfo();
    private final CustomerInfo customerInfo = new CustomerInfo();
    private final AccountInfo accountInfo = new AccountInfo();
    private final CardInfo cardInfo = new CardInfo();
    private final MoreInfo moreInfo = new MoreInfo();

    public GeneralInfo getGeneralInfo() {
        return generalInfo;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public MoreInfo getMoreInfo() {
        return moreInfo;
    }

    // ---- Convenience accessors for the most-used general-info fields -----------------

    public String getFromTranId() {
        return generalInfo.fromTranId;
    }

    public String getFromProgram() {
        return generalInfo.fromProgram;
    }

    public String getToTranId() {
        return generalInfo.toTranId;
    }

    public String getToProgram() {
        return generalInfo.toProgram;
    }

    public String getUserId() {
        return generalInfo.userId;
    }

    public UserType getUserType() {
        return UserType.fromCode(generalInfo.userType);
    }

    public ProgramContext getProgramContext() {
        return ProgramContext.fromCode(generalInfo.pgmContext);
    }

    /** {@code CDEMO-PGM-ENTER} — first entry into the current program. */
    public boolean isEnter() {
        return getProgramContext() == ProgramContext.ENTER;
    }

    /** {@code CDEMO-PGM-REENTER} — a subsequent turn in the current program. */
    public boolean isReenter() {
        return getProgramContext() == ProgramContext.REENTER;
    }

    /**
     * {@code 05 CDEMO-GENERAL-INFO} — transfer-of-control routing plus the signed-on
     * user's identity and the pseudo-conversational context flag.
     */
    public static class GeneralInfo {

        /** CDEMO-FROM-TRANID PIC X(04). */
        private String fromTranId;
        /** CDEMO-FROM-PROGRAM PIC X(08). */
        private String fromProgram;
        /** CDEMO-TO-TRANID PIC X(04). */
        private String toTranId;
        /** CDEMO-TO-PROGRAM PIC X(08). */
        private String toProgram;
        /** CDEMO-USER-ID PIC X(08). */
        private String userId;
        /** CDEMO-USER-TYPE PIC X(01) — 'A' admin / 'U' user. */
        private String userType;
        /** CDEMO-PGM-CONTEXT PIC 9(01) — 0 ENTER / 1 RE-ENTER. */
        private int pgmContext;

        public String getFromTranId() {
            return fromTranId;
        }

        public void setFromTranId(String fromTranId) {
            this.fromTranId = fromTranId;
        }

        public String getFromProgram() {
            return fromProgram;
        }

        public void setFromProgram(String fromProgram) {
            this.fromProgram = fromProgram;
        }

        public String getToTranId() {
            return toTranId;
        }

        public void setToTranId(String toTranId) {
            this.toTranId = toTranId;
        }

        public String getToProgram() {
            return toProgram;
        }

        public void setToProgram(String toProgram) {
            this.toProgram = toProgram;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public int getPgmContext() {
            return pgmContext;
        }

        public void setPgmContext(int pgmContext) {
            this.pgmContext = pgmContext;
        }
    }

    /** {@code 05 CDEMO-CUSTOMER-INFO} — selected customer carried between screens. */
    public static class CustomerInfo {

        /** CDEMO-CUST-ID PIC 9(09) — 9-char zero-padded id (never used in arithmetic). */
        private String custId;
        /** CDEMO-CUST-FNAME PIC X(25). */
        private String custFirstName;
        /** CDEMO-CUST-MNAME PIC X(25). */
        private String custMiddleName;
        /** CDEMO-CUST-LNAME PIC X(25). */
        private String custLastName;

        public String getCustId() {
            return custId;
        }

        public void setCustId(String custId) {
            this.custId = custId;
        }

        public String getCustFirstName() {
            return custFirstName;
        }

        public void setCustFirstName(String custFirstName) {
            this.custFirstName = custFirstName;
        }

        public String getCustMiddleName() {
            return custMiddleName;
        }

        public void setCustMiddleName(String custMiddleName) {
            this.custMiddleName = custMiddleName;
        }

        public String getCustLastName() {
            return custLastName;
        }

        public void setCustLastName(String custLastName) {
            this.custLastName = custLastName;
        }
    }

    /** {@code 05 CDEMO-ACCOUNT-INFO} — selected account carried between screens. */
    public static class AccountInfo {

        /** CDEMO-ACCT-ID PIC 9(11) — 11-char zero-padded id (never used in arithmetic). */
        private String acctId;
        /** CDEMO-ACCT-STATUS PIC X(01). */
        private String acctStatus;

        public String getAcctId() {
            return acctId;
        }

        public void setAcctId(String acctId) {
            this.acctId = acctId;
        }

        public String getAcctStatus() {
            return acctStatus;
        }

        public void setAcctStatus(String acctStatus) {
            this.acctStatus = acctStatus;
        }
    }

    /** {@code 05 CDEMO-CARD-INFO} — selected card carried between screens. */
    public static class CardInfo {

        /** CDEMO-CARD-NUM PIC 9(16) — 16-char zero-padded PAN (never used in arithmetic). */
        private String cardNum;

        public String getCardNum() {
            return cardNum;
        }

        public void setCardNum(String cardNum) {
            this.cardNum = cardNum;
        }
    }

    /** {@code 05 CDEMO-MORE-INFO} — last BMS map/mapset shown (used on re-entry). */
    public static class MoreInfo {

        /** CDEMO-LAST-MAP PIC X(7). */
        private String lastMap;
        /** CDEMO-LAST-MAPSET PIC X(7). */
        private String lastMapset;

        public String getLastMap() {
            return lastMap;
        }

        public void setLastMap(String lastMap) {
            this.lastMap = lastMap;
        }

        public String getLastMapset() {
            return lastMapset;
        }

        public void setLastMapset(String lastMapset) {
            this.lastMapset = lastMapset;
        }
    }
}
