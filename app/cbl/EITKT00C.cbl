      ******************************************************************
      * Program     : EITKT00C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Ticket/Sale List
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EITKT00C.
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
             WHEN EIBAID = DFHPF7
               PERFORM 1200-PAGE-UP
             WHEN EIBAID = DFHPF8
               PERFORM 1300-PAGE-DOWN
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
             MAP('EITKT00')
             MAPSET('EITKT00')
             ERASE
           END-EXEC
           .
      *---------------------------------------------------------------*
       1200-PAGE-UP.
           PERFORM 1000-SEND-MAP
           .
      *---------------------------------------------------------------*
       1300-PAGE-DOWN.
           PERFORM 1000-SEND-MAP
           .
