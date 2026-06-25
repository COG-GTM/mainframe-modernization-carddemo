      ******************************************************************
      * Program     : EIADM01C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Admin Menu - user administration functions
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EIADM01C.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-COMMAREA.
           COPY EICOM01Y.

       COPY EIMSG01Y.

       01  WS-OPTION               PIC X(02).
       01  WS-ERR-MSG              PIC X(78) VALUE SPACES.

       LINKAGE SECTION.
       01  DFHCOMMAREA             PIC X(500).

      *****************************************************************
       PROCEDURE DIVISION.
           MOVE DFHCOMMAREA TO WS-COMMAREA.

           EVALUATE TRUE
             WHEN EIBCALEN = 0
               PERFORM 1000-SEND-ADMIN-MAP
             WHEN EIBAID = DFHENTER
               PERFORM 2000-PROCESS-OPTION
             WHEN EIBAID = DFHPF3
               MOVE 'EC00' TO CDEMO-TO-TRANID
               EXEC CICS XCTL
                 PROGRAM('EISGN00C')
                 COMMAREA(WS-COMMAREA)
               END-EXEC
             WHEN OTHER
               MOVE ECI-MSG-INVALID-KEY TO WS-ERR-MSG
               PERFORM 1000-SEND-ADMIN-MAP
           END-EVALUATE.

           EXEC CICS RETURN
             TRANSID('EA00')
             COMMAREA(WS-COMMAREA)
           END-EXEC.

      *---------------------------------------------------------------*
       1000-SEND-ADMIN-MAP.
           EXEC CICS SEND
             MAP('EIADM01')
             MAPSET('EIADM01')
             ERASE
           END-EXEC
           .
      *---------------------------------------------------------------*
       2000-PROCESS-OPTION.
           EXEC CICS RECEIVE
             MAP('EIADM01')
             MAPSET('EIADM01')
           END-EXEC

           EVALUATE WS-OPTION
             WHEN 'UL'
               EXEC CICS XCTL
                 PROGRAM('EIUSR00C')
                 COMMAREA(WS-COMMAREA)
               END-EXEC
             WHEN 'UA'
               EXEC CICS XCTL
                 PROGRAM('EIUSR01C')
                 COMMAREA(WS-COMMAREA)
               END-EXEC
             WHEN 'UM'
               EXEC CICS XCTL
                 PROGRAM('EIUSR02C')
                 COMMAREA(WS-COMMAREA)
               END-EXEC
             WHEN 'UE'
               EXEC CICS XCTL
                 PROGRAM('EIUSR03C')
                 COMMAREA(WS-COMMAREA)
               END-EXEC
             WHEN OTHER
               MOVE 'OPCION NO VALIDA' TO WS-ERR-MSG
               PERFORM 1000-SEND-ADMIN-MAP
           END-EVALUATE
           .
