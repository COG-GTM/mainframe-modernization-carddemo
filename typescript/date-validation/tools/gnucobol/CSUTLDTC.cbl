      ******************************************************************
      * Stand-in for CSUTLDTC: CEEDAYS (IBM LE) is not available under
      * GnuCOBOL. Always reports severity 0, so EDIT-DATE-LE passes.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CSUTLDTC.
       DATA DIVISION.
       LINKAGE SECTION.
       01 LS-DATE         PIC X(10).
       01 LS-DATE-FORMAT  PIC X(10).
       01 LS-RESULT       PIC X(80).
       PROCEDURE DIVISION USING LS-DATE, LS-DATE-FORMAT, LS-RESULT.
           MOVE '0000' TO LS-RESULT(1:4)
           MOVE 0 TO RETURN-CODE
           GOBACK.
