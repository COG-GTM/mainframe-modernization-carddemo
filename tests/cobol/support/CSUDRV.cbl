      *****************************************************************
      * Test driver for CSUTLDTC: passes CSUDRV_DATE / CSUDRV_FORMAT and
      * prints the 80-byte result and the RETURN-CODE it sets.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CSUDRV.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 WS-DATE              PIC X(10)  VALUE SPACES.
       01 WS-FORMAT            PIC X(10)  VALUE SPACES.
       01 WS-RESULT            PIC X(80)  VALUE SPACES.
       01 WS-RC                PIC -9(4).
       PROCEDURE DIVISION.
           ACCEPT WS-DATE FROM ENVIRONMENT 'CSUDRV_DATE'
           ACCEPT WS-FORMAT FROM ENVIRONMENT 'CSUDRV_FORMAT'
           CALL 'CSUTLDTC' USING WS-DATE WS-FORMAT WS-RESULT
           MOVE RETURN-CODE TO WS-RC
           DISPLAY 'RESULT:' WS-RESULT
           DISPLAY 'RC:' WS-RC
           MOVE 0 TO RETURN-CODE
           STOP RUN.
