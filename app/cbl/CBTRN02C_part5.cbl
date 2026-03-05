      *         DISPLAY 'ACCT-CREDIT-LIMIT:' ACCT-CREDIT-LIMIT                  
      *         DISPLAY 'TRAN-AMT         :' DALYTRAN-AMT                       
                COMPUTE WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT                      
                                    - ACCT-CURR-CYC-DEBIT                       
                                    + DALYTRAN-AMT                              
                                                                                
                IF ACCT-CREDIT-LIMIT >= WS-TEMP-BAL                             
                  CONTINUE                                                      
                ELSE                                                            
                  MOVE 102 TO WS-VALIDATION-FAIL-REASON                         
                  MOVE 'OVERLIMIT TRANSACTION'                                  
                    TO WS-VALIDATION-FAIL-REASON-DESC                           
                END-IF                                                          
                IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS (1:10)               
                  CONTINUE                                                      
                ELSE                                                            
                  MOVE 103 TO WS-VALIDATION-FAIL-REASON                         
                  MOVE 'TRANSACTION RECEIVED AFTER ACCT EXPIRATION'             
                    TO WS-VALIDATION-FAIL-REASON-DESC                           
                END-IF                                                          
           END-READ                                                             
           EXIT.                                                                
      *---------------------------------------------------------------*         
       2000-POST-TRANSACTION.                                                   
           MOVE  DALYTRAN-ID            TO    TRAN-ID                           
           MOVE  DALYTRAN-TYPE-CD       TO    TRAN-TYPE-CD                      
           MOVE  DALYTRAN-CAT-CD        TO    TRAN-CAT-CD                       
           MOVE  DALYTRAN-SOURCE        TO    TRAN-SOURCE                       
           MOVE  DALYTRAN-DESC          TO    TRAN-DESC                         
           MOVE  DALYTRAN-AMT           TO    TRAN-AMT                          
           MOVE  DALYTRAN-MERCHANT-ID   TO    TRAN-MERCHANT-ID                  
           MOVE  DALYTRAN-MERCHANT-NAME TO    TRAN-MERCHANT-NAME                
           MOVE  DALYTRAN-MERCHANT-CITY TO    TRAN-MERCHANT-CITY                
           MOVE  DALYTRAN-MERCHANT-ZIP  TO    TRAN-MERCHANT-ZIP                 
           MOVE  DALYTRAN-CARD-NUM      TO    TRAN-CARD-NUM                     
           MOVE  DALYTRAN-ORIG-TS       TO    TRAN-ORIG-TS                      
           PERFORM Z-GET-DB2-FORMAT-TIMESTAMP                                   
           MOVE  DB2-FORMAT-TS          TO    TRAN-PROC-TS                      
                                                                                
           PERFORM 2700-UPDATE-TCATBAL                                          
           PERFORM 2800-UPDATE-ACCOUNT-REC                                      
           PERFORM 2900-WRITE-TRANSACTION-FILE                                  
                                                                                
           EXIT.                                                                
                                                                                
       2500-WRITE-REJECT-REC.                                                   
           MOVE DALYTRAN-RECORD TO REJECT-TRAN-DATA                             
           MOVE WS-VALIDATION-TRAILER TO VALIDATION-TRAILER                     
      *     DISPLAY '***' REJECT-RECORD                                         
           MOVE 8 TO APPL-RESULT                                                
           WRITE FD-REJS-RECORD FROM REJECT-RECORD                              
           IF DALYREJS-STATUS = '00'                                            
               MOVE 0 TO  APPL-RESULT                                           
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR WRITING TO REJECTS FILE'                          
               MOVE DALYREJS-STATUS  TO IO-STATUS                               
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF                                                               
           EXIT.                                                                
      *---------------------------------------------------------------*         
       2700-UPDATE-TCATBAL.                                                     
      * Update the balances in transaction balance file.                        
           MOVE XREF-ACCT-ID TO FD-TRANCAT-ACCT-ID                              
           MOVE DALYTRAN-TYPE-CD TO FD-TRANCAT-TYPE-CD                          
           MOVE DALYTRAN-CAT-CD TO FD-TRANCAT-CD                                
                                                                                
           MOVE 'N' TO WS-CREATE-TRANCAT-REC                                    
           READ TCATBAL-FILE INTO TRAN-CAT-BAL-RECORD                           
              INVALID KEY                                                       
                DISPLAY 'TCATBAL record not found for key : '                   
                   FD-TRAN-CAT-KEY '.. Creating.'                               
                MOVE 'Y' TO WS-CREATE-TRANCAT-REC                               
           END-READ.                                                            
                                                                                
           IF  TCATBALF-STATUS = '00'  OR '23'                                  
               MOVE 0 TO APPL-RESULT                                            
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR READING TRANSACTION BALANCE FILE'                 
               MOVE TCATBALF-STATUS TO IO-STATUS                                
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF.                                                              
                                                                                
           IF WS-CREATE-TRANCAT-REC = 'Y'                                       
              PERFORM 2700-A-CREATE-TCATBAL-REC                                 
           ELSE                                                                 
              PERFORM 2700-B-UPDATE-TCATBAL-REC                                 
           END-IF                                                               
                                                                                
