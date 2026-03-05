      ******************************************************************
      *Procedure Division Copybook for DATE related code
      ******************************************************************
      *Date validation paragraph for reuse and hopefully not misuse
      *Accompanying WORKING Storage is CSUTLDTR
      ******************************************************************
      * ***  PERFORM EDIT-DATE-CCYYMMDD
      *         THRU EDIT-DATE-CCYYMMDD-EXIT
      *         to validate CCYYMMDD dates
      *      Reusable paras
      *      a) EDIT-YEAR-CCYY
      *      b) EDIT-MONTH
      *      c) EDIT-DAY
      *      d) EDIT-DATE-OF-BIRTH
      *      e) EDIT-DATE-OF-BIRTH
      ******************************************************************

       EDIT-DATE-CCYYMMDD.
           SET WS-EDIT-DATE-IS-INVALID   TO TRUE
           .

      ******************************************************************
      *Check for valid year and century
      ******************************************************************
       EDIT-YEAR-CCYY.

           SET FLG-YEAR-NOT-OK             TO TRUE

      *    Not supplied
           IF WS-EDIT-DATE-CCYY            EQUAL LOW-VALUES
           OR WS-EDIT-DATE-CCYY            EQUAL SPACES
              SET INPUT-ERROR              TO TRUE
              SET  FLG-YEAR-BLANK          TO TRUE
              IF WS-RETURN-MSG-OFF
                 STRING
                   FUNCTION TRIM(WS-EDIT-VARIABLE-NAME)
                   ' : Year must be supplied.'
                   DELIMITED BY SIZE
                   INTO WS-RETURN-MSG
              END-IF
      *       Intentional violation of structured programming norms
              GO TO EDIT-YEAR-CCYY-EXIT
           ELSE
              CONTINUE
           END-IF

      *    Not numeric
           IF WS-EDIT-DATE-CCYY            IS NOT NUMERIC
              SET INPUT-ERROR              TO TRUE
              SET  FLG-YEAR-NOT-OK         TO TRUE
              IF WS-RETURN-MSG-OFF
                 STRING
                   FUNCTION TRIM(WS-EDIT-VARIABLE-NAME)
                   ' must be 4 digit number.'
                   DELIMITED BY SIZE
                   INTO WS-RETURN-MSG
              END-IF
              GO TO EDIT-YEAR-CCYY-EXIT
           ELSE
              CONTINUE
           END-IF

      ******************************************************************
      *    Century not reasonable
      ******************************************************************
      *  Not having learnt our lesson from history and Y2K
      *  And being unable to imagine COBOL in the 2100s
      *  We code only 19 and 20 as valid century values
      ******************************************************************
           IF THIS-CENTURY
           OR LAST-CENTURY
              CONTINUE
           ELSE
              SET INPUT-ERROR              TO TRUE
              SET  FLG-YEAR-NOT-OK         TO TRUE
              IF WS-RETURN-MSG-OFF
                 STRING
                   FUNCTION TRIM(WS-EDIT-VARIABLE-NAME)
                   ' : Century is not valid.'
                   DELIMITED BY SIZE
                   INTO WS-RETURN-MSG
              END-IF
              GO TO EDIT-YEAR-CCYY-EXIT
           END-IF

           SET FLG-YEAR-ISVALID            TO TRUE
           .
       EDIT-YEAR-CCYY-EXIT.
           EXIT
           .
       EDIT-MONTH.
           SET FLG-MONTH-NOT-OK            TO TRUE

           IF WS-EDIT-DATE-MM              EQUAL LOW-VALUES
           OR WS-EDIT-DATE-MM              EQUAL SPACES
              SET INPUT-ERROR              TO TRUE
              SET  FLG-MONTH-BLANK         TO TRUE
              IF WS-RETURN-MSG-OFF
                 STRING
                   FUNCTION TRIM(WS-EDIT-VARIABLE-NAME)
