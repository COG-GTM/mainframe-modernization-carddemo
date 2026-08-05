       IDENTIFICATION DIVISION.
       PROGRAM-ID. LOADACCT.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IN-FILE ASSIGN TO INFILE
                  ORGANIZATION IS LINE SEQUENTIAL
                  FILE STATUS IS IN-STATUS.
           SELECT OUT-FILE ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS FD-ACCT-ID
                  FILE STATUS IS OUT-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD IN-FILE.
       01 IN-REC PIC X(300).
       FD OUT-FILE.
       01 FD-ACCTFILE-REC.
          05 FD-ACCT-ID   PIC 9(11).
          05 FD-ACCT-DATA PIC X(289).
       WORKING-STORAGE SECTION.
       01 IN-STATUS  PIC XX.
       01 OUT-STATUS PIC XX.
       01 EOF        PIC X VALUE 'N'.
       PROCEDURE DIVISION.
           OPEN INPUT IN-FILE
           OPEN OUTPUT OUT-FILE
           PERFORM UNTIL EOF = 'Y'
               READ IN-FILE
                   AT END MOVE 'Y' TO EOF
                   NOT AT END
                       MOVE IN-REC TO FD-ACCTFILE-REC
                       WRITE FD-ACCTFILE-REC
               END-READ
           END-PERFORM
           CLOSE IN-FILE
           CLOSE OUT-FILE
           GOBACK.
