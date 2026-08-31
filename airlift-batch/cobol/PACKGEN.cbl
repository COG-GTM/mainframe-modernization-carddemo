      *****************************************************************
      * PACKGEN - write codec test vectors using the compiler itself.
      *
      * Emits one binary AIRPACK-RECORD per case (DD_OUTFILE) plus a
      * human-readable line with the same values rendered by COBOL
      * numeric-edited MOVEs (DD_TEXTFILE). Both codecs in this harness
      * must decode the binary bytes to exactly the printed values, so the
      * expectation for packed-decimal, binary and zoned encodings comes
      * from GnuCOBOL rather than from an assumption in the test.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. PACKGEN.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT VECT-FILE ASSIGN TO OUTFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT TEXT-FILE ASSIGN TO TEXTFILE
                  ORGANIZATION IS LINE SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  VECT-FILE.
       01  VECT-REC                    PIC X(51).
       FD  TEXT-FILE.
       01  TEXT-REC                    PIC X(80).
       WORKING-STORAGE SECTION.
       COPY AIRPACK.
       01  WS-STATUS                   PIC X(02).
       01  WS-LINE.
           05  WL-LABEL                PIC X(16).
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-ZONED-SIGNED         PIC +9(9).99.
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-ZONED-UNSIGNED       PIC 9(7).
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-COMP3-SIGNED         PIC +9(9).99.
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-COMP3-UNSIGNED       PIC 9(5).999.
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-COMP-HALF            PIC +9(4).
           05  FILLER                  PIC X VALUE SPACE.
           05  WL-COMP-FULL            PIC +9(9).
       PROCEDURE DIVISION.
           OPEN OUTPUT VECT-FILE
           PERFORM CHECK-STATUS
           OPEN OUTPUT TEXT-FILE
           PERFORM CHECK-STATUS

           MOVE 'ZERO'             TO AP-LABEL
           MOVE 0                  TO AP-ZONED-SIGNED
           MOVE 0                  TO AP-ZONED-UNSIGNED
           MOVE 0                  TO AP-COMP3-SIGNED
           MOVE 0                  TO AP-COMP3-UNSIGNED
           MOVE 0                  TO AP-COMP-HALF
           MOVE 0                  TO AP-COMP-FULL
           PERFORM EMIT-CASE

           MOVE 'POSITIVE'         TO AP-LABEL
           MOVE 1234567.89         TO AP-ZONED-SIGNED
           MOVE 1234567            TO AP-ZONED-UNSIGNED
           MOVE 1234567.89         TO AP-COMP3-SIGNED
           MOVE 12345.678          TO AP-COMP3-UNSIGNED
           MOVE 1234               TO AP-COMP-HALF
           MOVE 123456789          TO AP-COMP-FULL
           PERFORM EMIT-CASE

           MOVE 'NEGATIVE'         TO AP-LABEL
           MOVE -1234567.89        TO AP-ZONED-SIGNED
           MOVE 7654321            TO AP-ZONED-UNSIGNED
           MOVE -1234567.89        TO AP-COMP3-SIGNED
           MOVE 99999.999          TO AP-COMP3-UNSIGNED
           MOVE -1234              TO AP-COMP-HALF
           MOVE -123456789         TO AP-COMP-FULL
           PERFORM EMIT-CASE

           MOVE 'SMALL-CENTS'      TO AP-LABEL
           MOVE 0.01               TO AP-ZONED-SIGNED
           MOVE 1                  TO AP-ZONED-UNSIGNED
           MOVE -0.01              TO AP-COMP3-SIGNED
           MOVE 0.001              TO AP-COMP3-UNSIGNED
           MOVE 1                  TO AP-COMP-HALF
           MOVE -1                 TO AP-COMP-FULL
           PERFORM EMIT-CASE

           MOVE 'MAXIMUM'          TO AP-LABEL
           MOVE 999999999.99       TO AP-ZONED-SIGNED
           MOVE 9999999            TO AP-ZONED-UNSIGNED
           MOVE 999999999.99       TO AP-COMP3-SIGNED
           MOVE 99999.999          TO AP-COMP3-UNSIGNED
           MOVE 9999               TO AP-COMP-HALF
           MOVE 999999999          TO AP-COMP-FULL
           PERFORM EMIT-CASE

           MOVE 'MINIMUM'          TO AP-LABEL
           MOVE -999999999.99      TO AP-ZONED-SIGNED
           MOVE 0                  TO AP-ZONED-UNSIGNED
           MOVE -999999999.99      TO AP-COMP3-SIGNED
           MOVE 0.999              TO AP-COMP3-UNSIGNED
           MOVE -9999              TO AP-COMP-HALF
           MOVE -999999999         TO AP-COMP-FULL
           PERFORM EMIT-CASE

           CLOSE VECT-FILE
           CLOSE TEXT-FILE
           DISPLAY 'PACKGEN: VECTORS WRITTEN'
           GOBACK.

       EMIT-CASE.
           WRITE VECT-REC FROM AIRPACK-RECORD
           PERFORM CHECK-STATUS
           MOVE AP-LABEL           TO WL-LABEL
           MOVE AP-ZONED-SIGNED    TO WL-ZONED-SIGNED
           MOVE AP-ZONED-UNSIGNED  TO WL-ZONED-UNSIGNED
           MOVE AP-COMP3-SIGNED    TO WL-COMP3-SIGNED
           MOVE AP-COMP3-UNSIGNED  TO WL-COMP3-UNSIGNED
           MOVE AP-COMP-HALF       TO WL-COMP-HALF
           MOVE AP-COMP-FULL       TO WL-COMP-FULL
           MOVE WS-LINE            TO TEXT-REC
           WRITE TEXT-REC
           PERFORM CHECK-STATUS
           EXIT.

       CHECK-STATUS.
           IF WS-STATUS NOT = '00'
              DISPLAY 'PACKGEN: FILE STATUS ' WS-STATUS
              MOVE 12 TO RETURN-CODE
              STOP RUN
           END-IF
           EXIT.
