      ******************************************************************
      * Program     : EIRPT00C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Transaction Reports (online request)
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EIRPT00C.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-COMMAREA.
           COPY EICOM01Y.

       COPY EIMSG01Y.

       01  WS-ERR-MSG               PIC X(78) VALUE SPACES.

       LINKAGE SECTION.
       01  DFHCOMMAREA              PIC X(500).

      *****************************************************************
       PROCEDURE DIVISION.
           MOVE DFHCOMMAREA TO WS-COMMAREA.

           EVALUATE TRUE
             WHEN EIBAID = DFHPF3
               EXEC CICS XCTL
                 PROGRAM('EIMEN01C')
                 COMMAREA(WS-COMMAREA)
               END-EXEC
             WHEN EIBAID = DFHENTER
               PERFORM 2000-SUBMIT-REPORT
             WHEN OTHER
               PERFORM 1000-SEND-MAP
           END-EVALUATE.

           EXEC CICS RETURN
             TRANSID('EM00')
             COMMAREA(WS-COMMAREA)
           END-EXEC.

      *---------------------------------------------------------------*
       1000-SEND-MAP.
           EXEC CICS SEND
             MAP('EIRPT00')
             MAPSET('EIRPT00')
             ERASE
           END-EXEC
           .
      *---------------------------------------------------------------*
       2000-SUBMIT-REPORT.
           EXEC CICS RECEIVE
             MAP('EIRPT00')
             MAPSET('EIRPT00')
           END-EXEC
           MOVE 'INFORME ENVIADO A IMPRESION' TO WS-ERR-MSG
           PERFORM 1000-SEND-MAP
           .
