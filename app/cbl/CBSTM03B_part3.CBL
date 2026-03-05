           MOVE CUSTFILE-STATUS TO LK-M03B-RC.

       3999-EXIT.
           EXIT.

       4000-ACCTFILE-PROC.

           IF M03B-OPEN
               OPEN INPUT ACCT-FILE
               GO TO 4900-EXIT
           END-IF.

           IF M03B-READ-K
               MOVE LK-M03B-KEY (1:LK-M03B-KEY-LN) TO FD-ACCT-ID
               READ ACCT-FILE INTO LK-M03B-FLDT
               END-READ
               GO TO 4900-EXIT
           END-IF.

           IF M03B-CLOSE
               CLOSE ACCT-FILE
               GO TO 4900-EXIT
           END-IF.

       4900-EXIT.
           MOVE ACCTFILE-STATUS TO LK-M03B-RC.

       4999-EXIT.
           EXIT.

