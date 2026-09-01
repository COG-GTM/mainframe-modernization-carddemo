package com.carddemo.posttran;

/**
 * Field offsets/lengths transcribed from the copybooks used by app/cbl/CBTRN02C.cbl:102-126.
 * Offsets are zero-based byte positions inside the fixed-length record.
 */
public final class Layouts {

    private Layouts() {
    }

    /** app/cpy/CVTRA06Y.cpy — DALYTRAN-RECORD, 350 bytes. */
    public static final class DalyTran {
        public static final int LEN = 350;
        public static final int ID = 0, ID_LEN = 16;
        public static final int TYPE_CD = 16, TYPE_CD_LEN = 2;
        public static final int CAT_CD = 18, CAT_CD_LEN = 4;
        public static final int SOURCE = 22, SOURCE_LEN = 10;
        public static final int DESC = 32, DESC_LEN = 100;
        public static final int AMT = 132, AMT_LEN = 11;          // S9(09)V99
        public static final int MERCHANT_ID = 143, MERCHANT_ID_LEN = 9;
        public static final int MERCHANT_NAME = 152, MERCHANT_NAME_LEN = 50;
        public static final int MERCHANT_CITY = 202, MERCHANT_CITY_LEN = 50;
        public static final int MERCHANT_ZIP = 252, MERCHANT_ZIP_LEN = 10;
        public static final int CARD_NUM = 262, CARD_NUM_LEN = 16;
        public static final int ORIG_TS = 278, ORIG_TS_LEN = 26;
        public static final int PROC_TS = 304, PROC_TS_LEN = 26;

        private DalyTran() {
        }
    }

    /** app/cpy/CVTRA05Y.cpy — TRAN-RECORD, 350 bytes (same field order as DALYTRAN). */
    public static final class Tran {
        public static final int LEN = 350;
        public static final int ID = 0, ID_LEN = 16;
        public static final int TYPE_CD = 16, TYPE_CD_LEN = 2;
        public static final int CAT_CD = 18, CAT_CD_LEN = 4;
        public static final int SOURCE = 22, SOURCE_LEN = 10;
        public static final int DESC = 32, DESC_LEN = 100;
        public static final int AMT = 132, AMT_LEN = 11;
        public static final int MERCHANT_ID = 143, MERCHANT_ID_LEN = 9;
        public static final int MERCHANT_NAME = 152, MERCHANT_NAME_LEN = 50;
        public static final int MERCHANT_CITY = 202, MERCHANT_CITY_LEN = 50;
        public static final int MERCHANT_ZIP = 252, MERCHANT_ZIP_LEN = 10;
        public static final int CARD_NUM = 262, CARD_NUM_LEN = 16;
        public static final int ORIG_TS = 278, ORIG_TS_LEN = 26;
        public static final int PROC_TS = 304, PROC_TS_LEN = 26;
        public static final int FILLER = 330, FILLER_LEN = 20;

        private Tran() {
        }
    }

    /** app/cpy/CVACT03Y.cpy — CARD-XREF-RECORD, 50 bytes; KSDS key = XREF-CARD-NUM (16). */
    public static final class Xref {
        public static final int LEN = 50;
        public static final int KEY_LEN = 16;
        public static final int CARD_NUM = 0, CARD_NUM_LEN = 16;
        public static final int CUST_ID = 16, CUST_ID_LEN = 9;
        public static final int ACCT_ID = 25, ACCT_ID_LEN = 11;

        private Xref() {
        }
    }

    /** app/cpy/CVACT01Y.cpy — ACCOUNT-RECORD, 300 bytes; KSDS key = ACCT-ID (11). */
    public static final class Account {
        public static final int LEN = 300;
        public static final int KEY_LEN = 11;
        public static final int ID = 0, ID_LEN = 11;
        public static final int ACTIVE_STATUS = 11, ACTIVE_STATUS_LEN = 1;
        public static final int CURR_BAL = 12, CURR_BAL_LEN = 12;         // S9(10)V99
        public static final int CREDIT_LIMIT = 24, CREDIT_LIMIT_LEN = 12;
        public static final int CASH_CREDIT_LIMIT = 36, CASH_CREDIT_LIMIT_LEN = 12;
        public static final int OPEN_DATE = 48, OPEN_DATE_LEN = 10;
        public static final int EXPIRAION_DATE = 58, EXPIRAION_DATE_LEN = 10;
        public static final int REISSUE_DATE = 68, REISSUE_DATE_LEN = 10;
        public static final int CURR_CYC_CREDIT = 78, CURR_CYC_CREDIT_LEN = 12;
        public static final int CURR_CYC_DEBIT = 90, CURR_CYC_DEBIT_LEN = 12;

        private Account() {
        }
    }

    /** app/cpy/CVTRA01Y.cpy — TRAN-CAT-BAL-RECORD, 50 bytes; KSDS key = TRAN-CAT-KEY (17). */
    public static final class TranCatBal {
        public static final int LEN = 50;
        public static final int KEY = 0, KEY_LEN = 17;
        public static final int ACCT_ID = 0, ACCT_ID_LEN = 11;
        public static final int TYPE_CD = 11, TYPE_CD_LEN = 2;
        public static final int CD = 13, CD_LEN = 4;
        public static final int BAL = 17, BAL_LEN = 11;                   // S9(09)V99
        public static final int FILLER = 28, FILLER_LEN = 22;

        private TranCatBal() {
        }
    }

    /** app/cbl/CBTRN02C.cbl:176-178, 180-182 — REJECT-RECORD = 350 data + 80 trailer (LRECL 430). */
    public static final class Reject {
        public static final int LEN = 430;
        public static final int TRAN_DATA = 0, TRAN_DATA_LEN = 350;
        public static final int TRAILER = 350, TRAILER_LEN = 80;
        public static final int FAIL_REASON = 350, FAIL_REASON_LEN = 4;
        public static final int FAIL_REASON_DESC = 354, FAIL_REASON_DESC_LEN = 76;

        private Reject() {
        }
    }
}
