       IDENTIFICATION DIVISION.
       PROGRAM-ID. LOADIDX.
      * Loads the line-sequential lookup fixtures into GnuCOBOL indexed
      * files so the original CBTRN03C can read them by key.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT XREF-IN ASSIGN TO XREFIN
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT TYPE-IN ASSIGN TO TYPEIN
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT CATG-IN ASSIGN TO CATGIN
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT XREF-OUT ASSIGN TO CARDXREF
                  ORGANIZATION IS INDEXED ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS XO-KEY.
           SELECT TYPE-OUT ASSIGN TO TRANTYPE
                  ORGANIZATION IS INDEXED ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS TO-KEY.
           SELECT CATG-OUT ASSIGN TO TRANCATG
                  ORGANIZATION IS INDEXED ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS CO-KEY.
       DATA DIVISION.
       FILE SECTION.
       FD  XREF-IN.
       01  XI-REC               PIC X(50).
       FD  TYPE-IN.
       01  TI-REC               PIC X(60).
       FD  CATG-IN.
       01  CI-REC               PIC X(60).
       FD  XREF-OUT.
       01  XO-REC.
           05 XO-KEY            PIC X(16).
           05 FILLER            PIC X(34).
       FD  TYPE-OUT.
       01  TO-REC.
           05 TO-KEY            PIC X(02).
           05 FILLER            PIC X(58).
       FD  CATG-OUT.
       01  CO-REC.
           05 CO-KEY            PIC X(06).
           05 FILLER            PIC X(54).
       WORKING-STORAGE SECTION.
       01  WS-EOF               PIC X.
       PROCEDURE DIVISION.
           OPEN INPUT XREF-IN TYPE-IN CATG-IN
           OPEN OUTPUT XREF-OUT TYPE-OUT CATG-OUT
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
              READ XREF-IN AT END MOVE 'Y' TO WS-EOF
                 NOT AT END WRITE XO-REC FROM XI-REC END-READ
           END-PERFORM
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
              READ TYPE-IN AT END MOVE 'Y' TO WS-EOF
                 NOT AT END WRITE TO-REC FROM TI-REC END-READ
           END-PERFORM
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
              READ CATG-IN AT END MOVE 'Y' TO WS-EOF
                 NOT AT END WRITE CO-REC FROM CI-REC END-READ
           END-PERFORM
           CLOSE XREF-IN TYPE-IN CATG-IN XREF-OUT TYPE-OUT CATG-OUT
           GOBACK.
