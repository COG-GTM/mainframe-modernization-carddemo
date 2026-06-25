      ******************************************************************
      * Program     : EITRN02C.CBL
      * Application : ECIRetail
      * Type        : BATCH COBOL Program
      * Function    : Post daily ticket records from POS to master.
      *               Validates tickets, updates category balance per
      *               centro, and writes rejected tickets to GDG.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EITRN02C.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT DALYTICKET-FILE ASSIGN TO DALYTKT
                  ORGANIZATION IS SEQUENTIAL
                  ACCESS MODE  IS SEQUENTIAL
                  FILE STATUS  IS DALYTKT-STATUS.

           SELECT TICKET-FILE ASSIGN TO TKTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-TICKET-ID
                  FILE STATUS  IS TKTFILE-STATUS.

           SELECT XREF-FILE ASSIGN TO   XREFFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-XREF-TARJ-NUM
                  FILE STATUS  IS XREFFILE-STATUS.

           SELECT DALYREJS-FILE ASSIGN TO DALYREJS
                  ORGANIZATION IS SEQUENTIAL
                  ACCESS MODE  IS SEQUENTIAL
                  FILE STATUS  IS DALYREJS-STATUS.

           SELECT CENTRO-FILE ASSIGN TO CNTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-CENTRO-ID
                  FILE STATUS  IS CNTFILE-STATUS.

           SELECT VCATBAL-FILE ASSIGN TO VCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-VENTA-CAT-KEY
                  FILE STATUS  IS VCATBALF-STATUS.

      *
       DATA DIVISION.
       FILE SECTION.
       FD  DALYTICKET-FILE.
       01  FD-DALYTKT-RECORD.
           05 FD-DALYTKT-ID                     PIC X(16).
           05 FD-DALYTKT-DATA                   PIC X(334).

       FD  TICKET-FILE.
       01  FD-TKTFILE-REC.
           05 FD-TICKET-ID                      PIC X(16).
           05 FD-TICKET-DATA                    PIC X(334).

       FD  XREF-FILE.
       01  FD-XREFFILE-REC.
           05 FD-XREF-TARJ-NUM                  PIC X(16).
           05 FD-XREF-DATA                      PIC X(34).

       FD  DALYREJS-FILE.
       01  FD-REJS-RECORD.
           05 FD-REJECT-RECORD                  PIC X(350).
           05 FD-VALIDATION-TRAILER             PIC X(80).

       FD  CENTRO-FILE.
       01  FD-CNTFILE-REC.
           05 FD-CENTRO-ID                      PIC 9(05).
           05 FD-CENTRO-DATA                    PIC X(295).

       FD  VCATBAL-FILE.
       01  FD-VENTA-CAT-BAL-RECORD.
           05 FD-VENTA-CAT-KEY.
              10 FD-VCATBAL-CENTRO-ID           PIC 9(05).
              10 FD-VCATBAL-TIPO-CD             PIC X(02).
              10 FD-VCATBAL-CAT-CD              PIC 9(04).
           05 FD-VENTA-CAT-DATA                 PIC X(39).

       WORKING-STORAGE SECTION.

      *****************************************************************
       COPY EITKT06Y.
       01  DALYTKT-STATUS.
           05  DALYTKT-STAT1      PIC X.
           05  DALYTKT-STAT2      PIC X.

       COPY EITKT05Y.
       01  TKTFILE-STATUS.
           05  TKTFILE-STAT1      PIC X.
           05  TKTFILE-STAT2      PIC X.

       COPY EIXRF03Y.
       01  XREFFILE-STATUS.
           05  XREFFILE-STAT1      PIC X.
           05  XREFFILE-STAT2      PIC X.

       01  DALYREJS-STATUS.
           05  DALYREJS-STAT1      PIC X.
           05  DALYREJS-STAT2      PIC X.

       COPY EICNT01Y.
       01  CNTFILE-STATUS.
           05  CNTFILE-STAT1      PIC X.
           05  CNTFILE-STAT2      PIC X.

       COPY EICAT01Y.
       01  VCATBALF-STATUS.
           05  VCATBALF-STAT1      PIC X.
           05  VCATBALF-STAT2      PIC X.

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

       01  COBOL-TS.
           05 COB-YYYY                  PIC X(04).
           05 COB-MM                    PIC X(02).
           05 COB-DD                    PIC X(02).
           05 COB-HH                    PIC X(02).
           05 COB-MIN                   PIC X(02).
           05 COB-SS                    PIC X(02).
           05 COB-MIL                   PIC X(02).
           05 COB-REST                  PIC X(05).
       01  DB2-FORMAT-TS                PIC X(26).
       01  FILLER REDEFINES DB2-FORMAT-TS.
           06 DB2-YYYY                  PIC X(004).
           06 DB2-STREEP-1              PIC X.
           06 DB2-MM                    PIC X(002).
           06 DB2-STREEP-2              PIC X.
           06 DB2-DD                    PIC X(002).
           06 DB2-STREEP-3              PIC X.
           06 DB2-HH                    PIC X(002).
           06 DB2-DOT-1                 PIC X.
           06 DB2-MIN                   PIC X(002).
           06 DB2-DOT-2                 PIC X.
           06 DB2-SS                    PIC X(002).
           06 DB2-DOT-3                 PIC X.
           06 DB2-MIL                   PIC 9(002).
           06 DB2-REST                  PIC X(04).

        01 REJECT-RECORD.
           05 REJECT-TRAN-DATA          PIC X(350).
           05 VALIDATION-TRAILER        PIC X(80).

        01 WS-VALIDATION-TRAILER.
           05 WS-VALIDATION-FAIL-REASON      PIC 9(04).
           05 WS-VALIDATION-FAIL-REASON-DESC PIC X(76).

        01 WS-COUNTERS.
           05 WS-TICKET-COUNT               PIC 9(09) VALUE 0.
           05 WS-REJECT-COUNT               PIC 9(09) VALUE 0.
           05 WS-CENTRO-COUNT               PIC 9(05) VALUE 0.
           05 WS-TEMP-BAL                   PIC S9(09)V99.

        01 WS-FLAGS.
           05 WS-CREATE-VCATBAL-REC         PIC X(01) VALUE 'N'.

      *****************************************************************
       PROCEDURE DIVISION.
           DISPLAY 'START OF EXECUTION OF PROGRAM EITRN02C'.
           DISPLAY 'POSTEO DE TICKETS DIARIOS - ECIRETAIL'.
           PERFORM 0000-DALYTICKET-OPEN.
           PERFORM 0100-TKTFILE-OPEN.
           PERFORM 0200-XREFFILE-OPEN.
           PERFORM 0300-DALYREJS-OPEN.
           PERFORM 0400-CNTFILE-OPEN.
           PERFORM 0500-VCATBALF-OPEN.

           PERFORM UNTIL END-OF-FILE = 'Y'
               IF  END-OF-FILE = 'N'
                   PERFORM 1000-DALYTICKET-GET-NEXT
                   IF  END-OF-FILE = 'N'
                     ADD 1 TO WS-TICKET-COUNT
                     MOVE 0 TO WS-VALIDATION-FAIL-REASON
                     MOVE SPACES TO WS-VALIDATION-FAIL-REASON-DESC
                     PERFORM 1500-VALIDATE-TICKET
                     IF WS-VALIDATION-FAIL-REASON = 0
                       PERFORM 2000-POST-TICKET
                     ELSE
                       ADD 1 TO WS-REJECT-COUNT
                       PERFORM 2500-WRITE-REJECT-REC
                     END-IF
                   END-IF
               END-IF
           END-PERFORM.

           PERFORM 9000-DALYTICKET-CLOSE.
           PERFORM 9100-TKTFILE-CLOSE.
           PERFORM 9200-XREFFILE-CLOSE.
           PERFORM 9300-DALYREJS-CLOSE.
           PERFORM 9400-CNTFILE-CLOSE.
           PERFORM 9500-VCATBALF-CLOSE.
           DISPLAY 'TICKETS PROCESADOS  :' WS-TICKET-COUNT
           DISPLAY 'TICKETS RECHAZADOS  :' WS-REJECT-COUNT
           IF WS-REJECT-COUNT > 0
              MOVE 4 TO RETURN-CODE
           END-IF
           DISPLAY 'END OF EXECUTION OF PROGRAM EITRN02C'.

           GOBACK.
      *---------------------------------------------------------------*
       0000-DALYTICKET-OPEN.
           MOVE 8 TO APPL-RESULT.
           OPEN INPUT DALYTICKET-FILE
           IF  DALYTKT-STATUS = '00'
               MOVE 0 TO APPL-RESULT
           ELSE
               MOVE 12 TO APPL-RESULT
           END-IF
           IF  APPL-AOK
               CONTINUE
           ELSE
               DISPLAY 'ERROR OPENING DALYTICKET'
               MOVE DALYTKT-STATUS TO IO-STATUS
               PERFORM 9910-DISPLAY-IO-STATUS
               PERFORM 9999-ABEND-PROGRAM
           END-IF
           .
      *---------------------------------------------------------------*
       0100-TKTFILE-OPEN.
           MOVE 8 TO APPL-RESULT.
           OPEN I-O TICKET-FILE
           IF  TKTFILE-STATUS = '00'
               MOVE 0 TO APPL-RESULT
           ELSE
               MOVE 12 TO APPL-RESULT
           END-IF
           IF  APPL-AOK
               CONTINUE
           ELSE
               DISPLAY 'ERROR OPENING TICKET FILE'
               MOVE TKTFILE-STATUS TO IO-STATUS
               PERFORM 9910-DISPLAY-IO-STATUS
               PERFORM 9999-ABEND-PROGRAM
           END-IF
           .
      *---------------------------------------------------------------*
       0200-XREFFILE-OPEN.
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
       0300-DALYREJS-OPEN.
           MOVE 8 TO APPL-RESULT.
           OPEN OUTPUT DALYREJS-FILE
           IF  DALYREJS-STATUS = '00'
               MOVE 0 TO APPL-RESULT
           ELSE
               MOVE 12 TO APPL-RESULT
           END-IF
           IF  APPL-AOK
               CONTINUE
           ELSE
               DISPLAY 'ERROR OPENING REJECTS FILE'
               MOVE DALYREJS-STATUS TO IO-STATUS
               PERFORM 9910-DISPLAY-IO-STATUS
               PERFORM 9999-ABEND-PROGRAM
           END-IF
           .
      *---------------------------------------------------------------*
       0400-CNTFILE-OPEN.
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
       0500-VCATBALF-OPEN.
           MOVE 8 TO APPL-RESULT.
           OPEN I-O VCATBAL-FILE
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
       1000-DALYTICKET-GET-NEXT.
           READ DALYTICKET-FILE INTO DALYTICKET-RECORD
           IF  DALYTKT-STATUS = '00'
               CONTINUE
           ELSE
               IF DALYTKT-STATUS = '10'
                   MOVE 'Y' TO END-OF-FILE
               ELSE
                   DISPLAY 'ERROR READING DALYTICKET FILE'
                   MOVE DALYTKT-STATUS TO IO-STATUS
                   PERFORM 9910-DISPLAY-IO-STATUS
                   PERFORM 9999-ABEND-PROGRAM
               END-IF
           END-IF
           .
      *---------------------------------------------------------------*
       1500-VALIDATE-TICKET.
      *    Validate centro exists
           MOVE DALYTICKET-CENTRO-ID TO FD-CENTRO-ID
           READ CENTRO-FILE
           IF  CNTFILE-STATUS NOT = '00'
               MOVE 1001 TO WS-VALIDATION-FAIL-REASON
               MOVE 'CENTRO NO ENCONTRADO EN MAESTRO'
                   TO WS-VALIDATION-FAIL-REASON-DESC
               GO TO 1500-EXIT
           END-IF
      *    Validate tarjeta if present
           IF DALYTICKET-TARJ-NUM NOT = SPACES
             MOVE DALYTICKET-TARJ-NUM TO FD-XREF-TARJ-NUM
             READ XREF-FILE
             IF  XREFFILE-STATUS NOT = '00'
               MOVE 1002 TO WS-VALIDATION-FAIL-REASON
               MOVE 'TARJETA NO ENCONTRADA EN XREF'
                   TO WS-VALIDATION-FAIL-REASON-DESC
               GO TO 1500-EXIT
             END-IF
           END-IF
      *    Validate amount
           IF DALYTICKET-IMPORTE = 0
               MOVE 1003 TO WS-VALIDATION-FAIL-REASON
               MOVE 'IMPORTE DEL TICKET ES CERO'
                   TO WS-VALIDATION-FAIL-REASON-DESC
               GO TO 1500-EXIT
           END-IF
           .
       1500-EXIT.
           EXIT.
      *---------------------------------------------------------------*
       2000-POST-TICKET.
      *    Write ticket to master VSAM
           MOVE DALYTICKET-RECORD TO TICKET-RECORD
           MOVE FUNCTION CURRENT-DATE TO COBOL-TS
           STRING COB-YYYY '-' COB-MM '-' COB-DD '-'
                  COB-HH '.' COB-MIN '.' COB-SS '.'
                  COB-MIL '0000'
                  DELIMITED BY SIZE INTO DB2-FORMAT-TS
           END-STRING
           MOVE DB2-FORMAT-TS TO TICKET-PROC-TS
           MOVE TICKET-RECORD TO FD-TKTFILE-REC
           WRITE FD-TKTFILE-REC
           IF  TKTFILE-STATUS = '00'
               CONTINUE
           ELSE
             IF TKTFILE-STATUS = '22'
               DISPLAY 'DUPLICATE TICKET KEY: ' TICKET-ID
               MOVE TICKET-RECORD TO FD-TKTFILE-REC
               REWRITE FD-TKTFILE-REC
             ELSE
               DISPLAY 'ERROR WRITING TICKET'
               MOVE TKTFILE-STATUS TO IO-STATUS
               PERFORM 9910-DISPLAY-IO-STATUS
               PERFORM 9999-ABEND-PROGRAM
             END-IF
           END-IF
      *    Update category balance for centro
           MOVE DALYTICKET-CENTRO-ID TO FD-VCATBAL-CENTRO-ID
           MOVE DALYTICKET-TIPO-CD   TO FD-VCATBAL-TIPO-CD
           MOVE DALYTICKET-CAT-CD    TO FD-VCATBAL-CAT-CD
           READ VCATBAL-FILE INTO VENTA-CAT-BAL-RECORD
           IF  VCATBALF-STATUS = '00'
               ADD DALYTICKET-IMPORTE TO VENTA-CAT-BAL
               MOVE VENTA-CAT-BAL-RECORD TO FD-VENTA-CAT-BAL-RECORD
               REWRITE FD-VENTA-CAT-BAL-RECORD
           ELSE
      *        Create new VCATBAL record
               MOVE DALYTICKET-CENTRO-ID TO VCATBAL-CENTRO-ID
               MOVE DALYTICKET-TIPO-CD   TO VCATBAL-TIPO-CD
               MOVE DALYTICKET-CAT-CD    TO VCATBAL-CAT-CD
               MOVE DALYTICKET-IMPORTE   TO VENTA-CAT-BAL
               MOVE 0 TO VENTA-CAT-PUNTOS
               MOVE VENTA-CAT-BAL-RECORD TO FD-VENTA-CAT-BAL-RECORD
               WRITE FD-VENTA-CAT-BAL-RECORD
           END-IF
           .
      *---------------------------------------------------------------*
       2500-WRITE-REJECT-REC.
           MOVE DALYTICKET-RECORD TO REJECT-TRAN-DATA
           MOVE WS-VALIDATION-TRAILER TO VALIDATION-TRAILER
           WRITE FD-REJS-RECORD FROM REJECT-RECORD
           .
      *---------------------------------------------------------------*
       9000-DALYTICKET-CLOSE.
           CLOSE DALYTICKET-FILE.
       9100-TKTFILE-CLOSE.
           CLOSE TICKET-FILE.
       9200-XREFFILE-CLOSE.
           CLOSE XREF-FILE.
       9300-DALYREJS-CLOSE.
           CLOSE DALYREJS-FILE.
       9400-CNTFILE-CLOSE.
           CLOSE CENTRO-FILE.
       9500-VCATBALF-CLOSE.
           CLOSE VCATBAL-FILE.
      *---------------------------------------------------------------*
       9910-DISPLAY-IO-STATUS.
           MOVE IO-STAT1 TO TWO-BYTES-LEFT
           MOVE IO-STAT2 TO TWO-BYTES-RIGHT
           DISPLAY 'FILE STATUS: ' IO-STAT1 IO-STAT2
           .
      *---------------------------------------------------------------*
       9999-ABEND-PROGRAM.
           DISPLAY 'ABEND IN EITRN02C'
           MOVE 12 TO RETURN-CODE
           GOBACK
           .
