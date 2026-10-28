      *****************************************************************
      * Test double for the LE abend service CEE3ABD.
      * Ends the run unit with RETURN-CODE 99 so tests can detect an
      * abend without a mainframe runtime.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CEE3ABD.
       PROCEDURE DIVISION.
           DISPLAY 'CEE3ABD: ABEND U0999'
           MOVE 99 TO RETURN-CODE
           STOP RUN.
