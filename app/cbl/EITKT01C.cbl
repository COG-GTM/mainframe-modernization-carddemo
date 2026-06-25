      ******************************************************************
      * Program     : EITKT01C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Ticket/Sale View Detail
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EITKT01C.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-COMMAREA.
           COPY EICOM01Y.

       COPY EITKT05Y.
       COPY EIMSG01Y.

       01  WS-TICKET-FILE          PIC X(08) VALUE 'TICKTFL '.
       01  WS-RESP                  PIC S9(08) COMP.
       01  WS-INPUT-TICKET-ID       PIC X(16).
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
               PERFORM 2000-VIEW-TICKET
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
             MAP('EITKT01')
             MAPSET('EITKT01')
             ERASE
           END-EXEC
           .
      *---------------------------------------------------------------*
       2000-VIEW-TICKET.
           EXEC CICS RECEIVE
             MAP('EITKT01')
             MAPSET('EITKT01')
           END-EXEC

           MOVE WS-INPUT-TICKET-ID TO TICKET-ID
           EXEC CICS READ
             FILE(WS-TICKET-FILE)
             INTO(TICKET-RECORD)
             RIDFLD(TICKET-ID)
             RESP(WS-RESP)
           END-EXEC

           IF WS-RESP = DFHRESP(NORMAL)
             PERFORM 1000-SEND-MAP
           ELSE
             MOVE ECI-MSG-NOT-FOUND TO WS-ERR-MSG
             PERFORM 1000-SEND-MAP
           END-IF
           .
