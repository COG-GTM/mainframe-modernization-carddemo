      ******************************************************************
      * Program     : EISGN00C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Signon screen - authenticates users
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EISGN00C.
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
       01  WS-RESP2                 PIC S9(08) COMP.
       01  WS-ERR-MSG               PIC X(78) VALUE SPACES.
       01  WS-INPUT-USER            PIC X(08) VALUE SPACES.
       01  WS-INPUT-PASS            PIC X(08) VALUE SPACES.

       LINKAGE SECTION.
       01  DFHCOMMAREA              PIC X(500).

      *****************************************************************
       PROCEDURE DIVISION.
           IF EIBCALEN > 0
               MOVE DFHCOMMAREA TO WS-COMMAREA
           END-IF

           EVALUATE TRUE
             WHEN EIBCALEN = 0
               PERFORM 1000-SEND-SIGNON-MAP
             WHEN EIBAID = DFHENTER
               PERFORM 2000-PROCESS-SIGNON
             WHEN EIBAID = DFHPF3
               EXEC CICS SEND TEXT
                 FROM(ECI-MSG-THANK-YOU)
                 ERASE
               END-EXEC
               EXEC CICS RETURN END-EXEC
             WHEN OTHER
               MOVE ECI-MSG-INVALID-KEY TO WS-ERR-MSG
               PERFORM 1000-SEND-SIGNON-MAP
           END-EVALUATE.

           EXEC CICS RETURN
             TRANSID('EC00')
             COMMAREA(WS-COMMAREA)
           END-EXEC.

      *---------------------------------------------------------------*
       1000-SEND-SIGNON-MAP.
           EXEC CICS SEND
             MAP('EISGN00')
             MAPSET('EISGN00')
             ERASE
           END-EXEC
           .
      *---------------------------------------------------------------*
       2000-PROCESS-SIGNON.
           EXEC CICS RECEIVE
             MAP('EISGN00')
             MAPSET('EISGN00')
             INTO(WS-COMMAREA)
           END-EXEC

           MOVE WS-INPUT-USER TO SEC-USR-ID
           EXEC CICS READ
             FILE(WS-USRSEC-FILE)
             INTO(SEC-USER-RECORD)
             RIDFLD(SEC-USR-ID)
             RESP(WS-RESP)
           END-EXEC

           IF WS-RESP = DFHRESP(NORMAL)
             IF SEC-USR-PWD = WS-INPUT-PASS
               MOVE SEC-USR-ID TO CDEMO-USER-ID
               MOVE SEC-USR-TYPE TO CDEMO-USER-TYPE
               MOVE SEC-USR-CENTRO-ID TO CDEMO-USER-CENTRO-ID
               MOVE 'EC00' TO CDEMO-FROM-TRANID
               MOVE 'EISGN00C' TO CDEMO-FROM-PROGRAM
               IF SEC-USR-TYPE-ADMIN
                 MOVE 'EA00' TO CDEMO-TO-TRANID
                 MOVE 'EIADM01C' TO CDEMO-TO-PROGRAM
               ELSE
                 MOVE 'EM00' TO CDEMO-TO-TRANID
                 MOVE 'EIMEN01C' TO CDEMO-TO-PROGRAM
               END-IF
               EXEC CICS XCTL
                 PROGRAM(CDEMO-TO-PROGRAM)
                 COMMAREA(WS-COMMAREA)
               END-EXEC
             ELSE
               MOVE 'CONTRASENA INCORRECTA' TO WS-ERR-MSG
               PERFORM 1000-SEND-SIGNON-MAP
             END-IF
           ELSE
               MOVE 'USUARIO NO ENCONTRADO' TO WS-ERR-MSG
               PERFORM 1000-SEND-SIGNON-MAP
           END-IF
           .
