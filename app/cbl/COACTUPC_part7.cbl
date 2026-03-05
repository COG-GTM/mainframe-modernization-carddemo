      *North America Phone Area codes
       COPY CSLKPCDY.

      ******************************************************************
      * Variables for use in INSPECT statements
      ******************************************************************
       01  LIT-ALL-ALPHA-FROM     PIC X(52) VALUE SPACES.
       01  LIT-ALL-ALPHANUM-FROM  PIC X(62) VALUE SPACES.
       01  LIT-ALL-NUM-FROM       PIC X(10) VALUE SPACES.
       77  LIT-ALPHA-SPACES-TO    PIC X(52) VALUE SPACES.
       77  LIT-ALPHANUM-SPACES-TO PIC X(62) VALUE SPACES.
       77  LIT-NUM-SPACES-TO      PIC X(10) VALUE SPACES.

      *IBM SUPPLIED COPYBOOKS
       COPY DFHBMSCA.
       COPY DFHAID.

      *COMMON COPYBOOKS
      *Screen Titles
       COPY COTTL01Y.

      *Account Update Screen Layout
       COPY COACTUP.

      *Current Date
       COPY CSDAT01Y.

      *Common Messages
       COPY CSMSG01Y.

      *Abend Variables
       COPY CSMSG02Y.

      *Signed on user data
       COPY CSUSR01Y.

      *Dataset layouts

      *ACCT RECORD LAYOUT
       COPY CVACT01Y.

      *CARD XREF LAYOUT
       COPY CVACT03Y.

      *CUSTOMER LAYOUT
       COPY CVCUS01Y.

      ******************************************************************
      *Application Commmarea Copybook
       COPY COCOM01Y.

       01 WS-THIS-PROGCOMMAREA.
          05 ACCT-UPDATE-SCREEN-DATA.
             10 ACUP-CHANGE-ACTION                     PIC X(1)
                                                       VALUE LOW-VALUES.
                88 ACUP-DETAILS-NOT-FETCHED            VALUES
                                                       LOW-VALUES,
                                                       SPACES.
                88 ACUP-SHOW-DETAILS                   VALUE 'S'.
                88 ACUP-CHANGES-MADE                   VALUES 'E', 'N'
                                                            , 'C', 'L'
                                                            , 'F'.
                88 ACUP-CHANGES-NOT-OK                 VALUE 'E'.
                88 ACUP-CHANGES-OK-NOT-CONFIRMED       VALUE 'N'.
                88 ACUP-CHANGES-OKAYED-AND-DONE        VALUE 'C'.
                88 ACUP-CHANGES-FAILED                 VALUES 'L', 'F'.
                88 ACUP-CHANGES-OKAYED-LOCK-ERROR      VALUE 'L'.
                88 ACUP-CHANGES-OKAYED-BUT-FAILED      VALUE 'F'.
          05 ACUP-OLD-DETAILS.
             10 ACUP-OLD-ACCT-DATA.
                15  ACUP-OLD-ACCT-ID-X                 PIC X(11).
                15  ACUP-OLD-ACCT-ID                   REDEFINES
                    ACUP-OLD-ACCT-ID-X                 PIC 9(11).
                15  ACUP-OLD-ACTIVE-STATUS             PIC X(01).
                15  ACUP-OLD-CURR-BAL                  PIC X(12).
                15  ACUP-OLD-CURR-BAL-N REDEFINES
                    ACUP-OLD-CURR-BAL                  PIC S9(10)V99.
                15  ACUP-OLD-CREDIT-LIMIT              PIC X(12).
                15  ACUP-OLD-CREDIT-LIMIT-N            REDEFINES
                    ACUP-OLD-CREDIT-LIMIT              PIC S9(10)V99.
                15  ACUP-OLD-CASH-CREDIT-LIMIT         PIC X(12).
                15  ACUP-OLD-CASH-CREDIT-LIMIT-N       REDEFINES
                    ACUP-OLD-CASH-CREDIT-LIMIT         PIC S9(10)V99.
                15  ACUP-OLD-OPEN-DATE                 PIC X(08).
                15  ACUP-OLD-OPEN-DATE-PARTS           REDEFINES
                    ACUP-OLD-OPEN-DATE.
                    20 ACUP-OLD-OPEN-YEAR              PIC X(4).
                    20 ACUP-OLD-OPEN-MON               PIC X(2).
                    20 ACUP-OLD-OPEN-DAY               PIC X(2).
                15  ACUP-OLD-EXPIRAION-DATE            PIC X(08).
                15  ACUP-OLD-EXPIRAION-DATE-PARTS      REDEFINES
                    ACUP-OLD-EXPIRAION-DATE.
                    20 ACUP-OLD-EXP-YEAR                PIC X(4).
                    20 ACUP-OLD-EXP-MON                 PIC X(2).
                    20 ACUP-OLD-EXP-DAY                 PIC X(2).
                15  ACUP-OLD-REISSUE-DATE              PIC X(08).
                15  ACUP-OLD-REISSUE-DATE-PARTS        REDEFINES
                    ACUP-OLD-REISSUE-DATE.
                    20 ACUP-OLD-REISSUE-YEAR           PIC X(4).
                    20 ACUP-OLD-REISSUE-MON            PIC X(2).
