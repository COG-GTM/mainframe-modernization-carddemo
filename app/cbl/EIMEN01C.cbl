      ******************************************************************
      * Program     : EIMEN01C.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Main Menu - navigation hub for user functions
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EIMEN01C.
       AUTHOR.        ECI-SISTEMAS.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-COMMAREA.
           COPY EICOM01Y.

       COPY EIMSG01Y.

       01  WS-OPTION               PIC X(02).
       01  WS-ERR-MSG              PIC X(78) VALUE SPACES.
       01  WS-PROGRAM-TO-CALL      PIC X(08).

       LINKAGE SECTION.
       01  DFHCOMMAREA             PIC X(500).

      *****************************************************************
       PROCEDURE DIVISION.
           MOVE DFHCOMMAREA TO WS-COMMAREA.

           EVALUATE TRUE
             WHEN EIBCALEN = 0
               PERFORM 1000-SEND-MENU-MAP
             WHEN EIBAID = DFHENTER
               PERFORM 2000-PROCESS-OPTION
             WHEN EIBAID = DFHPF3
               MOVE 'EC00' TO CDEMO-TO-TRANID
               MOVE 'EISGN00C' TO CDEMO-TO-PROGRAM
               EXEC CICS XCTL
                 PROGRAM('EISGN00C')
                 COMMAREA(WS-COMMAREA)
               END-EXEC
             WHEN OTHER
               MOVE ECI-MSG-INVALID-KEY TO WS-ERR-MSG
               PERFORM 1000-SEND-MENU-MAP
           END-EVALUATE.

           EXEC CICS RETURN
             TRANSID('EM00')
             COMMAREA(WS-COMMAREA)
           END-EXEC.

      *---------------------------------------------------------------*
       1000-SEND-MENU-MAP.
           EXEC CICS SEND
             MAP('EIMEN01')
             MAPSET('EIMEN01')
             ERASE
           END-EXEC
           .
      *---------------------------------------------------------------*
       2000-PROCESS-OPTION.
           EXEC CICS RECEIVE
             MAP('EIMEN01')
             MAPSET('EIMEN01')
           END-EXEC

           EVALUATE WS-OPTION
             WHEN 'CV'
               MOVE 'EICNTVWC' TO WS-PROGRAM-TO-CALL
             WHEN 'CU'
               MOVE 'EICNTUPC' TO WS-PROGRAM-TO-CALL
             WHEN 'TL'
               MOVE 'EITJFLIC' TO WS-PROGRAM-TO-CALL
             WHEN 'TV'
               MOVE 'EITJFSLC' TO WS-PROGRAM-TO-CALL
             WHEN 'VL'
               MOVE 'EITKT00C' TO WS-PROGRAM-TO-CALL
             WHEN 'VA'
               MOVE 'EITKT02C' TO WS-PROGRAM-TO-CALL
             WHEN 'RI'
               MOVE 'EIRPT00C' TO WS-PROGRAM-TO-CALL
             WHEN OTHER
               MOVE 'OPCION NO VALIDA' TO WS-ERR-MSG
               PERFORM 1000-SEND-MENU-MAP
               EXEC CICS RETURN
                 TRANSID('EM00')
                 COMMAREA(WS-COMMAREA)
               END-EXEC
           END-EVALUATE

           MOVE 'EM00' TO CDEMO-FROM-TRANID
           MOVE 'EIMEN01C' TO CDEMO-FROM-PROGRAM
           EXEC CICS XCTL
             PROGRAM(WS-PROGRAM-TO-CALL)
             COMMAREA(WS-COMMAREA)
           END-EXEC
           .
