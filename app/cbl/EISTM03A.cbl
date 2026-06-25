      ******************************************************************
      * Program     : EISTM03A.CBL
      * Application : ECIRetail
      * Type        : BATCH COBOL Program
      * Function    : Produce daily sales statement per centro.
      *               Consolidates all ticket transactions by centro
      *               and generates a report file.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EISTM03A.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TICKET-FILE ASSIGN TO TKTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-TICKET-ID
                  FILE STATUS  IS TKTFILE-STATUS.

           SELECT CENTRO-FILE ASSIGN TO CNTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-CENTRO-ID
                  FILE STATUS  IS CNTFILE-STATUS.

           SELECT REPORT-FILE ASSIGN TO RPTFILE
                  ORGANIZATION IS SEQUENTIAL
                  ACCESS MODE  IS SEQUENTIAL
                  FILE STATUS  IS RPTFILE-STATUS.

      *
       DATA DIVISION.
       FILE SECTION.
       FD  TICKET-FILE.
       01  FD-TKTFILE-REC.
           05 FD-TICKET-ID                      PIC X(16).
           05 FD-TICKET-DATA                    PIC X(334).

       FD  CENTRO-FILE.
       01  FD-CNTFILE-REC.
           05 FD-CENTRO-ID                      PIC 9(05).
           05 FD-CENTRO-DATA                    PIC X(295).

       FD  REPORT-FILE
           RECORDING MODE IS F
           RECORD CONTAINS 132 CHARACTERS.
       01  FD-REPORT-REC                        PIC X(132).

       WORKING-STORAGE SECTION.

       COPY EITKT05Y.
       01  TKTFILE-STATUS.
           05  TKTFILE-STAT1      PIC X.
           05  TKTFILE-STAT2      PIC X.

       COPY EICNT01Y.
       01  CNTFILE-STATUS.
           05  CNTFILE-STAT1      PIC X.
           05  CNTFILE-STAT2      PIC X.

       01  RPTFILE-STATUS.
           05  RPTFILE-STAT1      PIC X.
           05  RPTFILE-STAT2      PIC X.

       01  IO-STATUS.
           05  IO-STAT1            PIC X.
           05  IO-STAT2            PIC X.

       01  APPL-RESULT             PIC S9(9)   COMP.
           88  APPL-AOK            VALUE 0.
           88  APPL-EOF            VALUE 16.

       01  END-OF-FILE             PIC X(01)    VALUE 'N'.

       01  WS-REPORT-HEADER.
           05  FILLER              PIC X(40) VALUE
               '=== EXTRACTO VENTAS DIARIO ECI ========'.
           05  FILLER              PIC X(20) VALUE SPACES.
           05  WS-RPT-FECHA        PIC X(10).
           05  FILLER              PIC X(62) VALUE SPACES.

       01  WS-CENTRO-HEADER.
           05  FILLER              PIC X(10) VALUE 'CENTRO: '.
           05  WS-RPT-CENTRO-ID    PIC 9(05).
           05  FILLER              PIC X(03) VALUE ' - '.
           05  WS-RPT-CENTRO-NOM   PIC X(50).
           05  FILLER              PIC X(64) VALUE SPACES.

       01  WS-DETAIL-LINE.
           05  FILLER              PIC X(03) VALUE '  '.
           05  WS-DTL-TICKET-ID    PIC X(16).
           05  FILLER              PIC X(02) VALUE '  '.
           05  WS-DTL-SECCION      PIC X(20).
           05  FILLER              PIC X(02) VALUE '  '.
           05  WS-DTL-IMPORTE      PIC Z,ZZZ,ZZ9.99.
           05  FILLER              PIC X(02) VALUE '  '.
           05  WS-DTL-ARTICULOS    PIC ZZ9.
           05  FILLER              PIC X(02) VALUE '  '.
           05  WS-DTL-METODO       PIC X(02).
           05  FILLER              PIC X(64) VALUE SPACES.

       01  WS-TOTAL-LINE.
           05  FILLER              PIC X(20) VALUE
               '  TOTAL CENTRO:     '.
           05  WS-TOT-IMPORTE      PIC Z,ZZZ,ZZZ,ZZ9.99.
           05  FILLER              PIC X(05) VALUE '  TKT'.
           05  WS-TOT-TICKETS      PIC ZZ,ZZ9.
           05  FILLER              PIC X(80) VALUE SPACES.

       01  WS-COUNTERS.
           05  WS-LAST-CENTRO      PIC 9(05) VALUE ZEROS.
           05  WS-CENTRO-TOTAL     PIC S9(11)V99 VALUE 0.
           05  WS-CENTRO-TICKETS   PIC 9(07) VALUE 0.
           05  WS-GRAND-TOTAL      PIC S9(11)V99 VALUE 0.
           05  WS-GRAND-TICKETS    PIC 9(09) VALUE 0.
           05  WS-FIRST-TIME       PIC X(01) VALUE 'Y'.

       LINKAGE SECTION.
       01  EXTERNAL-PARMS.
           05  PARM-LENGTH         PIC S9(04) COMP.
           05  PARM-DATE           PIC X(10).

      *****************************************************************
       PROCEDURE DIVISION USING EXTERNAL-PARMS.
           DISPLAY 'START OF EXECUTION OF PROGRAM EISTM03A'.
           DISPLAY 'EXTRACTO VENTAS DIARIO - 123 CENTROS ECI'.
           MOVE PARM-DATE TO WS-RPT-FECHA.

           OPEN INPUT TICKET-FILE
           OPEN INPUT CENTRO-FILE
           OPEN OUTPUT REPORT-FILE
           WRITE FD-REPORT-REC FROM WS-REPORT-HEADER

           PERFORM UNTIL END-OF-FILE = 'Y'
               READ TICKET-FILE INTO TICKET-RECORD
               IF  TKTFILE-STATUS = '00'
                   IF TICKET-CENTRO-ID NOT = WS-LAST-CENTRO
                     IF WS-FIRST-TIME = 'N'
                       PERFORM 2000-WRITE-CENTRO-TOTAL
                     ELSE
                       MOVE 'N' TO WS-FIRST-TIME
                     END-IF
                     MOVE TICKET-CENTRO-ID TO WS-LAST-CENTRO
                     MOVE 0 TO WS-CENTRO-TOTAL
                     MOVE 0 TO WS-CENTRO-TICKETS
                     PERFORM 1500-WRITE-CENTRO-HEADER
                   END-IF
                   PERFORM 1800-WRITE-DETAIL
               ELSE
                   MOVE 'Y' TO END-OF-FILE
                   IF WS-FIRST-TIME = 'N'
                     PERFORM 2000-WRITE-CENTRO-TOTAL
                   END-IF
               END-IF
           END-PERFORM

           CLOSE TICKET-FILE
           CLOSE CENTRO-FILE
           CLOSE REPORT-FILE
           DISPLAY 'TOTAL VENTAS: ' WS-GRAND-TOTAL
           DISPLAY 'TOTAL TICKETS: ' WS-GRAND-TICKETS
           DISPLAY 'END OF EXECUTION OF PROGRAM EISTM03A'.
           GOBACK.

      *---------------------------------------------------------------*
       1500-WRITE-CENTRO-HEADER.
           MOVE TICKET-CENTRO-ID TO FD-CENTRO-ID
           READ CENTRO-FILE INTO CENTRO-RECORD
           MOVE TICKET-CENTRO-ID TO WS-RPT-CENTRO-ID
           IF CNTFILE-STATUS = '00'
             MOVE CENTRO-NOMBRE TO WS-RPT-CENTRO-NOM
           ELSE
             MOVE 'CENTRO NO ENCONTRADO' TO WS-RPT-CENTRO-NOM
           END-IF
           WRITE FD-REPORT-REC FROM WS-CENTRO-HEADER
           .
      *---------------------------------------------------------------*
       1800-WRITE-DETAIL.
           ADD 1 TO WS-CENTRO-TICKETS
           ADD 1 TO WS-GRAND-TICKETS
           ADD TICKET-IMPORTE TO WS-CENTRO-TOTAL
           ADD TICKET-IMPORTE TO WS-GRAND-TOTAL
           MOVE TICKET-ID         TO WS-DTL-TICKET-ID
           MOVE TICKET-SECCION    TO WS-DTL-SECCION
           MOVE TICKET-IMPORTE    TO WS-DTL-IMPORTE
           MOVE TICKET-NUM-ARTICULOS TO WS-DTL-ARTICULOS
           MOVE TICKET-METODO-PAGO TO WS-DTL-METODO
           WRITE FD-REPORT-REC FROM WS-DETAIL-LINE
           .
      *---------------------------------------------------------------*
       2000-WRITE-CENTRO-TOTAL.
           MOVE WS-CENTRO-TOTAL TO WS-TOT-IMPORTE
           MOVE WS-CENTRO-TICKETS TO WS-TOT-TICKETS
           WRITE FD-REPORT-REC FROM WS-TOTAL-LINE
           .
