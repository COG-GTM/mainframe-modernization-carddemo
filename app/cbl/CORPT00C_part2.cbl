         05 FILLER                     PIC X(80) VALUE
         "TRAN-PROC-DT,305,10,CH".
         05 FILLER-1.
            10 FILLER                  PIC X(18) VALUE
         "PARM-START-DATE,C'".
            10 PARM-START-DATE-1       PIC X(10) VALUE SPACES.
            10 FILLER                  PIC X(52) VALUE "'".
         05 FILLER-2.
            10 FILLER                  PIC X(16) VALUE
         "PARM-END-DATE,C'".
            10 PARM-END-DATE-1         PIC X(10) VALUE SPACES.
            10 FILLER                  PIC X(54) VALUE "'".
         05 FILLER                     PIC X(80) VALUE
         "/*".
         05 FILLER                     PIC X(80) VALUE
         "//STEP10R.DATEPARM DD *".
         05 FILLER-3.
            10 PARM-START-DATE-2       PIC X(10) VALUE SPACES.
            10 FILLER                  PIC X VALUE SPACE.
            10 PARM-END-DATE-2         PIC X(10) VALUE SPACES.
            10 FILLER                  PIC X(59) VALUE SPACES.
         05 FILLER                     PIC X(80) VALUE
         "/*".
         05 FILLER                     PIC X(80) VALUE
         "/*EOF".
        02 JOB-DATA-2 REDEFINES JOB-DATA-1.
         05 JOB-LINES OCCURS 1000 TIMES PIC X(80).

       01 CSUTLDTC-PARM.
          05 CSUTLDTC-DATE                   PIC X(10).
          05 CSUTLDTC-DATE-FORMAT            PIC X(10).
          05 CSUTLDTC-RESULT.
             10 CSUTLDTC-RESULT-SEV-CD       PIC X(04).
             10 FILLER                       PIC X(11).
             10 CSUTLDTC-RESULT-MSG-NUM      PIC X(04).
             10 CSUTLDTC-RESULT-MSG          PIC X(61).

       COPY COCOM01Y.

       COPY CORPT00.

       COPY COTTL01Y.
       COPY CSDAT01Y.
       COPY CSMSG01Y.

       COPY CVTRA05Y.

       COPY DFHAID.
       COPY DFHBMSCA.

      *----------------------------------------------------------------*
      *                        LINKAGE SECTION
      *----------------------------------------------------------------*
       LINKAGE SECTION.
       01  DFHCOMMAREA.
         05  LK-COMMAREA                           PIC X(01)
             OCCURS 1 TO 32767 TIMES DEPENDING ON EIBCALEN.

      *----------------------------------------------------------------*
      *                       PROCEDURE DIVISION
      *----------------------------------------------------------------*
       PROCEDURE DIVISION.
       MAIN-PARA.

           SET ERR-FLG-OFF TO TRUE
           SET TRANSACT-NOT-EOF TO TRUE
           SET SEND-ERASE-YES TO TRUE

           MOVE SPACES TO WS-MESSAGE
                          ERRMSGO OF CORPT0AO

           IF EIBCALEN = 0
               MOVE 'COSGN00C' TO CDEMO-TO-PROGRAM
               PERFORM RETURN-TO-PREV-SCREEN
           ELSE
               MOVE DFHCOMMAREA(1:EIBCALEN) TO CARDDEMO-COMMAREA
               IF NOT CDEMO-PGM-REENTER
                   SET CDEMO-PGM-REENTER    TO TRUE
                   MOVE LOW-VALUES          TO CORPT0AO
                   MOVE -1       TO MONTHLYL OF CORPT0AI
                   PERFORM SEND-TRNRPT-SCREEN
               ELSE
                   PERFORM RECEIVE-TRNRPT-SCREEN
                   EVALUATE EIBAID
                       WHEN DFHENTER
                           PERFORM PROCESS-ENTER-KEY
                       WHEN DFHPF3
                           MOVE 'COMEN01C' TO CDEMO-TO-PROGRAM
                           PERFORM RETURN-TO-PREV-SCREEN
                       WHEN OTHER
                           MOVE 'Y'                       TO WS-ERR-FLG
                           MOVE -1       TO MONTHLYL OF CORPT0AI
                           MOVE CCDA-MSG-INVALID-KEY      TO WS-MESSAGE
                           PERFORM SEND-TRNRPT-SCREEN
                   END-EVALUATE
               END-IF
           END-IF

           EXEC CICS RETURN
                     TRANSID (WS-TRANID)
