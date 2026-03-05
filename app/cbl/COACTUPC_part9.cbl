                15  ACUP-NEW-CUST-FIRST-NAME           PIC X(25).
                15  ACUP-NEW-CUST-MIDDLE-NAME          PIC X(25).
                15  ACUP-NEW-CUST-LAST-NAME            PIC X(25).
                15  ACUP-NEW-CUST-ADDR-LINE-1          PIC X(50).
                15  ACUP-NEW-CUST-ADDR-LINE-2          PIC X(50).
                15  ACUP-NEW-CUST-ADDR-LINE-3          PIC X(50).
                15  ACUP-NEW-CUST-ADDR-STATE-CD        PIC X(02).
                15  ACUP-NEW-CUST-ADDR-COUNTRY-CD      PIC X(03).
                15  ACUP-NEW-CUST-ADDR-ZIP             PIC X(10).
                15  ACUP-NEW-CUST-PHONE-NUM-1          PIC X(15).
                15  ACUP-NEW-CUST-PHONE-NUM-1-X REDEFINES
                    ACUP-NEW-CUST-PHONE-NUM-1.
                    20 FILLER                          PIC X(1).
                    20 ACUP-NEW-CUST-PHONE-NUM-1A      PIC X(3).
                    20 FILLER                          PIC X(1).
                    20 ACUP-NEW-CUST-PHONE-NUM-1B      PIC X(3).
                    20 FILLER                          PIC X(1).
                    20 ACUP-NEW-CUST-PHONE-NUM-1C      PIC X(4).
                    20 FILLER                          PIC X(2).
                15  ACUP-NEW-CUST-PHONE-NUM-2          PIC X(15).
                15  ACUP-NEW-CUST-PHONE-NUM-2-X REDEFINES
                    ACUP-NEW-CUST-PHONE-NUM-2.
                    20 FILLER                          PIC X(1).
                    20 ACUP-NEW-CUST-PHONE-NUM-2A      PIC X(3).
                    20 FILLER                          PIC X(1).
                    20 ACUP-NEW-CUST-PHONE-NUM-2B      PIC X(3).
                    20 FILLER                          PIC X(1).
                    20 ACUP-NEW-CUST-PHONE-NUM-2C      PIC X(4).
                    20 FILLER                          PIC X(2).
                15  ACUP-NEW-CUST-SSN-X.
                    20 ACUP-NEW-CUST-SSN-1             PIC X(03).
                    20 ACUP-NEW-CUST-SSN-2             PIC X(02).
                    20 ACUP-NEW-CUST-SSN-3             PIC X(04).
                15  ACUP-NEW-CUST-SSN                  REDEFINES
                    ACUP-NEW-CUST-SSN-X                PIC 9(09).
                15  ACUP-NEW-CUST-GOVT-ISSUED-ID       PIC X(20).
                15  ACUP-NEW-CUST-DOB-YYYY-MM-DD       PIC X(08).
                15  ACUP-NEW-CUST-DOB-PARTS            REDEFINES
                    ACUP-NEW-CUST-DOB-YYYY-MM-DD.
                    20 ACUP-NEW-CUST-DOB-YEAR          PIC X(4).
                    20 ACUP-NEW-CUST-DOB-MON           PIC X(2).
                    20 ACUP-NEW-CUST-DOB-DAY           PIC X(2).
                15  ACUP-NEW-CUST-EFT-ACCOUNT-ID       PIC X(10).
                15  ACUP-NEW-CUST-PRI-HOLDER-IND       PIC X(01).
                15  ACUP-NEW-CUST-FICO-SCORE-X         PIC X(03).
                15  ACUP-NEW-CUST-FICO-SCORE           REDEFINES
                    ACUP-NEW-CUST-FICO-SCORE-X         PIC 9(03).
                    88 FICO-RANGE-IS-VALID             VALUES 300
                                                       THROUGH 850.
       01  WS-COMMAREA                                 PIC X(2000).


       LINKAGE SECTION.
       01  DFHCOMMAREA.
         05  FILLER                                PIC X(1)
             OCCURS 1 TO 32767 TIMES DEPENDING ON EIBCALEN.

       PROCEDURE DIVISION.
       0000-MAIN.


           EXEC CICS HANDLE ABEND
                     LABEL(ABEND-ROUTINE)
           END-EXEC

           INITIALIZE CC-WORK-AREA
                      WS-MISC-STORAGE
                      WS-COMMAREA
      *****************************************************************
      * Store our context
      *****************************************************************
           MOVE LIT-THISTRANID       TO WS-TRANID
      *****************************************************************
      * Ensure error message is cleared                               *
      *****************************************************************
           SET WS-RETURN-MSG-OFF  TO TRUE
      *****************************************************************
      * Store passed data if  any                *
      *****************************************************************
           IF EIBCALEN IS EQUAL TO 0
               OR (CDEMO-FROM-PROGRAM = LIT-MENUPGM
               AND NOT CDEMO-PGM-REENTER)
              INITIALIZE CARDDEMO-COMMAREA
                         WS-THIS-PROGCOMMAREA
              SET CDEMO-PGM-ENTER TO TRUE
              SET ACUP-DETAILS-NOT-FETCHED TO TRUE
           ELSE
              MOVE DFHCOMMAREA (1:LENGTH OF CARDDEMO-COMMAREA)  TO
                                CARDDEMO-COMMAREA
              MOVE DFHCOMMAREA(LENGTH OF CARDDEMO-COMMAREA + 1:
                               LENGTH OF WS-THIS-PROGCOMMAREA ) TO
                                WS-THIS-PROGCOMMAREA
           END-IF
      *****************************************************************
      * Remap PFkeys as needed.
      * Store the Mapped PF Key
      *****************************************************************
           PERFORM YYYY-STORE-PFKEY
              THRU YYYY-STORE-PFKEY-EXIT
      *****************************************************************
