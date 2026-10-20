      *****************************************************************
      * Test harness for CBACT04C under GnuCOBOL: unloads ACCTFILE in
      * key order to 300-byte fixed records (no trailing-space strip).
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. C4UNLOAD.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT ACCOUNT-FILE ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-ACCT-ID
                  FILE STATUS  IS WS-STATUS.
           SELECT ACCT-OUT ASSIGN TO ACCTOUT
                  ORGANIZATION IS SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD  ACCOUNT-FILE.
       01  FD-ACCTFILE-REC.
           05 FD-ACCT-ID                 PIC 9(11).
           05 FILLER                     PIC X(289).
       FD  ACCT-OUT.
       01  ACCT-OUT-REC                  PIC X(300).
       WORKING-STORAGE SECTION.
       01  WS-STATUS                     PIC XX.
       PROCEDURE DIVISION.
           OPEN INPUT ACCOUNT-FILE OUTPUT ACCT-OUT
           PERFORM UNTIL WS-STATUS = '10'
              READ ACCOUNT-FILE
                 AT END CONTINUE
                 NOT AT END WRITE ACCT-OUT-REC FROM FD-ACCTFILE-REC
              END-READ
           END-PERFORM
           CLOSE ACCOUNT-FILE ACCT-OUT
           GOBACK.
