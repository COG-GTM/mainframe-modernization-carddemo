                                                                                
       1220-EDIT-CARD-EXIT.                                                     
           EXIT                                                                 
           .                                                                    
                                                                                
       1230-EDIT-NAME.                                                          
      *    Not BLANK                                                            
           SET FLG-CARDNAME-NOT-OK      TO TRUE                                 
                                                                                
      *    Not supplied                                                         
           IF CCUP-NEW-CRDNAME   EQUAL LOW-VALUES                               
           OR CCUP-NEW-CRDNAME   EQUAL SPACES                                   
           OR CCUP-NEW-CRDNAME   EQUAL ZEROS                                    
              SET INPUT-ERROR           TO TRUE                                 
              SET FLG-CARDNAME-BLANK  TO TRUE                                   
              IF WS-RETURN-MSG-OFF                                              
                 SET WS-PROMPT-FOR-NAME TO TRUE                                 
              END-IF                                                            
              GO TO  1230-EDIT-NAME-EXIT                                        
           END-IF                                                               
                                                                                
      *    Only Alphabets and space allowed                                     
           MOVE CCUP-NEW-CRDNAME        TO CARD-NAME-CHECK                      
           INSPECT CARD-NAME-CHECK                                              
             CONVERTING LIT-ALL-ALPHA-FROM                                      
                     TO LIT-ALL-SPACES-TO                                       
                                                                                
           IF FUNCTION LENGTH(FUNCTION TRIM(CARD-NAME-CHECK)) = 0               
              CONTINUE                                                          
           ELSE                                                                 
              SET INPUT-ERROR           TO TRUE                                 
              SET FLG-CARDNAME-NOT-OK   TO TRUE                                 
              IF WS-RETURN-MSG-OFF                                              
                 SET WS-NAME-MUST-BE-ALPHA  TO TRUE                             
              END-IF                                                            
              GO TO  1230-EDIT-NAME-EXIT                                        
           END-IF                                                               
                                                                                
           SET FLG-CARDNAME-ISVALID     TO TRUE                                 
           .                                                                    
       1230-EDIT-NAME-EXIT.                                                     
           EXIT                                                                 
           .                                                                    
                                                                                
       1240-EDIT-CARDSTATUS.                                                    
      *    Must be Y or N                                                       
           SET FLG-CARDSTATUS-NOT-OK      TO TRUE                               
                                                                                
      *    Not supplied                                                         
           IF CCUP-NEW-CRDSTCD   EQUAL LOW-VALUES                               
           OR CCUP-NEW-CRDSTCD   EQUAL SPACES                                   
           OR CCUP-NEW-CRDSTCD   EQUAL ZEROS                                    
              SET INPUT-ERROR           TO TRUE                                 
              SET FLG-CARDSTATUS-BLANK  TO TRUE                                 
              IF WS-RETURN-MSG-OFF                                              
                 SET CARD-STATUS-MUST-BE-YES-NO TO TRUE                         
              END-IF                                                            
              GO TO  1240-EDIT-CARDSTATUS-EXIT                                  
           END-IF                                                               
                                                                                
           MOVE CCUP-NEW-CRDSTCD          TO FLG-YES-NO-CHECK                   
                                                                                
           IF FLG-YES-NO-VALID                                                  
              SET FLG-CARDSTATUS-ISVALID  TO TRUE                               
           ELSE                                                                 
              SET INPUT-ERROR             TO TRUE                               
              SET FLG-CARDSTATUS-NOT-OK   TO TRUE                               
              IF WS-RETURN-MSG-OFF                                              
                 SET CARD-STATUS-MUST-BE-YES-NO  TO TRUE                        
              END-IF                                                            
              GO TO  1240-EDIT-CARDSTATUS-EXIT                                  
           END-IF                                                               
           .                                                                    
       1240-EDIT-CARDSTATUS-EXIT.                                               
           EXIT                                                                 
           .                                                                    
       1250-EDIT-EXPIRY-MON.                                                    
                                                                                
                                                                                
           SET FLG-CARDEXPMON-NOT-OK      TO TRUE                               
                                                                                
      *    Not supplied                                                         
           IF CCUP-NEW-EXPMON   EQUAL LOW-VALUES                                
           OR CCUP-NEW-EXPMON   EQUAL SPACES                                    
           OR CCUP-NEW-EXPMON   EQUAL ZEROS                                     
              SET INPUT-ERROR           TO TRUE                                 
              SET FLG-CARDEXPMON-BLANK  TO TRUE                                 
              IF WS-RETURN-MSG-OFF                                              
                 SET CARD-EXPIRY-MONTH-NOT-VALID TO TRUE                        
              END-IF                                                            
              GO TO  1250-EDIT-EXPIRY-MON-EXIT                                  
           END-IF                                                               
                                                                                
      *    Must be numeric                                                      
      *    Must be 1 to 12                                                      
           MOVE CCUP-NEW-EXPMON           TO CARD-MONTH-CHECK                   
                                                                                
           IF VALID-MONTH                                                       
              SET FLG-CARDEXPMON-ISVALID  TO TRUE                               
           ELSE                                                                 
