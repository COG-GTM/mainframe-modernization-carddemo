      *****************************************************************
      * Test driver: CALLs the program named by RUNPGM_PROGRAM with a
      * JCL-style PARM area (halfword length + text from RUNPGM_PARM),
      * the way z/OS starts EXEC PGM=xxx,PARM='...'.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. RUNPGM.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 WS-PGM               PIC X(8)   VALUE SPACES.
       01 WS-PARM-TEXT         PIC X(100) VALUE SPACES.
       01 JCL-PARM.
          05 JCL-PARM-LEN      PIC S9(4) COMP.
          05 JCL-PARM-DATA     PIC X(100).
       PROCEDURE DIVISION.
           ACCEPT WS-PGM FROM ENVIRONMENT 'RUNPGM_PROGRAM'
           ACCEPT WS-PARM-TEXT FROM ENVIRONMENT 'RUNPGM_PARM'
           MOVE WS-PARM-TEXT TO JCL-PARM-DATA
           IF WS-PARM-TEXT = SPACES
              MOVE 0 TO JCL-PARM-LEN
           ELSE
              MOVE FUNCTION LENGTH(FUNCTION TRIM(WS-PARM-TEXT TRAILING))
                TO JCL-PARM-LEN
           END-IF
           CALL WS-PGM USING JCL-PARM
           STOP RUN.
