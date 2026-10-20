      *****************************************************************
      * Test harness for CBACT04C under GnuCOBOL: loads the four
      * line-sequential ASCII files into the indexed files CBACT04C
      * opens (same SELECT keys as CBACT04C).
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. C4LOAD.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TCAT-TXT ASSIGN TO TCATTXT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT XREF-TXT ASSIGN TO XREFTXT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT ACCT-TXT ASSIGN TO ACCTTXT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT DISC-TXT ASSIGN TO DISCTXT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT TCATBAL-FILE ASSIGN TO TCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-TRAN-CAT-KEY
                  FILE STATUS  IS WS-STATUS.
           SELECT XREF-FILE ASSIGN TO XREFFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-XREF-CARD-NUM
                  ALTERNATE RECORD KEY IS FD-XREF-ACCT-ID
                  FILE STATUS  IS WS-STATUS.
           SELECT ACCOUNT-FILE ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-ACCT-ID
                  FILE STATUS  IS WS-STATUS.
           SELECT DISCGRP-FILE ASSIGN TO DISCGRP
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-DISCGRP-KEY
                  FILE STATUS  IS WS-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  TCAT-TXT.
       01  TCAT-TXT-REC                  PIC X(50).
       FD  XREF-TXT.
       01  XREF-TXT-REC                  PIC X(50).
       FD  ACCT-TXT.
       01  ACCT-TXT-REC                  PIC X(300).
       FD  DISC-TXT.
       01  DISC-TXT-REC                  PIC X(50).
       FD  TCATBAL-FILE.
       01  FD-TRAN-CAT-BAL-RECORD.
           05 FD-TRAN-CAT-KEY            PIC X(17).
           05 FILLER                     PIC X(33).
       FD  XREF-FILE.
       01  FD-XREFFILE-REC.
           05 FD-XREF-CARD-NUM           PIC X(16).
           05 FILLER                     PIC X(09).
           05 FD-XREF-ACCT-ID            PIC 9(11).
           05 FILLER                     PIC X(14).
       FD  ACCOUNT-FILE.
       01  FD-ACCTFILE-REC.
           05 FD-ACCT-ID                 PIC 9(11).
           05 FILLER                     PIC X(289).
       FD  DISCGRP-FILE.
       01  FD-DISCGRP-REC.
           05 FD-DISCGRP-KEY             PIC X(16).
           05 FILLER                     PIC X(34).
       WORKING-STORAGE SECTION.
       01  WS-STATUS                     PIC XX.
       01  WS-EOF                        PIC X.
       PROCEDURE DIVISION.
           OPEN INPUT TCAT-TXT OUTPUT TCATBAL-FILE
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
              READ TCAT-TXT AT END MOVE 'Y' TO WS-EOF
              NOT AT END WRITE FD-TRAN-CAT-BAL-RECORD FROM TCAT-TXT-REC
                         PERFORM CHECK-STATUS
              END-READ
           END-PERFORM
           CLOSE TCAT-TXT TCATBAL-FILE
           OPEN INPUT XREF-TXT OUTPUT XREF-FILE
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
              READ XREF-TXT AT END MOVE 'Y' TO WS-EOF
              NOT AT END WRITE FD-XREFFILE-REC FROM XREF-TXT-REC
                         PERFORM CHECK-STATUS
              END-READ
           END-PERFORM
           CLOSE XREF-TXT XREF-FILE
           OPEN INPUT ACCT-TXT OUTPUT ACCOUNT-FILE
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
              READ ACCT-TXT AT END MOVE 'Y' TO WS-EOF
              NOT AT END WRITE FD-ACCTFILE-REC FROM ACCT-TXT-REC
                         PERFORM CHECK-STATUS
              END-READ
           END-PERFORM
           CLOSE ACCT-TXT ACCOUNT-FILE
           OPEN INPUT DISC-TXT OUTPUT DISCGRP-FILE
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
              READ DISC-TXT AT END MOVE 'Y' TO WS-EOF
              NOT AT END WRITE FD-DISCGRP-REC FROM DISC-TXT-REC
                         PERFORM CHECK-STATUS
              END-READ
           END-PERFORM
           CLOSE DISC-TXT DISCGRP-FILE
           GOBACK.
       CHECK-STATUS.
           IF WS-STATUS NOT = '00'
              DISPLAY 'C4LOAD WRITE FAILED, STATUS ' WS-STATUS
              MOVE 12 TO RETURN-CODE
              STOP RUN
           END-IF.
