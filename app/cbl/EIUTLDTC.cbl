      ******************************************************************
      * Program     : EIUTLDTC.CBL
      * Application : ECIRetail
      * Type        : ONLINE COBOL Program (CICS)
      * Function    : Utility - Date/Time formatting for maps
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    EIUTLDTC.
       AUTHOR.        ECI-SISTEMAS.
       ENVIRONMENT DIVISION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       COPY EIDAT01Y.
       01  WS-DATE-WORK.
           05  WS-DATE-IN           PIC 9(08).
           05  WS-TIME-IN           PIC 9(08).
       LINKAGE SECTION.
       01  LS-DATE-OUT              PIC X(10).
       01  LS-TIME-OUT              PIC X(08).
      *****************************************************************
       PROCEDURE DIVISION USING LS-DATE-OUT LS-TIME-OUT.
           MOVE FUNCTION CURRENT-DATE TO WS-CURDATE-DATA
           STRING WS-CURDATE-YEAR '-' WS-CURDATE-MONTH '-'
                  WS-CURDATE-DAY DELIMITED BY SIZE
                  INTO LS-DATE-OUT
           END-STRING
           STRING WS-CURTIME-HOURS ':' WS-CURTIME-MINUTE ':'
                  WS-CURTIME-SECOND DELIMITED BY SIZE
                  INTO LS-TIME-OUT
           END-STRING
           GOBACK.
