             88  HTML-L61
               VALUE '<td style="width:55%; padding:0px 5px; background-
      -              'color:#f2f2f2; text-align:left;">'.
             88  HTML-L64
               VALUE '<td style="width:20%; padding:0px 5px; background-
      -              'color:#f2f2f2; text-align:right;">'.
             88  HTML-L75
               VALUE '<h3>End of Statement</h3>'.
             88  HTML-L78 VALUE '</table>'.
             88  HTML-L79 VALUE '</body>'.
             88  HTML-L80 VALUE '</html>'.
           05  HTML-L11.
               10  FILLER   PIC X(34)
                          VALUE '<h3>Statement for Account Number: '.
               10  L11-ACCT PIC X(20).
               10  FILLER   PIC X(05) VALUE '</h3>'.
           05  HTML-L23.
               10  FILLER   PIC X(26)
                          VALUE '<p style="font-size:16px">'.
               10  L23-NAME PIC X(50).
           05  HTML-ADDR-LN PIC X(100).
           05  HTML-BSIC-LN PIC X(100).
           05  HTML-TRAN-LN PIC X(100).

       01  WS-TRNX-TABLE.
           05  WS-CARD-TBL OCCURS 51 TIMES.
               10  WS-CARD-NUM                          PIC X(16).
               10  WS-TRAN-TBL OCCURS 10 TIMES.
                   15  WS-TRAN-NUM                      PIC X(16).
                   15  WS-TRAN-REST                     PIC X(318).
       01  WS-TRN-TBL-CNTR.
           05  WS-TRN-TBL-CTR OCCURS 51 TIMES.
               10  WS-TRCT               PIC S9(4) COMP.

       01  PSAPTR                  POINTER.
       01  BUMP-TIOT               PIC S9(08) BINARY VALUE ZERO.
       01  TIOT-INDEX              REDEFINES BUMP-TIOT POINTER.

       LINKAGE SECTION.
       01  ALIGN-PSA        PIC 9(16) BINARY.
       01  PSA-BLOCK.
           05  FILLER       PIC X(536).
           05  TCB-POINT    POINTER.
       01  TCB-BLOCK.
           05  FILLER       PIC X(12).
           05  TIOT-POINT   POINTER.
       01  TIOT-BLOCK.
           05  TIOTNJOB     PIC X(08).
           05  TIOTJSTP     PIC X(08).
           05  TIOTPSTP     PIC X(08).
       01  TIOT-ENTRY.
           05  TIOT-SEG.
               10  TIO-LEN  PIC X(01).
               10  FILLER   PIC X(03).
               10  TIOCDDNM PIC X(08).
               10  FILLER   PIC X(05).
               10  UCB-ADDR PIC X(03).
                   88 NULL-UCB    VALUES LOW-VALUES.
           05  FILLER       PIC X(04).
             88  END-OF-TIOT      VALUE LOW-VALUES.
      *****************************************************************
       PROCEDURE DIVISION.
      *****************************************************************
      *    Check Unit Control blocks                                  *
      *****************************************************************
           SET ADDRESS OF PSA-BLOCK   TO PSAPTR.
           SET ADDRESS OF TCB-BLOCK   TO TCB-POINT.
           SET ADDRESS OF TIOT-BLOCK  TO TIOT-POINT.
           SET TIOT-INDEX             TO TIOT-POINT.
           DISPLAY 'Running JCL : ' TIOTNJOB ' Step ' TIOTJSTP.

           COMPUTE BUMP-TIOT = BUMP-TIOT + LENGTH OF TIOT-BLOCK.
           SET ADDRESS OF TIOT-ENTRY  TO TIOT-INDEX.

           DISPLAY 'DD Names from TIOT: '.
           PERFORM UNTIL END-OF-TIOT
                      OR TIO-LEN = LOW-VALUES
               IF NOT NULL-UCB
                   DISPLAY ': ' TIOCDDNM ' -- valid UCB'
               ELSE
                   DISPLAY ': ' TIOCDDNM ' --  null UCB'
               END-IF
               COMPUTE BUMP-TIOT = BUMP-TIOT + LENGTH OF TIOT-SEG
               SET ADDRESS OF TIOT-ENTRY TO TIOT-INDEX
           END-PERFORM.

           IF NOT NULL-UCB
               DISPLAY ': ' TIOCDDNM ' -- valid UCB'
           ELSE
               DISPLAY ': ' TIOCDDNM ' -- null  UCB'
           END-IF.

           OPEN OUTPUT STMT-FILE HTML-FILE.
           INITIALIZE WS-TRNX-TABLE WS-TRN-TBL-CNTR.

       0000-START.

           EVALUATE WS-FL-DD
             WHEN 'TRNXFILE'
               ALTER 8100-FILE-OPEN TO PROCEED TO 8100-TRNXFILE-OPEN
