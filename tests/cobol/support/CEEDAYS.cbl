      *****************************************************************
      * Test double for the LE date service CEEDAYS.
      * Echoes the arguments it receives and returns the feedback code
      * named by the CEEDAYS_RESULT environment variable.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CEEDAYS.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01 WS-RESULT            PIC X(16).
       LINKAGE SECTION.
       01 LK-DATE.
          05 LK-DATE-LEN       PIC S9(4) BINARY.
          05 LK-DATE-TEXT      PIC X(256).
       01 LK-FMT.
          05 LK-FMT-LEN        PIC S9(4) BINARY.
          05 LK-FMT-TEXT       PIC X(256).
       01 LK-LILIAN            PIC S9(9) BINARY.
       01 LK-FEEDBACK.
          05 LK-TOKEN          PIC X(8).
          05 LK-ISINFO         PIC S9(9) BINARY.
       PROCEDURE DIVISION USING LK-DATE LK-FMT LK-LILIAN LK-FEEDBACK.
           DISPLAY 'CEEDAYS DATE:' LK-DATE-LEN ':'
                   LK-DATE-TEXT(1:LK-DATE-LEN)
           DISPLAY 'CEEDAYS FMT:' LK-FMT-LEN ':'
                   LK-FMT-TEXT(1:LK-FMT-LEN)
           MOVE SPACES TO WS-RESULT
           ACCEPT WS-RESULT FROM ENVIRONMENT 'CEEDAYS_RESULT'
           MOVE 0 TO LK-ISINFO
           EVALUATE WS-RESULT
             WHEN 'VALID'
               MOVE X'0000000000000000' TO LK-TOKEN
             WHEN 'INSUFFICIENT'
               MOVE X'000309CB59C3C5C5' TO LK-TOKEN
             WHEN 'BAD-DATE-VALUE'
               MOVE X'000309CC59C3C5C5' TO LK-TOKEN
             WHEN 'INVALID-ERA'
               MOVE X'000309CD59C3C5C5' TO LK-TOKEN
             WHEN 'UNSUPP-RANGE'
               MOVE X'000309D159C3C5C5' TO LK-TOKEN
             WHEN 'INVALID-MONTH'
               MOVE X'000309D559C3C5C5' TO LK-TOKEN
             WHEN 'BAD-PIC-STRING'
               MOVE X'000309D659C3C5C5' TO LK-TOKEN
             WHEN 'NON-NUMERIC'
               MOVE X'000309D859C3C5C5' TO LK-TOKEN
             WHEN 'YEAR-IN-ERA-0'
               MOVE X'000309D959C3C5C5' TO LK-TOKEN
             WHEN OTHER
               MOVE X'000409DA59C3C5C5' TO LK-TOKEN
           END-EVALUATE
           GOBACK.
