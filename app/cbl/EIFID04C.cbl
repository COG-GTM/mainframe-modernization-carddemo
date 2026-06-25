      ******************************************************************
      * Program     : EIFID04C.CBL
      * Application : ECIRetail
      * Type        : BATCH COBOL Program
      * Function    : Loyalty points and discount calculator.
      *               Processes category balances per centro, applies
      *               discount policy, and calculates fidelity points.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EIFID04C.
       AUTHOR.        ECI-SISTEMAS.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT VCATBAL-FILE ASSIGN TO VCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-VENTA-CAT-KEY
                  FILE STATUS  IS VCATBALF-STATUS.

           SELECT XREF-FILE ASSIGN TO   XREFFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-XREF-TARJ-NUM
                  ALTERNATE RECORD KEY IS FD-XREF-CENTRO-ID
                  FILE STATUS  IS XREFFILE-STATUS.

           SELECT CENTRO-FILE ASSIGN TO CNTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-CENTRO-ID
                  FILE STATUS  IS CNTFILE-STATUS.

           SELECT DESCTO-FILE ASSIGN TO DESCTGRP
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-DESCTO-KEY
                  FILE STATUS  IS DESCTO-STATUS.

           SELECT FIDELITY-FILE ASSIGN TO FIDELTXN
                  ORGANIZATION IS SEQUENTIAL
                  ACCESS MODE  IS SEQUENTIAL
                  FILE STATUS  IS FIDELTXN-STATUS.

      *
       DATA DIVISION.
       FILE SECTION.
       FD  VCATBAL-FILE.
       01  FD-VENTA-CAT-BAL-RECORD.
           05 FD-VENTA-CAT-KEY.
              10 FD-VCATBAL-CENTRO-ID           PIC 9(05).
              10 FD-VCATBAL-TIPO-CD             PIC X(02).
              10 FD-VCATBAL-CAT-CD              PIC 9(04).
           05 FD-VENTA-CAT-DATA                 PIC X(39).

       FD  XREF-FILE.
       01  FD-XREFFILE-REC.
           05 FD-XREF-TARJ-NUM                  PIC X(16).
           05 FD-XREF-CLI-ID                    PIC 9(09).
           05 FD-XREF-CENTRO-ID                 PIC 9(05).
           05 FD-XREF-FILLER                    PIC X(20).

       FD  DESCTO-FILE.
       01  FD-DESCTO-REC.
           05 FD-DESCTO-KEY.
              10 FD-DESCTO-SEGMENTO-ID          PIC X(10).
              10 FD-DESCTO-TIPO-CD              PIC X(02).
              10 FD-DESCTO-CAT-CD               PIC 9(04).
           05 FD-DESCTO-DATA                    PIC X(34).

       FD  CENTRO-FILE.
       01  FD-CNTFILE-REC.
           05 FD-CENTRO-ID                      PIC 9(05).
           05 FD-CENTRO-DATA                    PIC X(295).

       FD  FIDELITY-FILE.
       01  FD-FIDELTXN-REC.
           05 FD-FIDEL-CENTRO-ID                PIC 9(05).
           05 FD-FIDEL-TIPO-CD                  PIC X(02).
           05 FD-FIDEL-CAT-CD                   PIC 9(04).
           05 FD-FIDEL-IMPORTE-VENTA            PIC S9(09)V99.
           05 FD-FIDEL-PUNTOS-GENERADOS         PIC 9(09).
           05 FD-FIDEL-DESCUENTO-APLICADO       PIC S9(07)V99.
           05 FD-FIDEL-FECHA-PROCESO            PIC X(10).
           05 FD-FIDEL-FILLER                   PIC X(295).

       WORKING-STORAGE SECTION.

      *****************************************************************
       COPY EICAT01Y.
       01  VCATBALF-STATUS.
           05  VCATBALF-STAT1      PIC X.
           05  VCATBALF-STAT2      PIC X.

       COPY EIXRF03Y.
       01  XREFFILE-STATUS.
           05  XREFFILE-STAT1      PIC X.
           05  XREFFILE-STAT2      PIC X.

       COPY EICAT02Y.
       01  DESCTO-STATUS.
           05 DESCTO-STAT1        PIC X.
           05 DESCTO-STAT2        PIC X.

       COPY EICNT01Y.
       01  CNTFILE-STATUS.
           05  CNTFILE-STAT1      PIC X.
           05  CNTFILE-STAT2      PIC X.

       01  FIDELTXN-STATUS.
           05  FIDELTXN-STAT1     PIC X.
           05  FIDELTXN-STAT2     PIC X.

       01  IO-STATUS.
           05  IO-STAT1            PIC X.
           05  IO-STAT2            PIC X.
       01  TWO-BYTES-BINARY        PIC 9(4) BINARY.
       01  TWO-BYTES-ALPHA         REDEFINES TWO-BYTES-BINARY.
           05  TWO-BYTES-LEFT      PIC X.
           05  TWO-BYTES-RIGHT     PIC X.
       01  IO-STATUS-04.
           05  IO-STATUS-0401      PIC 9   VALUE 0.
           05  IO-STATUS-0403      PIC 999 VALUE 0.

       01  APPL-RESULT             PIC S9(9)   COMP.
           88  APPL-AOK            VALUE 0.
           88  APPL-EOF            VALUE 16.

       01  END-OF-FILE             PIC X(01)    VALUE 'N'.
       01  ABCODE                  PIC S9(9) BINARY.
       01  TIMING                  PIC S9(9) BINARY.

       01 WS-MISC-VARS.
           05 WS-LAST-CENTRO-NUM        PIC 9(05) VALUE ZEROS.
           05 WS-PUNTOS-CALCULADOS      PIC 9(09).
           05 WS-DESCUENTO-CALCULADO    PIC S9(07)V99.
           05 WS-TOTAL-PUNTOS-CENTRO    PIC 9(09).
           05 WS-FIRST-TIME             PIC X(01) VALUE 'Y'.
           05 WS-CENTRO-ZONA            PIC X(10).
       01 WS-COUNTERS.
           05 WS-RECORD-COUNT           PIC 9(09) VALUE 0.
           05 WS-CENTROS-PROCESADOS     PIC 9(05) VALUE 0.

       LINKAGE SECTION.
       01  EXTERNAL-PARMS.
           05  PARM-LENGTH         PIC S9(04) COMP.
           05  PARM-DATE           PIC X(10).
      *****************************************************************
       PROCEDURE DIVISION USING EXTERNAL-PARMS.
           DISPLAY 'START OF EXECUTION OF PROGRAM EIFID04C'.
           DISPLAY 'CALCULO PUNTOS FIDELIDAD - 123 CENTROS'.
           PERFORM 0000-VCATBALF-OPEN.
           PERFORM 0100-XREFFILE-OPEN.
           PERFORM 0200-DESCTO-OPEN.
           PERFORM 0300-CNTFILE-OPEN.
           PERFORM 0400-FIDELTXN-OPEN.

           PERFORM UNTIL END-OF-FILE = 'Y'
               IF  END-OF-FILE = 'N'
                   PERFORM 1000-VCATBALF-GET-NEXT
                   IF  END-OF-FILE = 'N'
                     ADD 1 TO WS-RECORD-COUNT
                     DISPLAY VENTA-CAT-BAL-RECORD
                     IF VCATBAL-CENTRO-ID NOT= WS-LAST-CENTRO-NUM
                       IF WS-FIRST-TIME NOT = 'Y'
                          DISPLAY 'CENTRO ' WS-LAST-CENTRO-NUM
                              ' TOTAL PUNTOS: ' WS-TOTAL-PUNTOS-CENTRO
                       ELSE
                          MOVE 'N' TO WS-FIRST-TIME
                       END-IF
                       MOVE 0 TO WS-TOTAL-PUNTOS-CENTRO
                       MOVE VCATBAL-CENTRO-ID TO WS-LAST-CENTRO-NUM
                       ADD 1 TO WS-CENTROS-PROCESADOS
                       MOVE VCATBAL-CENTRO-ID TO FD-CENTRO-ID
                       PERFORM 1100-GET-CENTRO-DATA
                     END-IF
                     MOVE CENTRO-ZONA-COMERCIAL TO WS-CENTRO-ZONA
                     MOVE WS-CENTRO-ZONA TO FD-DESCTO-SEGMENTO-ID
                     MOVE VCATBAL-TIPO-CD TO FD-DESCTO-TIPO-CD
                     MOVE VCATBAL-CAT-CD TO FD-DESCTO-CAT-CD
                     PERFORM 1200-GET-DESCUENTO-RATE
                     PERFORM 1300-COMPUTE-PUNTOS
                     PERFORM 1400-WRITE-FIDEL-TXN
                   END-IF
               ELSE
                    DISPLAY 'CENTRO ' WS-LAST-CENTRO-NUM
                        ' TOTAL PUNTOS: ' WS-TOTAL-PUNTOS-CENTRO
               END-IF
           END-PERFORM.

           PERFORM 9000-VCATBALF-CLOSE.
           PERFORM 9100-XREFFILE-CLOSE.
           PERFORM 9200-DESCTO-CLOSE.
           PERFORM 9300-CNTFILE-CLOSE.
           PERFORM 9400-FIDELTXN-CLOSE.

           DISPLAY 'REGISTROS PROCESADOS: ' WS-RECORD-COUNT
           DISPLAY 'CENTROS PROCESADOS  : ' WS-CENTROS-PROCESADOS
           DISPLAY 'END OF EXECUTION OF PROGRAM EIFID04C'.

           GOBACK.
      *---------------------------------------------------------------*
       0000-VCATBALF-OPEN.
           MOVE 8 TO APPL-RESULT.
           OPEN INPUT VCATBAL-FILE
           IF  VCATBALF-STATUS = '00'
               MOVE 0 TO APPL-RESULT
           ELSE
               MOVE 12 TO APPL-RESULT
           END-IF
           IF  APPL-AOK
               CONTINUE
           ELSE
               DISPLAY 'ERROR OPENING VCATBAL FILE'
               MOVE VCATBALF-STATUS TO IO-STATUS
               PERFORM 9910-DISPLAY-IO-STATUS
               PERFORM 9999-ABEND-PROGRAM
           END-IF
           .
      *---------------------------------------------------------------*
       0100-XREFFILE-OPEN.
           MOVE 8 TO APPL-RESULT.
           OPEN INPUT XREF-FILE
           IF  XREFFILE-STATUS = '00'
               MOVE 0 TO APPL-RESULT
           ELSE
               MOVE 12 TO APPL-RESULT
           END-IF
           IF  APPL-AOK
               CONTINUE
           ELSE
               DISPLAY 'ERROR OPENING XREF FILE'
               MOVE XREFFILE-STATUS TO IO-STATUS
               PERFORM 9910-DISPLAY-IO-STATUS
               PERFORM 9999-ABEND-PROGRAM
           END-IF
           .
      *---------------------------------------------------------------*
       0200-DESCTO-OPEN.
           MOVE 8 TO APPL-RESULT.
           OPEN INPUT DESCTO-FILE
           IF  DESCTO-STATUS = '00'
               MOVE 0 TO APPL-RESULT
           ELSE
               MOVE 12 TO APPL-RESULT
           END-IF
           IF  APPL-AOK
               CONTINUE
           ELSE
               DISPLAY 'ERROR OPENING DESCUENTO FILE'
               MOVE DESCTO-STATUS TO IO-STATUS
               PERFORM 9910-DISPLAY-IO-STATUS
               PERFORM 9999-ABEND-PROGRAM
           END-IF
           .
      *---------------------------------------------------------------*
       0300-CNTFILE-OPEN.
           MOVE 8 TO APPL-RESULT.
           OPEN INPUT CENTRO-FILE
           IF  CNTFILE-STATUS = '00'
               MOVE 0 TO APPL-RESULT
           ELSE
               MOVE 12 TO APPL-RESULT
           END-IF
           IF  APPL-AOK
               CONTINUE
           ELSE
               DISPLAY 'ERROR OPENING CENTRO FILE'
               MOVE CNTFILE-STATUS TO IO-STATUS
               PERFORM 9910-DISPLAY-IO-STATUS
               PERFORM 9999-ABEND-PROGRAM
           END-IF
           .
      *---------------------------------------------------------------*
       0400-FIDELTXN-OPEN.
           MOVE 8 TO APPL-RESULT.
           OPEN OUTPUT FIDELITY-FILE
           IF  FIDELTXN-STATUS = '00'
               MOVE 0 TO APPL-RESULT
           ELSE
               MOVE 12 TO APPL-RESULT
           END-IF
           IF  APPL-AOK
               CONTINUE
           ELSE
               DISPLAY 'ERROR OPENING FIDELITY TXN FILE'
               MOVE FIDELTXN-STATUS TO IO-STATUS
               PERFORM 9910-DISPLAY-IO-STATUS
               PERFORM 9999-ABEND-PROGRAM
           END-IF
           .
      *---------------------------------------------------------------*
       1000-VCATBALF-GET-NEXT.
           READ VCATBAL-FILE INTO VENTA-CAT-BAL-RECORD
           IF  VCATBALF-STATUS = '00'
               CONTINUE
           ELSE
               IF VCATBALF-STATUS = '10'
                   MOVE 'Y' TO END-OF-FILE
               ELSE
                   DISPLAY 'ERROR READING VCATBAL FILE'
                   MOVE VCATBALF-STATUS TO IO-STATUS
                   PERFORM 9910-DISPLAY-IO-STATUS
                   PERFORM 9999-ABEND-PROGRAM
               END-IF
           END-IF
           .
      *---------------------------------------------------------------*
       1100-GET-CENTRO-DATA.
           READ CENTRO-FILE INTO CENTRO-RECORD
           IF  CNTFILE-STATUS NOT = '00'
               DISPLAY 'CENTRO NOT FOUND: ' FD-CENTRO-ID
               MOVE 'DEFECTO   ' TO CENTRO-ZONA-COMERCIAL
           END-IF
           .
      *---------------------------------------------------------------*
       1200-GET-DESCUENTO-RATE.
           READ DESCTO-FILE INTO DESCTO-RECORD
           IF  DESCTO-STATUS NOT = '00'
               MOVE 0 TO DESCTO-PCT-DESCUENTO
               MOVE 1 TO DESCTO-PUNTOS-X-EURO
           END-IF
           .
      *---------------------------------------------------------------*
       1300-COMPUTE-PUNTOS.
      *    Calculate loyalty points based on sales amount
           COMPUTE WS-PUNTOS-CALCULADOS =
               VENTA-CAT-BAL * DESCTO-PUNTOS-X-EURO
           ADD WS-PUNTOS-CALCULADOS TO WS-TOTAL-PUNTOS-CENTRO
           ADD WS-PUNTOS-CALCULADOS TO VENTA-CAT-PUNTOS
      *    Calculate discount amount
           COMPUTE WS-DESCUENTO-CALCULADO =
               VENTA-CAT-BAL * DESCTO-PCT-DESCUENTO / 100
           .
      *---------------------------------------------------------------*
       1400-WRITE-FIDEL-TXN.
           MOVE VCATBAL-CENTRO-ID      TO FD-FIDEL-CENTRO-ID
           MOVE VCATBAL-TIPO-CD        TO FD-FIDEL-TIPO-CD
           MOVE VCATBAL-CAT-CD         TO FD-FIDEL-CAT-CD
           MOVE VENTA-CAT-BAL          TO FD-FIDEL-IMPORTE-VENTA
           MOVE WS-PUNTOS-CALCULADOS   TO FD-FIDEL-PUNTOS-GENERADOS
           MOVE WS-DESCUENTO-CALCULADO TO FD-FIDEL-DESCUENTO-APLICADO
           MOVE PARM-DATE              TO FD-FIDEL-FECHA-PROCESO
           MOVE SPACES                 TO FD-FIDEL-FILLER
           WRITE FD-FIDELTXN-REC
           .
      *---------------------------------------------------------------*
       9000-VCATBALF-CLOSE.
           CLOSE VCATBAL-FILE.
       9100-XREFFILE-CLOSE.
           CLOSE XREF-FILE.
       9200-DESCTO-CLOSE.
           CLOSE DESCTO-FILE.
       9300-CNTFILE-CLOSE.
           CLOSE CENTRO-FILE.
       9400-FIDELTXN-CLOSE.
           CLOSE FIDELITY-FILE.
      *---------------------------------------------------------------*
       9910-DISPLAY-IO-STATUS.
           DISPLAY 'FILE STATUS: ' IO-STAT1 IO-STAT2
           .
      *---------------------------------------------------------------*
       9999-ABEND-PROGRAM.
           DISPLAY 'ABEND IN EIFID04C'
           MOVE 12 TO RETURN-CODE
           GOBACK
           .
