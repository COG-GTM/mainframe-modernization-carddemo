      ******************************************************************
      * Program     : EICNTVWC.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Centro/Store View
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EICNTVWC.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-COMMAREA.
           COPY EICOM01Y.

       COPY EICNT01Y.
       COPY EIMSG01Y.

       01  WS-CENTRO-FILE          PIC X(08) VALUE 'CNTROFL '.
       01  WS-RESP                  PIC S9(08) COMP.
       01  WS-INPUT-CENTRO-ID       PIC 9(05).
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
               PERFORM 2000-GET-CENTRO
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
             MAP('EICNTVW')
             MAPSET('EICNTVW')
             ERASE
           END-EXEC
           .
      *---------------------------------------------------------------*
       2000-GET-CENTRO.
           EXEC CICS RECEIVE
             MAP('EICNTVW')
             MAPSET('EICNTVW')
           END-EXEC

           MOVE WS-INPUT-CENTRO-ID TO CENTRO-ID
           EXEC CICS READ
             FILE(WS-CENTRO-FILE)
             INTO(CENTRO-RECORD)
             RIDFLD(CENTRO-ID)
             RESP(WS-RESP)
           END-EXEC

           IF WS-RESP = DFHRESP(NORMAL)
             PERFORM 1000-SEND-MAP
           ELSE
             MOVE ECI-MSG-NOT-FOUND TO WS-ERR-MSG
             PERFORM 1000-SEND-MAP
           END-IF
           .
