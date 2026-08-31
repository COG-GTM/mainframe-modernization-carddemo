      *****************************************************************
      * ARITHCHK - compiler evidence for the interest arithmetic.
      *
      * Repeats the statement at CBACT04C:464-465
      *     COMPUTE WS-MONTHLY-INT = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200
      * with the same field declarations as the program (CBACT04C:168
      * WS-MONTHLY-INT PIC S9(09)V99, CVTRA01Y TRAN-CAT-BAL PIC S9(09)V99,
      * CVTRA02Y DIS-INT-RATE PIC S9(04)V99) and, alongside it, the same
      * COMPUTE with ROUNDED. The two columns are what the truncation trap
      * in the Java mirror contradicts; the numbers in docs/ARITHMETIC.md
      * are this program's output, not an assumption.
      *
      *   DD_TEXTFILE - output line sequential report
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. ARITHCHK.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TEXT-FILE ASSIGN TO TEXTFILE
                  ORGANIZATION IS LINE SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  TEXT-FILE.
       01  TEXT-REC                    PIC X(80).
       WORKING-STORAGE SECTION.
       01  WS-STATUS                   PIC X(02).
       01  TRAN-CAT-BAL                PIC S9(09)V99.
       01  DIS-INT-RATE                PIC S9(04)V99.
       01  WS-MONTHLY-INT              PIC S9(09)V99.
       01  WS-ROUNDED-INT              PIC S9(09)V99.
       01  WS-CASE                     PIC 9(02) VALUE 0.
       01  WS-LINE.
           05  WL-CASE                 PIC 9(02).
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-BAL                  PIC +9(9).99.
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-RATE                 PIC +9(4).99.
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-TRUNCATED            PIC +9(9).99.
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-ROUNDED              PIC +9(9).99.
       PROCEDURE DIVISION.
           OPEN OUTPUT TEXT-FILE
           IF WS-STATUS NOT = '00'
              DISPLAY 'ARITHCHK: FILE STATUS ' WS-STATUS
              MOVE 12 TO RETURN-CODE
              STOP RUN
           END-IF

           MOVE 1000.00   TO TRAN-CAT-BAL
           MOVE 12.99     TO DIS-INT-RATE
           PERFORM EMIT-CASE

           MOVE 1234.56   TO TRAN-CAT-BAL
           MOVE 12.99     TO DIS-INT-RATE
           PERFORM EMIT-CASE

           MOVE 8901.23   TO TRAN-CAT-BAL
           MOVE 18.49     TO DIS-INT-RATE
           PERFORM EMIT-CASE

           MOVE 4567.89   TO TRAN-CAT-BAL
           MOVE 24.99     TO DIS-INT-RATE
           PERFORM EMIT-CASE

           MOVE -2500.55  TO TRAN-CAT-BAL
           MOVE 18.49     TO DIS-INT-RATE
           PERFORM EMIT-CASE

           MOVE 100.00    TO TRAN-CAT-BAL
           MOVE 0.00      TO DIS-INT-RATE
           PERFORM EMIT-CASE

           CLOSE TEXT-FILE
           DISPLAY 'ARITHCHK: CASES WRITTEN ' WS-CASE
           GOBACK.

       EMIT-CASE.
           ADD 1 TO WS-CASE
           COMPUTE WS-MONTHLY-INT
            = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200
           COMPUTE WS-ROUNDED-INT ROUNDED
            = ( TRAN-CAT-BAL * DIS-INT-RATE) / 1200
           MOVE WS-CASE         TO WL-CASE
           MOVE TRAN-CAT-BAL    TO WL-BAL
           MOVE DIS-INT-RATE    TO WL-RATE
           MOVE WS-MONTHLY-INT  TO WL-TRUNCATED
           MOVE WS-ROUNDED-INT  TO WL-ROUNDED
           MOVE WS-LINE         TO TEXT-REC
           WRITE TEXT-REC
           EXIT.
