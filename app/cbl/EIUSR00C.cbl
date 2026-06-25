      ******************************************************************
      * Program     : EIUSR00C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : List Users
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EIUSR00C.
       AUTHOR.        ECI-SISTEMAS.
       ENVIRONMENT DIVISION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-COMMAREA.
           COPY EICOM01Y.
       COPY EISEC01Y.
       COPY EIMSG01Y.
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
             WHEN OTHER
               PERFORM 1000-SEND-MAP
           END-EVALUATE.
           EXEC CICS RETURN
             TRANSID('EA00')
             COMMAREA(WS-COMMAREA)
           END-EXEC.
       1000-SEND-MAP.
           EXEC CICS SEND
             MAP('EIUSR00')
             MAPSET('EIUSR00')
             ERASE
           END-EXEC
           .
