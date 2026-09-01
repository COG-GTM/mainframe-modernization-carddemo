      ******************************************************************
      * Harness utility (NOT legacy source).
      * Loads the sequential seed extracts shipped in app/data into
      * GnuCOBOL INDEXED files that stand in for the VSAM KSDS clusters
      * used by POSTTRAN / CBTRN02C.
      * Key definitions taken from app/catlg/LISTCAT.txt:
      *   CARDXREF KEYLEN 16 RKP 0 LRECL 50   (line 365)
      *   ACCTDATA KEYLEN 11 RKP 0 LRECL 300  (line 22)
      *   TCATBALF KEYLEN 17 RKP 0 LRECL 50   (line 1334)
      *   TRANSACT KEYLEN 16 RKP 0 LRECL 350  (line 3555)
      * PARM selects which file to load.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. VSAMLOAD.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT SEQ-XREF ASSIGN TO SEQXREF
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS IS WS-ST.
           SELECT IDX-XREF ASSIGN TO XREFFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS IX-XREF-KEY
                  FILE STATUS IS WS-ST.

           SELECT SEQ-ACCT ASSIGN TO SEQACCT
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS IS WS-ST.
           SELECT IDX-ACCT ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS IX-ACCT-KEY
                  FILE STATUS IS WS-ST.

           SELECT SEQ-TCAT ASSIGN TO SEQTCAT
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS IS WS-ST.
           SELECT IDX-TCAT ASSIGN TO TCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS IX-TCAT-KEY
                  FILE STATUS IS WS-ST.

       DATA DIVISION.
       FILE SECTION.
       FD  SEQ-XREF.
       01  SQ-XREF-REC              PIC X(50).
       FD  IDX-XREF.
       01  IX-XREF-REC.
           05 IX-XREF-KEY           PIC X(16).
           05 FILLER                PIC X(34).
       FD  SEQ-ACCT.
       01  SQ-ACCT-REC              PIC X(300).
       FD  IDX-ACCT.
       01  IX-ACCT-REC.
           05 IX-ACCT-KEY           PIC X(11).
           05 FILLER                PIC X(289).
       FD  SEQ-TCAT.
       01  SQ-TCAT-REC              PIC X(50).
       FD  IDX-TCAT.
       01  IX-TCAT-REC.
           05 IX-TCAT-KEY           PIC X(17).
           05 FILLER                PIC X(33).

       WORKING-STORAGE SECTION.
       01  WS-ST                    PIC XX.
       01  WS-EOF                   PIC X VALUE 'N'.
       01  WS-CNT                   PIC 9(6) VALUE 0.
       01  WS-WHICH                 PIC X(8).

       PROCEDURE DIVISION.
           ACCEPT WS-WHICH FROM COMMAND-LINE
           EVALUATE WS-WHICH(1:4)
             WHEN 'XREF' PERFORM LOAD-XREF
             WHEN 'ACCT' PERFORM LOAD-ACCT
             WHEN 'TCAT' PERFORM LOAD-TCAT
             WHEN OTHER DISPLAY 'VSAMLOAD: BAD PARM ' WS-WHICH
                        MOVE 12 TO RETURN-CODE
           END-EVALUATE
           DISPLAY 'VSAMLOAD ' WS-WHICH(1:4) ' RECORDS=' WS-CNT
           GOBACK.

       LOAD-XREF.
           OPEN INPUT SEQ-XREF
           OPEN OUTPUT IDX-XREF
           PERFORM UNTIL WS-EOF = 'Y'
             READ SEQ-XREF INTO IX-XREF-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE IX-XREF-REC
                 ADD 1 TO WS-CNT
             END-READ
           END-PERFORM
           CLOSE SEQ-XREF IDX-XREF.

       LOAD-ACCT.
           OPEN INPUT SEQ-ACCT
           OPEN OUTPUT IDX-ACCT
           PERFORM UNTIL WS-EOF = 'Y'
             READ SEQ-ACCT INTO IX-ACCT-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE IX-ACCT-REC
                 ADD 1 TO WS-CNT
             END-READ
           END-PERFORM
           CLOSE SEQ-ACCT IDX-ACCT.

       LOAD-TCAT.
           OPEN INPUT SEQ-TCAT
           OPEN OUTPUT IDX-TCAT
           PERFORM UNTIL WS-EOF = 'Y'
             READ SEQ-TCAT INTO IX-TCAT-REC
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 WRITE IX-TCAT-REC
                 ADD 1 TO WS-CNT
             END-READ
           END-PERFORM
           CLOSE SEQ-TCAT IDX-TCAT.
