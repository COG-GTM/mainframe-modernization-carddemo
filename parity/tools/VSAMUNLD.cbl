      ******************************************************************
      * Harness utility (NOT legacy source).
      * Unloads the GnuCOBOL INDEXED stand-ins for the VSAM KSDS files
      * updated by CBTRN02C into flat, key-ordered sequential files so
      * the legacy and migrated after-images can be byte-compared.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. VSAMUNLD.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IDX-ACCT ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS IX-ACCT-KEY
                  FILE STATUS IS WS-ST.
           SELECT SEQ-ACCT ASSIGN TO SEQACCT
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS IS WS-ST.

           SELECT IDX-TCAT ASSIGN TO TCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS IX-TCAT-KEY
                  FILE STATUS IS WS-ST.
           SELECT SEQ-TCAT ASSIGN TO SEQTCAT
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS IS WS-ST.

           SELECT IDX-TRAN ASSIGN TO TRANFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS IX-TRAN-KEY
                  FILE STATUS IS WS-ST.
           SELECT SEQ-TRAN ASSIGN TO SEQTRAN
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS IS WS-ST.

       DATA DIVISION.
       FILE SECTION.
       FD  IDX-ACCT.
       01  IX-ACCT-REC.
           05 IX-ACCT-KEY           PIC X(11).
           05 FILLER                PIC X(289).
       FD  SEQ-ACCT.
       01  SQ-ACCT-REC              PIC X(300).
       FD  IDX-TCAT.
       01  IX-TCAT-REC.
           05 IX-TCAT-KEY           PIC X(17).
           05 FILLER                PIC X(33).
       FD  SEQ-TCAT.
       01  SQ-TCAT-REC              PIC X(50).
       FD  IDX-TRAN.
       01  IX-TRAN-REC.
           05 IX-TRAN-KEY           PIC X(16).
           05 FILLER                PIC X(334).
       FD  SEQ-TRAN.
       01  SQ-TRAN-REC              PIC X(350).

       WORKING-STORAGE SECTION.
       01  WS-ST                    PIC XX.
       01  WS-EOF                   PIC X VALUE 'N'.
       01  WS-CNT                   PIC 9(6) VALUE 0.
       01  WS-WHICH                 PIC X(8).

       PROCEDURE DIVISION.
           ACCEPT WS-WHICH FROM COMMAND-LINE
           EVALUATE WS-WHICH(1:4)
             WHEN 'ACCT' PERFORM UNLD-ACCT
             WHEN 'TCAT' PERFORM UNLD-TCAT
             WHEN 'TRAN' PERFORM UNLD-TRAN
             WHEN OTHER DISPLAY 'VSAMUNLD: BAD PARM ' WS-WHICH
                        MOVE 12 TO RETURN-CODE
           END-EVALUATE
           DISPLAY 'VSAMUNLD ' WS-WHICH(1:4) ' RECORDS=' WS-CNT
           GOBACK.

       UNLD-ACCT.
           OPEN INPUT IDX-ACCT
           OPEN OUTPUT SEQ-ACCT
           PERFORM UNTIL WS-EOF = 'Y'
             READ IDX-ACCT INTO SQ-ACCT-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END WRITE SQ-ACCT-REC
                          ADD 1 TO WS-CNT
             END-READ
           END-PERFORM
           CLOSE IDX-ACCT SEQ-ACCT.

       UNLD-TCAT.
           OPEN INPUT IDX-TCAT
           OPEN OUTPUT SEQ-TCAT
           PERFORM UNTIL WS-EOF = 'Y'
             READ IDX-TCAT INTO SQ-TCAT-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END WRITE SQ-TCAT-REC
                          ADD 1 TO WS-CNT
             END-READ
           END-PERFORM
           CLOSE IDX-TCAT SEQ-TCAT.

       UNLD-TRAN.
           OPEN INPUT IDX-TRAN
           OPEN OUTPUT SEQ-TRAN
           PERFORM UNTIL WS-EOF = 'Y'
             READ IDX-TRAN INTO SQ-TRAN-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END WRITE SQ-TRAN-REC
                          ADD 1 TO WS-CNT
             END-READ
           END-PERFORM
           CLOSE IDX-TRAN SEQ-TRAN.
