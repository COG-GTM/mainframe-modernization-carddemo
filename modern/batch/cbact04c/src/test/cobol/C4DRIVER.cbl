      *****************************************************************
      * Test harness for CBACT04C under GnuCOBOL: passes the JCL PARM
      * (first command-line argument) the way z/OS does.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. C4DRIVER.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  EXTERNAL-PARMS.
           05  PARM-LENGTH         PIC S9(04) COMP VALUE 10.
           05  PARM-DATE           PIC X(10).
       PROCEDURE DIVISION.
           ACCEPT PARM-DATE FROM ARGUMENT-VALUE
           CALL 'CBACT04C' USING EXTERNAL-PARMS
           GOBACK.
