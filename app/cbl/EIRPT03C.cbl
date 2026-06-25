      ******************************************************************
      * Program     : EIRPT03C.CBL
      * Application : ECIRetail
      * Type        : BATCH COBOL Program
      * Function    : Transaction report generator.
      *               Produces a summary report of tickets by
      *               centro, tipo producto and categoria.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EIRPT03C.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT VCATBAL-FILE ASSIGN TO VCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-VENTA-CAT-KEY
                  FILE STATUS  IS VCATBALF-STATUS.

           SELECT CENTRO-FILE ASSIGN TO CNTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-CENTRO-ID
                  FILE STATUS  IS CNTFILE-STATUS.

           SELECT REPORT-FILE ASSIGN TO RPTFILE
                  ORGANIZATION IS SEQUENTIAL
                  ACCESS MODE  IS SEQUENTIAL
                  FILE STATUS  IS RPTFILE-STATUS.

       DATA DIVISION.
       FILE SECTION.
       FD  VCATBAL-FILE.
       01  FD-VENTA-CAT-BAL-RECORD.
           05 FD-VENTA-CAT-KEY.
              10 FD-VCATBAL-CENTRO-ID           PIC 9(05).
              10 FD-VCATBAL-TIPO-CD             PIC X(02).
              10 FD-VCATBAL-CAT-CD              PIC 9(04).
           05 FD-VENTA-CAT-DATA                 PIC X(39).

       FD  CENTRO-FILE.
       01  FD-CNTFILE-REC.
           05 FD-CENTRO-ID                      PIC 9(05).
           05 FD-CENTRO-DATA                    PIC X(295).

       FD  REPORT-FILE
           RECORDING MODE IS F
           RECORD CONTAINS 132 CHARACTERS.
       01  FD-REPORT-REC                        PIC X(132).

       WORKING-STORAGE SECTION.

       COPY EICAT01Y.
       01  VCATBALF-STATUS.
           05  VCATBALF-STAT1      PIC X.
           05  VCATBALF-STAT2      PIC X.

       COPY EICNT01Y.
       01  CNTFILE-STATUS.
           05  CNTFILE-STAT1      PIC X.
           05  CNTFILE-STAT2      PIC X.

       01  RPTFILE-STATUS.
           05  RPTFILE-STAT1      PIC X.
           05  RPTFILE-STAT2      PIC X.

       01  APPL-RESULT             PIC S9(9)   COMP.
           88  APPL-AOK            VALUE 0.
           88  APPL-EOF            VALUE 16.
       01  END-OF-FILE             PIC X(01)    VALUE 'N'.

       01  WS-RPT-HEADER          PIC X(132) VALUE
           '=== INFORME CONSOLIDACION VENTAS POR CENTRO/CATEGORIA ====='.

       01  WS-RPT-DETAIL.
           05  WS-RPT-CENTRO       PIC 9(05).
           05  FILLER              PIC X(03) VALUE ' | '.
           05  WS-RPT-TIPO         PIC X(02).
           05  FILLER              PIC X(03) VALUE ' | '.
           05  WS-RPT-CAT          PIC 9(04).
           05  FILLER              PIC X(03) VALUE ' | '.
           05  WS-RPT-SALDO        PIC Z,ZZZ,ZZZ,ZZ9.99.
           05  FILLER              PIC X(03) VALUE ' | '.
           05  WS-RPT-PUNTOS       PIC Z,ZZZ,ZZ9.
           05  FILLER              PIC X(75) VALUE SPACES.

      *****************************************************************
       PROCEDURE DIVISION.
           DISPLAY 'START OF EXECUTION OF PROGRAM EIRPT03C'.
           OPEN INPUT VCATBAL-FILE
           OPEN INPUT CENTRO-FILE
           OPEN OUTPUT REPORT-FILE

           WRITE FD-REPORT-REC FROM WS-RPT-HEADER

           PERFORM UNTIL END-OF-FILE = 'Y'
               READ VCATBAL-FILE INTO VENTA-CAT-BAL-RECORD
               IF  VCATBALF-STATUS = '00'
                   MOVE VCATBAL-CENTRO-ID TO WS-RPT-CENTRO
                   MOVE VCATBAL-TIPO-CD   TO WS-RPT-TIPO
                   MOVE VCATBAL-CAT-CD    TO WS-RPT-CAT
                   MOVE VENTA-CAT-BAL     TO WS-RPT-SALDO
                   MOVE VENTA-CAT-PUNTOS  TO WS-RPT-PUNTOS
                   WRITE FD-REPORT-REC FROM WS-RPT-DETAIL
               ELSE
                   MOVE 'Y' TO END-OF-FILE
               END-IF
           END-PERFORM

           CLOSE VCATBAL-FILE
           CLOSE CENTRO-FILE
           CLOSE REPORT-FILE
           DISPLAY 'END OF EXECUTION OF PROGRAM EIRPT03C'.
           GOBACK.
