      ******************************************************************
      * Program     : EIUSR03C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Delete User
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EIUSR03C.
       AUTHOR.        ECI-SISTEMAS.
       ENVIRONMENT DIVISION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-COMMAREA.
           COPY EICOM01Y.
       COPY EISEC01Y.
       COPY EIMSG01Y.
       01  WS-USRSEC-FILE          PIC X(08) VALUE 'USRSECFL'.
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
                 PROGRAM('EIADM01C')
                 COMMAREA(WS-COMMAREA)
               END-EXEC
             WHEN EIBAID = DFHENTER
               PERFORM 2000-DELETE-USER
             WHEN OTHER
               PERFORM 1000-SEND-MAP
           END-EVALUATE.
           EXEC CICS RETURN
             TRANSID('EA00')
             COMMAREA(WS-COMMAREA)
           END-EXEC.
       1000-SEND-MAP.
           EXEC CICS SEND
             MAP('EIUSR03')
             MAPSET('EIUSR03')
             ERASE
           END-EXEC
           .
       2000-DELETE-USER.
           EXEC CICS RECEIVE
             MAP('EIUSR03')
             MAPSET('EIUSR03')
           END-EXEC
           EXEC CICS READ
             FILE(WS-USRSEC-FILE)
             INTO(SEC-USER-RECORD)
             RIDFLD(SEC-USR-ID)
             UPDATE
             RESP(WS-RESP)
           END-EXEC
           IF WS-RESP = DFHRESP(NORMAL)
             EXEC CICS DELETE
               FILE(WS-USRSEC-FILE)
               RESP(WS-RESP)
             END-EXEC
             MOVE ECI-MSG-DELETED TO WS-ERR-MSG
           ELSE
             MOVE ECI-MSG-NOT-FOUND TO WS-ERR-MSG
           END-IF
           PERFORM 1000-SEND-MAP
           .
