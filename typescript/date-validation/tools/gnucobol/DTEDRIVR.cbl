      ******************************************************************
      * Test driver: runs the real CSUTLDPY paragraphs over a list of
      * dates so the TypeScript port can be compared against them.
      * Input line : mode (D = date, B = date of birth) + CCYYMMDD
      * Output line: mode|date|year-flg|month-flg|day-flg|err|message
      *              flags: V = LOW-VALUES (valid), 0 = not ok, B = blank
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. DTEDRIVR.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IN-FILE  ASSIGN TO 'DTEIN'
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT OUT-FILE ASSIGN TO 'DTEOUT'
                  ORGANIZATION IS LINE SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD  IN-FILE.
       01  IN-REC.
           05 IN-MODE                     PIC X(01).
           05 IN-DATE                     PIC X(08).
       FD  OUT-FILE.
       01  OUT-REC                        PIC X(120).
       WORKING-STORAGE SECTION.
       01  WS-EOF                         PIC X VALUE 'N'.
       01  WS-INPUT-FLAG                  PIC X(01).
           88  INPUT-OK                   VALUE '0'.
           88  INPUT-ERROR                VALUE '1'.
       01  WS-RETURN-MSG                  PIC X(75).
           88  WS-RETURN-MSG-OFF          VALUE SPACES.
       01  WS-VARS.
           05 WS-EDIT-VARIABLE-NAME       PIC X(25).
           05 WS-DIV-BY                   PIC S9(4) COMP-3 VALUE 4.
           05 WS-DIVIDEND                 PIC S9(4) COMP-3 VALUE 0.
           05 WS-REMAINDER                PIC S9(4) COMP-3 VALUE 0.
       01  WS-DATE-WORK.
           COPY CSUTLDWY.
       01  WS-OUT-FLAGS.
           05 WS-OUT-FLG                  PIC X OCCURS 3.
       01  WS-I                           PIC 9.
       PROCEDURE DIVISION.
       MAIN-PARA.
           OPEN INPUT IN-FILE OUTPUT OUT-FILE
           PERFORM UNTIL WS-EOF = 'Y'
              READ IN-FILE
                 AT END MOVE 'Y' TO WS-EOF
                 NOT AT END PERFORM ONE-DATE
              END-READ
           END-PERFORM
           CLOSE IN-FILE OUT-FILE
           STOP RUN.
       ONE-DATE.
           SET INPUT-OK TO TRUE
           SET WS-RETURN-MSG-OFF TO TRUE
           IF IN-MODE = 'B'
              MOVE 'Date of Birth' TO WS-EDIT-VARIABLE-NAME
           ELSE
              MOVE 'Date'          TO WS-EDIT-VARIABLE-NAME
           END-IF
           MOVE IN-DATE TO WS-EDIT-DATE-CCYYMMDD
           PERFORM EDIT-DATE-CCYYMMDD THRU EDIT-DATE-CCYYMMDD-EXIT
           IF IN-MODE = 'B' AND WS-EDIT-DATE-IS-VALID
              PERFORM EDIT-DATE-OF-BIRTH THRU EDIT-DATE-OF-BIRTH-EXIT
           END-IF
           MOVE WS-EDIT-DATE-FLGS TO WS-OUT-FLAGS
           PERFORM VARYING WS-I FROM 1 BY 1 UNTIL WS-I > 3
              IF WS-OUT-FLG (WS-I) = LOW-VALUE
                 MOVE 'V' TO WS-OUT-FLG (WS-I)
              END-IF
           END-PERFORM
           MOVE SPACES TO OUT-REC
           STRING IN-MODE '|' IN-DATE '|'
                  WS-OUT-FLG (1) '|' WS-OUT-FLG (2) '|'
                  WS-OUT-FLG (3) '|' WS-INPUT-FLAG '|'
                  WS-RETURN-MSG
                  DELIMITED BY SIZE INTO OUT-REC
           WRITE OUT-REC.
           COPY CSUTLDPY.
