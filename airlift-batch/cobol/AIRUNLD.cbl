      *****************************************************************
      * AIRUNLD - unload a GnuCOBOL indexed file to a fixed-width flat
      * file in primary-key order, standing in for the IDCAMS REPRO
      * backups the estate uses to snapshot a KSDS (for example
      * TRANBKP.jcl via app/proc/REPROC.prc). Harness code.
      *
      *   AIRUNLD <ACCT|XREF|DISC|TCAT|TRAN>
      *   DD_INFILE  - input indexed file
      *   DD_OUTFILE - output sequential file
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. AIRUNLD.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT ACCT-IN  ASSIGN TO INFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS ACCT-IN-KEY
                  FILE STATUS  IS WS-STATUS.
           SELECT ACCT-OUT ASSIGN TO OUTFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT XREF-IN  ASSIGN TO INFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS XREF-IN-CARD
                  ALTERNATE RECORD KEY IS XREF-IN-ACCT
                  FILE STATUS  IS WS-STATUS.
           SELECT XREF-OUT ASSIGN TO OUTFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT DISC-IN  ASSIGN TO INFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS DISC-IN-KEY
                  FILE STATUS  IS WS-STATUS.
           SELECT DISC-OUT ASSIGN TO OUTFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT TCAT-IN  ASSIGN TO INFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS TCAT-IN-KEY
                  FILE STATUS  IS WS-STATUS.
           SELECT TCAT-OUT ASSIGN TO OUTFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT TRAN-IN  ASSIGN TO INFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS TRAN-IN-KEY
                  FILE STATUS  IS WS-STATUS.
           SELECT TRAN-OUT ASSIGN TO OUTFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  ACCT-IN.
       01  ACCT-IN-REC.
           05  ACCT-IN-KEY             PIC X(11).
           05  ACCT-IN-DATA            PIC X(289).
       FD  ACCT-OUT.
       01  ACCT-OUT-REC                PIC X(300).
       FD  XREF-IN.
       01  XREF-IN-REC.
           05  XREF-IN-CARD            PIC X(16).
           05  XREF-IN-CUST            PIC X(09).
           05  XREF-IN-ACCT            PIC X(11).
           05  XREF-IN-DATA            PIC X(14).
       FD  XREF-OUT.
       01  XREF-OUT-REC                PIC X(50).
       FD  DISC-IN.
       01  DISC-IN-REC.
           05  DISC-IN-KEY             PIC X(16).
           05  DISC-IN-DATA            PIC X(34).
       FD  DISC-OUT.
       01  DISC-OUT-REC                PIC X(50).
       FD  TCAT-IN.
       01  TCAT-IN-REC.
           05  TCAT-IN-KEY             PIC X(17).
           05  TCAT-IN-DATA            PIC X(33).
       FD  TCAT-OUT.
       01  TCAT-OUT-REC                PIC X(50).
       FD  TRAN-IN.
       01  TRAN-IN-REC.
           05  TRAN-IN-KEY             PIC X(16).
           05  TRAN-IN-DATA            PIC X(334).
       FD  TRAN-OUT.
       01  TRAN-OUT-REC                PIC X(350).
       WORKING-STORAGE SECTION.
       01  WS-STATUS                   PIC X(02).
       01  WS-KIND                     PIC X(08) VALUE SPACES.
       01  WS-COUNT                    PIC 9(09) VALUE 0.
       01  WS-EOF                      PIC X(01) VALUE 'N'.
       PROCEDURE DIVISION.
           ACCEPT WS-KIND FROM COMMAND-LINE
           EVALUATE WS-KIND
             WHEN 'ACCT'    PERFORM UNLOAD-ACCT
             WHEN 'XREF'    PERFORM UNLOAD-XREF
             WHEN 'DISC'    PERFORM UNLOAD-DISC
             WHEN 'TCAT'    PERFORM UNLOAD-TCAT
             WHEN 'TRAN'    PERFORM UNLOAD-TRAN
             WHEN OTHER
               DISPLAY 'AIRUNLD: UNKNOWN FILE KIND ' WS-KIND
               MOVE 16 TO RETURN-CODE
               STOP RUN
           END-EVALUATE
           DISPLAY 'AIRUNLD ' WS-KIND ' RECORDS UNLOADED: ' WS-COUNT
           GOBACK.

       CHECK-STATUS.
           IF WS-STATUS NOT = '00'
              DISPLAY 'AIRUNLD ' WS-KIND ' FILE STATUS ' WS-STATUS
              MOVE 12 TO RETURN-CODE
              STOP RUN
           END-IF
           EXIT.

       UNLOAD-ACCT.
           OPEN INPUT ACCT-IN
           PERFORM CHECK-STATUS
           OPEN OUTPUT ACCT-OUT
           PERFORM CHECK-STATUS
           PERFORM UNTIL WS-EOF = 'Y'
             READ ACCT-IN NEXT RECORD
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE ACCT-OUT-REC FROM ACCT-IN-REC
                 ADD 1 TO WS-COUNT
             END-READ
           END-PERFORM
           CLOSE ACCT-IN
           CLOSE ACCT-OUT
           EXIT.

       UNLOAD-XREF.
           OPEN INPUT XREF-IN
           PERFORM CHECK-STATUS
           OPEN OUTPUT XREF-OUT
           PERFORM CHECK-STATUS
           PERFORM UNTIL WS-EOF = 'Y'
             READ XREF-IN NEXT RECORD
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE XREF-OUT-REC FROM XREF-IN-REC
                 ADD 1 TO WS-COUNT
             END-READ
           END-PERFORM
           CLOSE XREF-IN
           CLOSE XREF-OUT
           EXIT.

       UNLOAD-DISC.
           OPEN INPUT DISC-IN
           PERFORM CHECK-STATUS
           OPEN OUTPUT DISC-OUT
           PERFORM CHECK-STATUS
           PERFORM UNTIL WS-EOF = 'Y'
             READ DISC-IN NEXT RECORD
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE DISC-OUT-REC FROM DISC-IN-REC
                 ADD 1 TO WS-COUNT
             END-READ
           END-PERFORM
           CLOSE DISC-IN
           CLOSE DISC-OUT
           EXIT.

       UNLOAD-TCAT.
           OPEN INPUT TCAT-IN
           PERFORM CHECK-STATUS
           OPEN OUTPUT TCAT-OUT
           PERFORM CHECK-STATUS
           PERFORM UNTIL WS-EOF = 'Y'
             READ TCAT-IN NEXT RECORD
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE TCAT-OUT-REC FROM TCAT-IN-REC
                 ADD 1 TO WS-COUNT
             END-READ
           END-PERFORM
           CLOSE TCAT-IN
           CLOSE TCAT-OUT
           EXIT.

       UNLOAD-TRAN.
           OPEN INPUT TRAN-IN
           PERFORM CHECK-STATUS
           OPEN OUTPUT TRAN-OUT
           PERFORM CHECK-STATUS
           PERFORM UNTIL WS-EOF = 'Y'
             READ TRAN-IN NEXT RECORD
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE TRAN-OUT-REC FROM TRAN-IN-REC
                 ADD 1 TO WS-COUNT
             END-READ
           END-PERFORM
           CLOSE TRAN-IN
           CLOSE TRAN-OUT
           EXIT.
