           .                                                                    
                                                                                
       2210-EDIT-ACCOUNT.                                                       
           SET FLG-ACCTFILTER-BLANK TO TRUE                                     
                                                                                
      *    Not supplied                                                         
           IF CC-ACCT-ID   EQUAL LOW-VALUES                                     
           OR CC-ACCT-ID   EQUAL SPACES                                         
           OR CC-ACCT-ID-N EQUAL ZEROS                                          
              SET FLG-ACCTFILTER-BLANK  TO TRUE                                 
              MOVE ZEROES       TO CDEMO-ACCT-ID                                
              GO TO  2210-EDIT-ACCOUNT-EXIT                                     
           END-IF                                                               
      *                                                                         
      *    Not numeric                                                          
      *    Not 11 characters                                                    
           IF CC-ACCT-ID  IS NOT NUMERIC                                        
              SET INPUT-ERROR TO TRUE                                           
              SET FLG-ACCTFILTER-NOT-OK TO TRUE                                 
              SET FLG-PROTECT-SELECT-ROWS-YES TO TRUE                           
              MOVE                                                              
              'ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER'            
                              TO WS-ERROR-MSG                                   
              MOVE ZERO       TO CDEMO-ACCT-ID                                  
              GO TO 2210-EDIT-ACCOUNT-EXIT                                      
           ELSE                                                                 
              MOVE CC-ACCT-ID TO CDEMO-ACCT-ID                                  
              SET FLG-ACCTFILTER-ISVALID TO TRUE                                
           END-IF                                                               
           .                                                                    
                                                                                
       2210-EDIT-ACCOUNT-EXIT.                                                  
           EXIT                                                                 
           .                                                                    
                                                                                
       2220-EDIT-CARD.                                                          
      *    Not numeric                                                          
      *    Not 16 characters                                                    
           SET FLG-CARDFILTER-BLANK TO TRUE                                     
                                                                                
      *    Not supplied                                                         
           IF CC-CARD-NUM   EQUAL LOW-VALUES                                    
           OR CC-CARD-NUM   EQUAL SPACES                                        
           OR CC-CARD-NUM-N EQUAL ZEROS                                         
              SET FLG-CARDFILTER-BLANK  TO TRUE                                 
              MOVE ZEROES       TO CDEMO-CARD-NUM                               
              GO TO  2220-EDIT-CARD-EXIT                                        
           END-IF                                                               
      *                                                                         
      *    Not numeric                                                          
      *    Not 16 characters                                                    
           IF CC-CARD-NUM  IS NOT NUMERIC                                       
              SET INPUT-ERROR TO TRUE                                           
              SET FLG-CARDFILTER-NOT-OK TO TRUE                                 
              SET FLG-PROTECT-SELECT-ROWS-YES TO TRUE                           
              IF WS-ERROR-MSG-OFF                                               
                 MOVE                                                           
              'CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER'            
                              TO WS-ERROR-MSG                                   
              END-IF                                                            
              MOVE ZERO       TO CDEMO-CARD-NUM                                 
              GO TO 2220-EDIT-CARD-EXIT                                         
           ELSE                                                                 
              MOVE CC-CARD-NUM-N TO CDEMO-CARD-NUM                              
              SET FLG-CARDFILTER-ISVALID TO TRUE                                
           END-IF                                                               
           .                                                                    
                                                                                
       2220-EDIT-CARD-EXIT.                                                     
           EXIT                                                                 
           .                                                                    
                                                                                
       2250-EDIT-ARRAY.                                                         
                                                                                
           IF INPUT-ERROR                                                       
              GO TO 2250-EDIT-ARRAY-EXIT                                        
           END-IF                                                               
                                                                                
           INSPECT  WS-EDIT-SELECT-FLAGS                                        
           TALLYING I                                                           
           FOR ALL 'S'                                                          
               ALL 'U'                                                          
                                                                                
           IF I > +1                                                            
               SET INPUT-ERROR      TO TRUE                                     
               SET WS-MORE-THAN-1-ACTION TO TRUE                                
                                                                                
               MOVE WS-EDIT-SELECT-FLAGS                                        
                                   TO WS-EDIT-SELECT-ERROR-FLAGS                
               INSPECT WS-EDIT-SELECT-ERROR-FLAGS                               
                 REPLACING ALL 'S' BY '1'                                       
                           ALL 'U' BY '1'                                       
                 CHARACTERS        BY '0'                                       
                                                                                
           END-IF                                                               
                                                                                
           MOVE ZERO TO I-SELECTED                                              
                                                                                
           PERFORM VARYING I FROM 1 BY 1 UNTIL I > 7                            
               EVALUATE TRUE                                                    
