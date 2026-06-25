      ******************************************************************
      * Program     : EITKT02C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Ticket/Sale Add (manual entry from POS)
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EITKT02C.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-COMMAREA.
           COPY EICOM01Y.

       COPY EITKT05Y.
       COPY EICNT01Y.
       COPY EIMSG01Y.

       01  WS-TICKET-FILE          PIC X(08) VALUE 'TICKTFL '.
       01  WS-CENTRO-FILE          PIC X(08) VALUE 'CNTROFL '.
       01  WS-RESP                  PIC S9(08) COMP.
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
               PERFORM 2000-ADD-TICKET
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
             MAP('EITKT02')
             MAPSET('EITKT02')
             ERASE
           END-EXEC
           .
      *---------------------------------------------------------------*
       2000-ADD-TICKET.
           EXEC CICS RECEIVE
             MAP('EITKT02')
             MAPSET('EITKT02')
           END-EXEC
      *    Validate centro
           MOVE TICKET-CENTRO-ID TO CENTRO-ID
           EXEC CICS READ
             FILE(WS-CENTRO-FILE)
             INTO(CENTRO-RECORD)
             RIDFLD(CENTRO-ID)
             RESP(WS-RESP)
           END-EXEC
           IF WS-RESP NOT = DFHRESP(NORMAL)
             MOVE 'CENTRO NO VALIDO' TO WS-ERR-MSG
             PERFORM 1000-SEND-MAP
             EXEC CICS RETURN
               TRANSID('EM00')
               COMMAREA(WS-COMMAREA)
             END-EXEC
           END-IF
      *    Write new ticket
           EXEC CICS WRITE
             FILE(WS-TICKET-FILE)
             FROM(TICKET-RECORD)
             RIDFLD(TICKET-ID)
             RESP(WS-RESP)
           END-EXEC
           IF WS-RESP = DFHRESP(NORMAL)
             MOVE ECI-MSG-ADDED TO WS-ERR-MSG
           ELSE
             MOVE 'ERROR AL GRABAR TICKET' TO WS-ERR-MSG
           END-IF
           PERFORM 1000-SEND-MAP
           .
