                   ' : Month must be supplied.'
                   DELIMITED BY SIZE
                   INTO WS-RETURN-MSG
              END-IF
              GO TO EDIT-MONTH-EXIT
           ELSE
              CONTINUE
           END-IF

      *    Month not reasonable
           IF WS-VALID-MONTH
              CONTINUE
           ELSE
              SET INPUT-ERROR              TO TRUE
              SET  FLG-MONTH-NOT-OK        TO TRUE
              IF WS-RETURN-MSG-OFF
                 STRING
                   FUNCTION TRIM(WS-EDIT-VARIABLE-NAME)
                   ': Month must be a number between 1 and 12.'
                   DELIMITED BY SIZE
                   INTO WS-RETURN-MSG
              END-IF
              GO TO EDIT-MONTH-EXIT
           END-IF

           IF FUNCTION TEST-NUMVAL (WS-EDIT-DATE-MM) = 0
              COMPUTE WS-EDIT-DATE-MM-N
                          = FUNCTION NUMVAL (WS-EDIT-DATE-MM)
              END-COMPUTE
           ELSE
              SET INPUT-ERROR              TO TRUE
              SET  FLG-MONTH-NOT-OK        TO TRUE
              IF WS-RETURN-MSG-OFF
                 STRING
                   FUNCTION TRIM(WS-EDIT-VARIABLE-NAME)
                   ': Month must be a number between 1 and 12.'
                   DELIMITED BY SIZE
                   INTO WS-RETURN-MSG
              END-IF
              GO TO EDIT-MONTH-EXIT
           END-IF

           SET FLG-MONTH-ISVALID           TO TRUE
           .
       EDIT-MONTH-EXIT.
           EXIT
           .


       EDIT-DAY.

           SET FLG-DAY-ISVALID             TO TRUE

           IF WS-EDIT-DATE-DD              EQUAL LOW-VALUES
           OR WS-EDIT-DATE-DD              EQUAL SPACES
              SET INPUT-ERROR              TO TRUE
              SET  FLG-DAY-BLANK           TO TRUE
              IF WS-RETURN-MSG-OFF
                 STRING
                   FUNCTION TRIM(WS-EDIT-VARIABLE-NAME)
                   ' : Day must be supplied.'
                   DELIMITED BY SIZE
                   INTO WS-RETURN-MSG
              END-IF
              GO TO EDIT-DAY-EXIT
           ELSE
              CONTINUE
           END-IF

           IF FUNCTION TEST-NUMVAL (WS-EDIT-DATE-DD) = 0
              COMPUTE WS-EDIT-DATE-DD-N
                          = FUNCTION NUMVAL (WS-EDIT-DATE-DD)
              END-COMPUTE
           ELSE
              SET INPUT-ERROR              TO TRUE
              SET  FLG-DAY-NOT-OK          TO TRUE
              IF WS-RETURN-MSG-OFF
                 STRING
                   FUNCTION TRIM(WS-EDIT-VARIABLE-NAME)
                   ':day must be a number between 1 and 31.'
                   DELIMITED BY SIZE
                   INTO WS-RETURN-MSG
              END-IF
              GO TO EDIT-DAY-EXIT
           END-IF

           IF WS-VALID-DAY
              CONTINUE
           ELSE
              SET INPUT-ERROR              TO TRUE
              SET FLG-DAY-NOT-OK          TO TRUE
              IF WS-RETURN-MSG-OFF
                 STRING
                   FUNCTION TRIM(WS-EDIT-VARIABLE-NAME)
                   ':day must be a number between 1 and 31.'
                   DELIMITED BY SIZE
                   INTO WS-RETURN-MSG
              END-IF
              GO TO EDIT-DAY-EXIT
           END-IF
