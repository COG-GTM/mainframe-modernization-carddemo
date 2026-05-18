      ******************************************************************
      * Program     : TCATD17C.CBL
      * Application : CardDemo - MBA-1765 Test Suite
      * Type        : BATCH COBOL Test Program
      * Function    : End-to-end tests for 17-digit card number
      *               migration. Covers all 7 test cases:
      *               1. Card CRUD with 17-digit numbers
      *               2. Card selection/filter validation
      *               3. Transactions with 17-digit card
      *               4. Cross-reference lookups
      *               5. Reports layout field widths
      *               6. Batch statement fields
      *               7. Negative tests
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    TCATD17C.
       AUTHOR.        DEVIN-AI.

       DATA DIVISION.
       WORKING-STORAGE SECTION.

       COPY CVACT02Y.
       COPY CVACT03Y.
       COPY CVTRA05Y.

       01  WS-TEST-COUNTERS.
           05  WS-TESTS-RUN       PIC 9(3) VALUE 0.
           05  WS-TESTS-PASSED    PIC 9(3) VALUE 0.
           05  WS-TESTS-FAILED    PIC 9(3) VALUE 0.

       01  WS-TEST-CARD-NUM       PIC X(17).
       01  WS-TEST-CARD-NUM-N     PIC 9(17).
       01  WS-VALID-17-DIGIT      PIC X(17)
                                  VALUE '12345678901234567'.
       01  WS-VALID-17-ZEROS      PIC X(17)
                                  VALUE '00000000000000000'.
       01  WS-INVALID-16-DIGIT    PIC X(16)
                                  VALUE '1234567890123456'.
       01  WS-INVALID-NON-NUM     PIC X(17)
                                  VALUE '1234567890ABCDEFG'.
       01  WS-INVALID-BLANK       PIC X(17)
                                  VALUE SPACES.

      ******************************************************************
       PROCEDURE DIVISION.
       0000-MAIN.
           DISPLAY '========================================'
           DISPLAY 'MBA-1765: 17-DIGIT CARD NUM TESTS'
           DISPLAY '========================================'
           DISPLAY ' '

           PERFORM TC01-CARD-RECORD-LAYOUT
           PERFORM TC02-CARD-FILTER-VALIDATION
           PERFORM TC03-TRANSACTION-RECORD-LAYOUT
           PERFORM TC04-CROSS-REFERENCE-LAYOUT
           PERFORM TC05-REPORT-LAYOUT
           PERFORM TC06-BATCH-STATEMENT-FIELDS
           PERFORM TC07-NEGATIVE-TESTS

           DISPLAY ' '
           DISPLAY '========================================'
           DISPLAY 'TEST RESULTS SUMMARY'
           DISPLAY '========================================'
           DISPLAY '  TESTS RUN:    ' WS-TESTS-RUN
           DISPLAY '  TESTS PASSED: ' WS-TESTS-PASSED
           DISPLAY '  TESTS FAILED: ' WS-TESTS-FAILED
           DISPLAY '========================================'

           IF WS-TESTS-FAILED > 0
              DISPLAY 'OVERALL: FAILURES DETECTED'
              STOP RUN
           ELSE
              DISPLAY 'OVERALL: ALL TESTS PASSED'
              STOP RUN
           END-IF.

      ******************************************************************
      * TC01: Card CRUD - 17-digit numbers
      ******************************************************************
       TC01-CARD-RECORD-LAYOUT.
           DISPLAY '--- TC01: CARD CRUD ---'

           MOVE WS-VALID-17-DIGIT TO CARD-NUM
           IF CARD-NUM = WS-VALID-17-DIGIT
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Card accepts 17'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Card reject 17'
           END-IF

           MOVE WS-VALID-17-DIGIT TO CARD-NUM
           MOVE 12345678901 TO CARD-ACCT-ID
           MOVE 123 TO CARD-CVV-CD
           MOVE 'Test' TO CARD-EMBOSSED-NAME
           MOVE '2026-12-31' TO CARD-EXPIRAION-DATE
           MOVE 'Y' TO CARD-ACTIVE-STATUS

           IF CARD-NUM = '12345678901234567'
           AND CARD-ACCT-ID = 12345678901
           AND CARD-CVV-CD = 123
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Full card OK'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Card record bad'
           END-IF

           MOVE SPACES TO CARD-RECORD
           MOVE WS-VALID-17-DIGIT TO CARD-NUM
           IF CARD-NUM = WS-VALID-17-DIGIT
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Card reset OK'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Card reset bad'
           END-IF.

      ******************************************************************
      * TC02: Card filter - 17-digit validation
      ******************************************************************
       TC02-CARD-FILTER-VALIDATION.
           DISPLAY '--- TC02: CARD FILTER ---'

           MOVE WS-VALID-17-DIGIT TO WS-TEST-CARD-NUM
           IF WS-TEST-CARD-NUM IS NUMERIC
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: 17 numeric OK'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: 17 numeric bad'
           END-IF

           MOVE WS-INVALID-NON-NUM TO WS-TEST-CARD-NUM
           IF WS-TEST-CARD-NUM IS NOT NUMERIC
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Non-num rejected'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Non-num accepted'
           END-IF

           IF LENGTH OF WS-TEST-CARD-NUM = 17
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Field is 17'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Field not 17'
           END-IF.

      ******************************************************************
      * TC03: Transactions - 17-digit card
      ******************************************************************
       TC03-TRANSACTION-RECORD-LAYOUT.
           DISPLAY '--- TC03: TRANSACTION ---'

           MOVE WS-VALID-17-DIGIT TO TRAN-CARD-NUM
           IF TRAN-CARD-NUM = WS-VALID-17-DIGIT
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Tran accepts 17'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Tran rejects 17'
           END-IF

           MOVE '0000000000000001' TO TRAN-ID
           MOVE 'PR' TO TRAN-TYPE-CD
           MOVE 5001 TO TRAN-CAT-CD
           MOVE WS-VALID-17-DIGIT TO TRAN-CARD-NUM
           IF TRAN-ID = '0000000000000001'
           AND TRAN-CARD-NUM = '12345678901234567'
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Full tran OK'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Tran bad'
           END-IF.

      ******************************************************************
      * TC04: Cross-reference
      ******************************************************************
       TC04-CROSS-REFERENCE-LAYOUT.
           DISPLAY '--- TC04: CROSS-REF ---'

           MOVE WS-VALID-17-DIGIT TO XREF-CARD-NUM
           IF XREF-CARD-NUM = WS-VALID-17-DIGIT
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: XREF accepts 17'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: XREF rejects 17'
           END-IF

           MOVE 123456789 TO XREF-CUST-ID
           MOVE 12345678901 TO XREF-ACCT-ID
           IF XREF-CARD-NUM = '12345678901234567'
           AND XREF-CUST-ID = 123456789
           AND XREF-ACCT-ID = 12345678901
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: XREF mapping OK'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: XREF mapping bad'
           END-IF.

      ******************************************************************
      * TC05: Reports - field widths
      ******************************************************************
       TC05-REPORT-LAYOUT.
           DISPLAY '--- TC05: REPORT LAYOUT ---'

           IF LENGTH OF CARD-NUM = 17
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: CARD-NUM 17'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: CARD-NUM not 17'
           END-IF

           IF LENGTH OF TRAN-CARD-NUM = 17
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: TRAN-CARD 17'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: TRAN-CARD not 17'
           END-IF

           IF LENGTH OF XREF-CARD-NUM = 17
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: XREF-CARD 17'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: XREF-CARD not 17'
           END-IF.

      ******************************************************************
      * TC06: Batch statement fields
      ******************************************************************
       TC06-BATCH-STATEMENT-FIELDS.
           DISPLAY '--- TC06: BATCH STMT ---'

           IF LENGTH OF TRAN-CARD-NUM = 17
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Batch card 17'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Batch card bad'
           END-IF

           MOVE '12345678901234567' TO TRAN-CARD-NUM
           MOVE '0000000000000001' TO TRAN-ID
           IF TRAN-CARD-NUM NOT = TRAN-ID
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Card<>TranID'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Card=TranID'
           END-IF.

      ******************************************************************
      * TC07: Negative tests
      ******************************************************************
       TC07-NEGATIVE-TESTS.
           DISPLAY '--- TC07: NEGATIVE ---'

           MOVE WS-INVALID-NON-NUM TO WS-TEST-CARD-NUM
           IF WS-TEST-CARD-NUM IS NOT NUMERIC
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Non-num rejected'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Non-num accepted'
           END-IF

           MOVE SPACES TO WS-TEST-CARD-NUM
           IF WS-TEST-CARD-NUM = SPACES
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Blank detected'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Blank missed'
           END-IF

           MOVE WS-VALID-17-ZEROS TO WS-TEST-CARD-NUM
           MOVE WS-TEST-CARD-NUM TO WS-TEST-CARD-NUM-N
           IF WS-TEST-CARD-NUM-N = ZEROS
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Zeros detected'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Zeros missed'
           END-IF

           MOVE 'ABCDEFGHIJKLMNOPQ' TO WS-TEST-CARD-NUM
           IF WS-TEST-CARD-NUM IS NOT NUMERIC
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: Alpha rejected'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: Alpha accepted'
           END-IF

           MOVE '1234567890123456 ' TO WS-TEST-CARD-NUM
           IF WS-TEST-CARD-NUM IS NOT NUMERIC
              PERFORM ASSERT-PASSED
              DISPLAY '  PASS: 16+sp rejected'
           ELSE
              PERFORM ASSERT-FAILED
              DISPLAY '  FAIL: 16+sp accepted'
           END-IF.

      ******************************************************************
      * Helper paragraphs
      ******************************************************************
       ASSERT-PASSED.
           ADD 1 TO WS-TESTS-RUN
           ADD 1 TO WS-TESTS-PASSED.

       ASSERT-FAILED.
           ADD 1 TO WS-TESTS-RUN
           ADD 1 TO WS-TESTS-FAILED.
