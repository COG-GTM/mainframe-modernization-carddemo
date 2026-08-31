      *****************************************************************
      * AIRCLOCK - deterministic replacement for FUNCTION CURRENT-DATE.
      *
      * Harness code. The slice programs in app/cbl are compiled from
      * unmodified source apart from one substitution performed by
      * airlift-batch/tools/preclock.py, which rewrites
      *     MOVE FUNCTION CURRENT-DATE TO COBOL-TS
      * (CBTRN02C:693, CBACT04C:614) into a CALL of this module so both
      * sides of the harness can agree on TRAN-PROC-TS. The returned
      * layout is the 21-character CURRENT-DATE format:
      * YYYYMMDDhhmmsshh+hhmm.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. AIRCLOCK.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-CLOCK                    PIC X(21) VALUE SPACES.
       LINKAGE SECTION.
       01  LK-CURRENT-DATE             PIC X(21).
       PROCEDURE DIVISION USING LK-CURRENT-DATE.
           ACCEPT WS-CLOCK FROM ENVIRONMENT "AIRLIFT_CLOCK"
           IF WS-CLOCK = SPACES
              DISPLAY 'AIRCLOCK: AIRLIFT_CLOCK IS NOT SET'
              MOVE 16 TO RETURN-CODE
              STOP RUN
           END-IF
           MOVE WS-CLOCK TO LK-CURRENT-DATE
           GOBACK.
