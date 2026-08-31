      *****************************************************************
      * INTDRV - stands in for the job step that starts CBACT04C.
      *
      * INTCALC.jcl STEP15 runs the program as
      *     EXEC PGM=CBACT04C,PARM='2022071800'
      * and CBACT04C declares PROCEDURE DIVISION USING EXTERNAL-PARMS
      * (CBACT04C:175-180), a halfword length followed by a 10-character
      * date. This driver builds the same parameter list from the command
      * line and CALLs the unmodified program. Harness code.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. INTDRV.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-ARG                      PIC X(10) VALUE SPACES.
       01  WS-PARM.
           05  WS-PARM-LENGTH          PIC S9(04) COMP.
           05  WS-PARM-DATE            PIC X(10).
       PROCEDURE DIVISION.
           ACCEPT WS-ARG FROM COMMAND-LINE
           IF WS-ARG = SPACES
              DISPLAY 'INTDRV: PARM DATE ARGUMENT IS REQUIRED'
              MOVE 16 TO RETURN-CODE
              STOP RUN
           END-IF
           MOVE 10 TO WS-PARM-LENGTH
           MOVE WS-ARG TO WS-PARM-DATE
           CALL 'CBACT04C' USING WS-PARM
           GOBACK.
