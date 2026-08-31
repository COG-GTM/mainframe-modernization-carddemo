      *****************************************************************
      * AIRLOAD - load a fixed-width flat file into a GnuCOBOL indexed
      * file, standing in for the IDCAMS REPRO steps that load the VSAM
      * KSDS clusters on the mainframe (for example ACCTFILE.jcl STEP15
      * and XREFFILE.jcl). Harness code: nothing under app/ is changed.
      *
      *   AIRLOAD <ACCT|XREF|DISC|TCAT>
      *   DD_INFILE  - input sequential file
      *   DD_OUTFILE - output indexed file
      *
      * Keys are declared as PIC X of the same length as the numeric or
      * alphanumeric key in the application program: the indexed handler
      * compares key bytes, and the loader does no arithmetic on them.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. AIRLOAD.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT ACCT-IN  ASSIGN TO INFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT ACCT-OUT ASSIGN TO OUTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS ACCT-OUT-KEY
                  FILE STATUS  IS WS-STATUS.
           SELECT XREF-IN  ASSIGN TO INFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT XREF-OUT ASSIGN TO OUTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS XREF-OUT-CARD
                  ALTERNATE RECORD KEY IS XREF-OUT-ACCT
                  FILE STATUS  IS WS-STATUS.
           SELECT DISC-IN  ASSIGN TO INFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT DISC-OUT ASSIGN TO OUTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS DISC-OUT-KEY
                  FILE STATUS  IS WS-STATUS.
           SELECT TCAT-IN  ASSIGN TO INFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT TCAT-OUT ASSIGN TO OUTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS TCAT-OUT-KEY
                  FILE STATUS  IS WS-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  ACCT-IN.
       01  ACCT-IN-REC                 PIC X(300).
       FD  ACCT-OUT.
       01  ACCT-OUT-REC.
           05  ACCT-OUT-KEY            PIC X(11).
           05  ACCT-OUT-DATA           PIC X(289).
       FD  XREF-IN.
       01  XREF-IN-REC                 PIC X(50).
       FD  XREF-OUT.
       01  XREF-OUT-REC.
           05  XREF-OUT-CARD           PIC X(16).
           05  XREF-OUT-CUST           PIC X(09).
           05  XREF-OUT-ACCT           PIC X(11).
           05  XREF-OUT-DATA           PIC X(14).
       FD  DISC-IN.
       01  DISC-IN-REC                 PIC X(50).
       FD  DISC-OUT.
       01  DISC-OUT-REC.
           05  DISC-OUT-KEY            PIC X(16).
           05  DISC-OUT-DATA           PIC X(34).
       FD  TCAT-IN.
       01  TCAT-IN-REC                 PIC X(50).
       FD  TCAT-OUT.
       01  TCAT-OUT-REC.
           05  TCAT-OUT-KEY            PIC X(17).
           05  TCAT-OUT-DATA           PIC X(33).
       WORKING-STORAGE SECTION.
       01  WS-STATUS                   PIC X(02).
       01  WS-KIND                     PIC X(08) VALUE SPACES.
       01  WS-COUNT                    PIC 9(09) VALUE 0.
       01  WS-EOF                      PIC X(01) VALUE 'N'.
       PROCEDURE DIVISION.
           ACCEPT WS-KIND FROM COMMAND-LINE
           EVALUATE WS-KIND
             WHEN 'ACCT'    PERFORM LOAD-ACCT
             WHEN 'XREF'    PERFORM LOAD-XREF
             WHEN 'DISC'    PERFORM LOAD-DISC
             WHEN 'TCAT'    PERFORM LOAD-TCAT
             WHEN OTHER
               DISPLAY 'AIRLOAD: UNKNOWN FILE KIND ' WS-KIND
               MOVE 16 TO RETURN-CODE
               STOP RUN
           END-EVALUATE
           DISPLAY 'AIRLOAD ' WS-KIND ' RECORDS LOADED: ' WS-COUNT
           GOBACK.

       CHECK-STATUS.
           IF WS-STATUS NOT = '00'
              DISPLAY 'AIRLOAD ' WS-KIND ' FILE STATUS ' WS-STATUS
              MOVE 12 TO RETURN-CODE
              STOP RUN
           END-IF
           EXIT.

       LOAD-ACCT.
           OPEN INPUT ACCT-IN
           PERFORM CHECK-STATUS
           OPEN OUTPUT ACCT-OUT
           PERFORM CHECK-STATUS
           PERFORM UNTIL WS-EOF = 'Y'
             READ ACCT-IN INTO ACCT-OUT-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE ACCT-OUT-REC
                 PERFORM CHECK-STATUS
                 ADD 1 TO WS-COUNT
             END-READ
           END-PERFORM
           CLOSE ACCT-IN
           CLOSE ACCT-OUT
           EXIT.

       LOAD-XREF.
           OPEN INPUT XREF-IN
           PERFORM CHECK-STATUS
           OPEN OUTPUT XREF-OUT
           PERFORM CHECK-STATUS
           PERFORM UNTIL WS-EOF = 'Y'
             READ XREF-IN INTO XREF-OUT-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE XREF-OUT-REC
                 PERFORM CHECK-STATUS
                 ADD 1 TO WS-COUNT
             END-READ
           END-PERFORM
           CLOSE XREF-IN
           CLOSE XREF-OUT
           EXIT.

       LOAD-DISC.
           OPEN INPUT DISC-IN
           PERFORM CHECK-STATUS
           OPEN OUTPUT DISC-OUT
           PERFORM CHECK-STATUS
           PERFORM UNTIL WS-EOF = 'Y'
             READ DISC-IN INTO DISC-OUT-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE DISC-OUT-REC
                 PERFORM CHECK-STATUS
                 ADD 1 TO WS-COUNT
             END-READ
           END-PERFORM
           CLOSE DISC-IN
           CLOSE DISC-OUT
           EXIT.

       LOAD-TCAT.
           OPEN INPUT TCAT-IN
           PERFORM CHECK-STATUS
           OPEN OUTPUT TCAT-OUT
           PERFORM CHECK-STATUS
           PERFORM UNTIL WS-EOF = 'Y'
             READ TCAT-IN INTO TCAT-OUT-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE TCAT-OUT-REC
                 PERFORM CHECK-STATUS
                 ADD 1 TO WS-COUNT
             END-READ
           END-PERFORM
           CLOSE TCAT-IN
           CLOSE TCAT-OUT
           EXIT.
