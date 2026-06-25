      ******************************************************************
      * Program     : EITJFSLC.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Loyalty Card View/Detail
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EITJFSLC.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-COMMAREA.
           COPY EICOM01Y.

       COPY EITJF01Y.
       COPY EIMSG01Y.

       01  WS-TARJETA-FILE         PIC X(08) VALUE 'TARJFLFL'.
       01  WS-RESP                  PIC S9(08) COMP.
       01  WS-INPUT-TARJ-NUM        PIC X(16).
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
               PERFORM 2000-VIEW-TARJETA
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
             MAP('EITJFSL')
             MAPSET('EITJFSL')
             ERASE
           END-EXEC
           .
      *---------------------------------------------------------------*
       2000-VIEW-TARJETA.
           EXEC CICS RECEIVE
             MAP('EITJFSL')
             MAPSET('EITJFSL')
           END-EXEC

           MOVE WS-INPUT-TARJ-NUM TO TARJ-NUM
           EXEC CICS READ
             FILE(WS-TARJETA-FILE)
             INTO(TARJETA-RECORD)
             RIDFLD(TARJ-NUM)
             RESP(WS-RESP)
           END-EXEC

           IF WS-RESP = DFHRESP(NORMAL)
             PERFORM 1000-SEND-MAP
           ELSE
             MOVE ECI-MSG-NOT-FOUND TO WS-ERR-MSG
             PERFORM 1000-SEND-MAP
           END-IF
           .
