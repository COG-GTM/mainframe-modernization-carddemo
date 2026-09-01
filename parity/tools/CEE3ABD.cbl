      ******************************************************************
      * Harness stub (NOT legacy source) for the Language Environment
      * service called by app/cbl/CBTRN02C.cbl:711 (CALL 'CEE3ABD').
      * CEE3ABD terminates the enclave with the supplied abend code; the
      * emulator has no LE, so this stub reports and ends the run with a
      * non-zero code instead of resolving to nothing at run time.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CEE3ABD.

       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-MSG                   PIC X(40)
           VALUE 'CEE3ABD STUB: ENCLAVE TERMINATED'.

       PROCEDURE DIVISION.
           DISPLAY WS-MSG
           MOVE 12 TO RETURN-CODE
           STOP RUN.
