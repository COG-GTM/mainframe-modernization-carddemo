      *****************************************************************
      * CEE3ABD - stand-in for the Language Environment abend service.
      *
      * Harness code. Every batch program in app/cbl ends its error
      * paths with CALL 'CEE3ABD' (for example CBTRN02C:711). Off the
      * mainframe that module does not exist, so the harness supplies one
      * that fails the step loudly instead of letting the run continue.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CEE3ABD.
       PROCEDURE DIVISION.
           DISPLAY 'CEE3ABD: ABEND REQUESTED BY THE CALLING PROGRAM'
           MOVE 3999 TO RETURN-CODE
           STOP RUN.
