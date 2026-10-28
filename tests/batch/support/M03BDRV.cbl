      *****************************************************************
      * Test driver for CBSTM03B: replays the operations listed in the
      * M03BOPS file (DD(8) OPER(1) KEY-LN(4) KEY(25) per line) and
      * prints DD|OPER|RC|data for each call.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. M03BDRV.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT OPS-FILE ASSIGN TO M03BOPS
                  ORGANIZATION IS LINE SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD OPS-FILE.
       01 OPS-REC.
          05 OPS-DD            PIC X(8).
          05 OPS-OPER          PIC X(1).
          05 OPS-KEY-LN        PIC 9(4).
          05 OPS-KEY           PIC X(25).
       WORKING-STORAGE SECTION.
       01 WS-EOF               PIC X VALUE 'N'.
       01 WS-M03B-AREA.
          05 WS-M03B-DD        PIC X(08).
          05 WS-M03B-OPER      PIC X(01).
          05 WS-M03B-RC        PIC X(02).
          05 WS-M03B-KEY       PIC X(25).
          05 WS-M03B-KEY-LN    PIC S9(4).
          05 WS-M03B-FLDT      PIC X(1000).
       PROCEDURE DIVISION.
           OPEN INPUT OPS-FILE
           PERFORM UNTIL WS-EOF = 'Y'
             READ OPS-FILE
               AT END MOVE 'Y' TO WS-EOF
               NOT AT END
                 MOVE OPS-DD     TO WS-M03B-DD
                 MOVE OPS-OPER   TO WS-M03B-OPER
                 MOVE OPS-KEY    TO WS-M03B-KEY
                 MOVE OPS-KEY-LN TO WS-M03B-KEY-LN
                 MOVE 'XX'       TO WS-M03B-RC
                 MOVE SPACES     TO WS-M03B-FLDT
                 CALL 'CBSTM03B' USING WS-M03B-AREA
                 DISPLAY WS-M03B-DD '|' WS-M03B-OPER '|' WS-M03B-RC
                         '|' FUNCTION TRIM(WS-M03B-FLDT TRAILING)
             END-READ
           END-PERFORM
           CLOSE OPS-FILE
           STOP RUN.
