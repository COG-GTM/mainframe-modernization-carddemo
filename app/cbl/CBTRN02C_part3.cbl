                                                                                
           PERFORM UNTIL END-OF-FILE = 'Y'                                      
               IF  END-OF-FILE = 'N'                                            
                   PERFORM 1000-DALYTRAN-GET-NEXT                               
                   IF  END-OF-FILE = 'N'                                        
                     ADD 1 TO WS-TRANSACTION-COUNT                              
      *              DISPLAY DALYTRAN-RECORD                                    
                     MOVE 0 TO WS-VALIDATION-FAIL-REASON                        
                     MOVE SPACES TO WS-VALIDATION-FAIL-REASON-DESC              
                     PERFORM 1500-VALIDATE-TRAN                                 
                     IF WS-VALIDATION-FAIL-REASON = 0                           
                       PERFORM 2000-POST-TRANSACTION                            
                     ELSE                                                       
                       ADD 1 TO WS-REJECT-COUNT                                 
                       PERFORM 2500-WRITE-REJECT-REC                            
                     END-IF                                                     
                   END-IF                                                       
               END-IF                                                           
           END-PERFORM.                                                         
                                                                                
           PERFORM 9000-DALYTRAN-CLOSE.                                         
           PERFORM 9100-TRANFILE-CLOSE.                                         
           PERFORM 9200-XREFFILE-CLOSE.                                         
           PERFORM 9300-DALYREJS-CLOSE.                                         
           PERFORM 9400-ACCTFILE-CLOSE.                                         
           PERFORM 9500-TCATBALF-CLOSE.                                         
           DISPLAY 'TRANSACTIONS PROCESSED :' WS-TRANSACTION-COUNT              
           DISPLAY 'TRANSACTIONS REJECTED  :' WS-REJECT-COUNT                   
           IF WS-REJECT-COUNT > 0                                               
              MOVE 4 TO RETURN-CODE                                             
           END-IF                                                               
           DISPLAY 'END OF EXECUTION OF PROGRAM CBTRN02C'.                      
                                                                                
           GOBACK.                                                              
      *---------------------------------------------------------------*         
       0000-DALYTRAN-OPEN.                                                      
           MOVE 8 TO APPL-RESULT.                                               
           OPEN INPUT DALYTRAN-FILE                                             
           IF  DALYTRAN-STATUS = '00'                                           
               MOVE 0 TO APPL-RESULT                                            
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR OPENING DALYTRAN'                                 
               MOVE DALYTRAN-STATUS TO IO-STATUS                                
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF                                                               
           EXIT.                                                                
      *---------------------------------------------------------------*         
       0100-TRANFILE-OPEN.                                                      
           MOVE 8 TO APPL-RESULT.                                               
           OPEN OUTPUT TRANSACT-FILE                                            
           IF  TRANFILE-STATUS = '00'                                           
               MOVE 0 TO APPL-RESULT                                            
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR OPENING TRANSACTION FILE'                         
               MOVE TRANFILE-STATUS TO IO-STATUS                                
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF                                                               
           EXIT.                                                                
                                                                                
      *---------------------------------------------------------------*         
       0200-XREFFILE-OPEN.                                                      
           MOVE 8 TO APPL-RESULT.                                               
           OPEN INPUT XREF-FILE                                                 
           IF  XREFFILE-STATUS = '00'                                           
               MOVE 0 TO APPL-RESULT                                            
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR OPENING CROSS REF FILE'                           
               MOVE XREFFILE-STATUS TO IO-STATUS                                
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF                                                               
           EXIT.                                                                
      *---------------------------------------------------------------*         
       0300-DALYREJS-OPEN.                                                      
           MOVE 8 TO APPL-RESULT.                                               
           OPEN OUTPUT DALYREJS-FILE                                            
           IF  DALYREJS-STATUS = '00'                                           
               MOVE 0 TO APPL-RESULT                                            
           ELSE                                                                 
               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
