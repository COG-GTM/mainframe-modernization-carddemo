           EXIT.                                                                
      *---------------------------------------------------------------*         
       2700-A-CREATE-TCATBAL-REC.                                               
           INITIALIZE TRAN-CAT-BAL-RECORD                                       
           MOVE XREF-ACCT-ID TO TRANCAT-ACCT-ID                                 
           MOVE DALYTRAN-TYPE-CD TO TRANCAT-TYPE-CD                             
           MOVE DALYTRAN-CAT-CD TO TRANCAT-CD                                   
           ADD DALYTRAN-AMT TO TRAN-CAT-BAL                                     
                                                                                
           WRITE FD-TRAN-CAT-BAL-RECORD FROM TRAN-CAT-BAL-RECORD                
                                                                                
           IF  TCATBALF-STATUS = '00'                                           
               MOVE 0 TO APPL-RESULT                                            
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR WRITING TRANSACTION BALANCE FILE'                 
               MOVE TCATBALF-STATUS TO IO-STATUS                                
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF.                                                              
      *---------------------------------------------------------------*         
       2700-B-UPDATE-TCATBAL-REC.                                               
           ADD DALYTRAN-AMT TO TRAN-CAT-BAL                                     
           REWRITE FD-TRAN-CAT-BAL-RECORD FROM TRAN-CAT-BAL-RECORD              
                                                                                
           IF  TCATBALF-STATUS = '00'                                           
               MOVE 0 TO APPL-RESULT                                            
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR REWRITING TRANSACTION BALANCE FILE'               
               MOVE TCATBALF-STATUS TO IO-STATUS                                
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF.                                                              
                                                                                
      *---------------------------------------------------------------*         
       2800-UPDATE-ACCOUNT-REC.                                                 
      * Update the balances in account record to reflect posted trans.          
           ADD DALYTRAN-AMT  TO ACCT-CURR-BAL                                   
           IF DALYTRAN-AMT >= 0                                                 
              ADD DALYTRAN-AMT TO ACCT-CURR-CYC-CREDIT                          
           ELSE                                                                 
              ADD DALYTRAN-AMT TO ACCT-CURR-CYC-DEBIT                           
           END-IF                                                               
                                                                                
           REWRITE FD-ACCTFILE-REC FROM  ACCOUNT-RECORD                         
              INVALID KEY                                                       
                MOVE 109 TO WS-VALIDATION-FAIL-REASON                           
                MOVE 'ACCOUNT RECORD NOT FOUND'                                 
                  TO WS-VALIDATION-FAIL-REASON-DESC                             
           END-REWRITE.                                                         
           EXIT.                                                                
      *---------------------------------------------------------------*         
       2900-WRITE-TRANSACTION-FILE.                                             
           MOVE 8 TO  APPL-RESULT.                                              
           WRITE FD-TRANFILE-REC FROM TRAN-RECORD                               
                                                                                
           IF  TRANFILE-STATUS = '00'                                           
               MOVE 0 TO  APPL-RESULT                                           
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR WRITING TO TRANSACTION FILE'                      
               MOVE TRANFILE-STATUS  TO IO-STATUS                               
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF                                                               
           EXIT.                                                                
                                                                                
      *---------------------------------------------------------------*         
       9000-DALYTRAN-CLOSE.                                                     
           MOVE 8 TO  APPL-RESULT.                                              
           CLOSE DALYTRAN-FILE                                                  
           IF  DALYTRAN-STATUS = '00'                                           
               MOVE 0 TO APPL-RESULT                                            
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR CLOSING DALYTRAN FILE'                            
               MOVE DALYTRAN-STATUS TO IO-STATUS                                
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF                                                               
           EXIT.                                                                
      *---------------------------------------------------------------*         
       9100-TRANFILE-CLOSE.                                                     
