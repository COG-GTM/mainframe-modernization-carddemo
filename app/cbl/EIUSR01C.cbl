      ******************************************************************
      * Program     : EIUSR01C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Add User
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EIUSR01C.
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
               PERFORM 2000-ADD-USER
             WHEN OTHER
               PERFORM 1000-SEND-MAP
           END-EVALUATE.
           EXEC CICS RETURN
             TRANSID('EA00')
             COMMAREA(WS-COMMAREA)
           END-EXEC.
       1000-SEND-MAP.
           EXEC CICS SEND
             MAP('EIUSR01')
             MAPSET('EIUSR01')
             ERASE
           END-EXEC
           .
       2000-ADD-USER.
           EXEC CICS RECEIVE
             MAP('EIUSR01')
             MAPSET('EIUSR01')
           END-EXEC
           EXEC CICS WRITE
             FILE(WS-USRSEC-FILE)
             FROM(SEC-USER-RECORD)
             RIDFLD(SEC-USR-ID)
             RESP(WS-RESP)
           END-EXEC
           IF WS-RESP = DFHRESP(NORMAL)
             MOVE ECI-MSG-ADDED TO WS-ERR-MSG
           ELSE
             MOVE 'ERROR AL CREAR USUARIO' TO WS-ERR-MSG
           END-IF
           PERFORM 1000-SEND-MAP
           .
