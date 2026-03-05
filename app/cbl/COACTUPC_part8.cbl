                    20 ACUP-OLD-REISSUE-DAY            PIC X(2).
                15  ACUP-OLD-CURR-CYC-CREDIT           PIC X(12).
                15  ACUP-OLD-CURR-CYC-CREDIT-N         REDEFINES
                    ACUP-OLD-CURR-CYC-CREDIT           PIC S9(10)V99.
                15  ACUP-OLD-CURR-CYC-DEBIT            PIC X(12).
                15  ACUP-OLD-CURR-CYC-DEBIT-N          REDEFINES
                    ACUP-OLD-CURR-CYC-DEBIT            PIC S9(10)V99.
                15  ACUP-OLD-GROUP-ID                  PIC X(10).
             10 ACUP-OLD-CUST-DATA.
                15  ACUP-OLD-CUST-ID-X                 PIC X(09).
                15  ACUP-OLD-CUST-ID                   REDEFINES
                    ACUP-OLD-CUST-ID-X                 PIC 9(09).
                15  ACUP-OLD-CUST-FIRST-NAME           PIC X(25).
                15  ACUP-OLD-CUST-MIDDLE-NAME          PIC X(25).
                15  ACUP-OLD-CUST-LAST-NAME            PIC X(25).
                15  ACUP-OLD-CUST-ADDR-LINE-1          PIC X(50).
                15  ACUP-OLD-CUST-ADDR-LINE-2          PIC X(50).
                15  ACUP-OLD-CUST-ADDR-LINE-3          PIC X(50).
                15  ACUP-OLD-CUST-ADDR-STATE-CD        PIC X(02).
                15  ACUP-OLD-CUST-ADDR-COUNTRY-CD      PIC X(03).
                15  ACUP-OLD-CUST-ADDR-ZIP             PIC X(10).
                15  ACUP-OLD-CUST-PHONE-NUM-1          PIC X(15).
                15  ACUP-OLD-CUST-PHONE-NUM-1-X REDEFINES
                    ACUP-OLD-CUST-PHONE-NUM-1.
                    20 FILLER                          PIC X(1).
                    20 ACUP-OLD-CUST-PHONE-NUM-1A      PIC X(3).
                    20 FILLER                          PIC X(1).
                    20 ACUP-OLD-CUST-PHONE-NUM-1B      PIC X(3).
                    20 FILLER                          PIC X(1).
                    20 ACUP-OLD-CUST-PHONE-NUM-1C      PIC X(4).
                    20 FILLER                          PIC X(2).
                15  ACUP-OLD-CUST-PHONE-NUM-2          PIC X(15).
                15  ACUP-OLD-CUST-PHONE-NUM-2-X REDEFINES
                    ACUP-OLD-CUST-PHONE-NUM-2.
                    20 FILLER                          PIC X(1).
                    20 ACUP-OLD-CUST-PHONE-NUM-2A      PIC X(3).
                    20 FILLER                          PIC X(1).
                    20 ACUP-OLD-CUST-PHONE-NUM-2B      PIC X(3).
                    20 FILLER                          PIC X(1).
                    20 ACUP-OLD-CUST-PHONE-NUM-2C      PIC X(4).
                    20 FILLER                          PIC X(2).
                15  ACUP-OLD-CUST-SSN-X                PIC X(09).
                15  ACUP-OLD-CUST-SSN                  REDEFINES
                    ACUP-OLD-CUST-SSN-X                PIC 9(09).
                15  ACUP-OLD-CUST-GOVT-ISSUED-ID       PIC X(20).
                15  ACUP-OLD-CUST-DOB-YYYY-MM-DD       PIC X(08).
                15  ACUP-OLD-CUST-DOB-PARTS            REDEFINES
                    ACUP-OLD-CUST-DOB-YYYY-MM-DD.
                    20 ACUP-OLD-CUST-DOB-YEAR          PIC X(4).
                    20 ACUP-OLD-CUST-DOB-MON           PIC X(2).
                    20 ACUP-OLD-CUST-DOB-DAY           PIC X(2).
                15  ACUP-OLD-CUST-EFT-ACCOUNT-ID       PIC X(10).
                15  ACUP-OLD-CUST-PRI-HOLDER-IND       PIC X(01).
                15  ACUP-OLD-CUST-FICO-SCORE-X         PIC X(03).
                15  ACUP-OLD-CUST-FICO-SCORE           REDEFINES
                    ACUP-OLD-CUST-FICO-SCORE-X         PIC 9(03).
          05 ACUP-NEW-DETAILS.
             10 ACUP-NEW-ACCT-DATA.
                15  ACUP-NEW-ACCT-ID-X                 PIC X(11).
                15  ACUP-NEW-ACCT-ID                   REDEFINES
                    ACUP-NEW-ACCT-ID-X                 PIC 9(11).
                15  ACUP-NEW-ACTIVE-STATUS             PIC X(01).
                15  ACUP-NEW-CURR-BAL                  PIC X(12).
                15  ACUP-NEW-CURR-BAL-N                REDEFINES
                    ACUP-NEW-CURR-BAL                  PIC S9(10)V99.
                15  ACUP-NEW-CREDIT-LIMIT              PIC X(12).
                15  ACUP-NEW-CREDIT-LIMIT-N            REDEFINES
                    ACUP-NEW-CREDIT-LIMIT              PIC S9(10)V99.
                15  ACUP-NEW-CASH-CREDIT-LIMIT         PIC X(12).
                15  ACUP-NEW-CASH-CREDIT-LIMIT-N       REDEFINES
                    ACUP-NEW-CASH-CREDIT-LIMIT         PIC S9(10)V99.
                15  ACUP-NEW-OPEN-DATE                 PIC X(08).
                15  ACUP-NEW-OPEN-DATE-PARTS           REDEFINES
                    ACUP-NEW-OPEN-DATE.
                    20 ACUP-NEW-OPEN-YEAR              PIC X(4).
                    20 ACUP-NEW-OPEN-MON               PIC X(2).
                    20 ACUP-NEW-OPEN-DAY               PIC X(2).
                15  ACUP-NEW-EXPIRAION-DATE            PIC X(08).
                15  ACUP-NEW-EXPIRAION-DATE-PARTS      REDEFINES
                    ACUP-NEW-EXPIRAION-DATE.
                    20 ACUP-NEW-EXP-YEAR                PIC X(4).
                    20 ACUP-NEW-EXP-MON                 PIC X(2).
                    20 ACUP-NEW-EXP-DAY                 PIC X(2).
                15  ACUP-NEW-REISSUE-DATE              PIC X(08).
                15  ACUP-NEW-REISSUE-DATE-PARTS        REDEFINES
                    ACUP-NEW-REISSUE-DATE.
                    20 ACUP-NEW-REISSUE-YEAR           PIC X(4).
                    20 ACUP-NEW-REISSUE-MON            PIC X(2).
                    20 ACUP-NEW-REISSUE-DAY            PIC X(2).
                15  ACUP-NEW-CURR-CYC-CREDIT           PIC X(12).
                15  ACUP-NEW-CURR-CYC-CREDIT-N         REDEFINES
                    ACUP-NEW-CURR-CYC-CREDIT           PIC S9(10)V99.
                15  ACUP-NEW-CURR-CYC-DEBIT            PIC X(12).
                15  ACUP-NEW-CURR-CYC-DEBIT-N          REDEFINES
                    ACUP-NEW-CURR-CYC-DEBIT            PIC S9(10)V99.
                15  ACUP-NEW-GROUP-ID                  PIC X(10).
             10 ACUP-NEW-CUST-DATA.
                15  ACUP-NEW-CUST-ID-X                 PIC X(09).
                15  ACUP-NEW-CUST-ID                   REDEFINES
                    ACUP-NEW-CUST-ID-X                 PIC 9(09).
