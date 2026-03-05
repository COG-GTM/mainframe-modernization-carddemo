                  END-IF                                                        
               WHEN OTHER                                                       
                  SET INPUT-ERROR                    TO TRUE                    
                  IF  WS-RETURN-MSG-OFF                                         
                      SET FLG-ACCTFILTER-NOT-OK      TO TRUE                    
                  END-IF                                                        
                  MOVE 'READ'                        TO ERROR-OPNAME            
                  MOVE LIT-CARDFILENAME              TO ERROR-FILE              
                  MOVE WS-RESP-CD                    TO ERROR-RESP              
                  MOVE WS-REAS-CD                    TO ERROR-RESP2             
                  MOVE WS-FILE-ERROR-MESSAGE         TO WS-RETURN-MSG           
           END-EVALUATE                                                         
           .                                                                    
                                                                                
       9100-GETCARD-BYACCTCARD-EXIT.                                            
           EXIT                                                                 
           .                                                                    
                                                                                
                                                                                
       9200-WRITE-PROCESSING.                                                   
                                                                                
      *    Read the Card file                                                   
      *                                                                         
      *    MOVE CC-ACCT-ID-N            TO WS-CARD-RID-ACCT-ID                  
           MOVE CC-CARD-NUM             TO WS-CARD-RID-CARDNUM                  
                                                                                
           EXEC CICS READ                                                       
                FILE      (LIT-CARDFILENAME)                                    
                UPDATE                                                          
                RIDFLD    (WS-CARD-RID-CARDNUM)                                 
                KEYLENGTH (LENGTH OF WS-CARD-RID-CARDNUM)                       
                INTO      (CARD-RECORD)                                         
                LENGTH    (LENGTH OF CARD-RECORD)                               
                RESP      (WS-RESP-CD)                                          
                RESP2     (WS-REAS-CD)                                          
           END-EXEC                                                             
                                                                                
      *****************************************************************         
      *    Could we lock the record ?                                           
      *****************************************************************         
           IF WS-RESP-CD EQUAL TO DFHRESP(NORMAL)                               
              CONTINUE                                                          
           ELSE                                                                 
              SET INPUT-ERROR                    TO TRUE                        
              IF  WS-RETURN-MSG-OFF                                             
                  SET COULD-NOT-LOCK-FOR-UPDATE  TO TRUE                        
              END-IF                                                            
              GO TO 9200-WRITE-PROCESSING-EXIT                                  
           END-IF                                                               
      *****************************************************************         
      *    Did someone change the record while we were out ?                    
      *****************************************************************         
           PERFORM 9300-CHECK-CHANGE-IN-REC                                     
              THRU 9300-CHECK-CHANGE-IN-REC-EXIT                                
           IF DATA-WAS-CHANGED-BEFORE-UPDATE                                    
              GO TO 9200-WRITE-PROCESSING-EXIT                                  
           END-IF                                                               
      *****************************************************************         
      * Prepare the update                                                      
      *****************************************************************         
           INITIALIZE CARD-UPDATE-RECORD                                        
           MOVE CCUP-NEW-CARDID             TO CARD-UPDATE-NUM                  
           MOVE CC-ACCT-ID-N                TO CARD-UPDATE-ACCT-ID              
           MOVE CCUP-NEW-CVV-CD             TO CARD-CVV-CD-X                    
           MOVE CARD-CVV-CD-N               TO CARD-UPDATE-CVV-CD               
           MOVE CCUP-NEW-CRDNAME            TO CARD-UPDATE-EMBOSSED-NAME        
           STRING  CCUP-NEW-EXPYEAR                                             
                   '-'                                                          
                   CCUP-NEW-EXPMON                                              
                   '-'                                                          
                   CCUP-NEW-EXPDAY                                              
                   DELIMITED BY SIZE                                            
              INTO CARD-UPDATE-EXPIRAION-DATE                                   
           END-STRING                                                           
           MOVE CCUP-NEW-CRDSTCD            TO CARD-UPDATE-ACTIVE-STATUS        
                                                                                
           EXEC CICS                                                            
                REWRITE FILE(LIT-CARDFILENAME)                                  
                        FROM(CARD-UPDATE-RECORD)                                
                        LENGTH(LENGTH OF CARD-UPDATE-RECORD)                    
                        RESP      (WS-RESP-CD)                                  
                        RESP2     (WS-REAS-CD)                                  
           END-EXEC.                                                            
                                                                                
      *****************************************************************         
      * Did the update succeed ?  *                                             
      *****************************************************************         
           IF WS-RESP-CD EQUAL TO DFHRESP(NORMAL)                               
             CONTINUE                                                           
           ELSE                                                                 
             SET LOCKED-BUT-UPDATE-FAILED    TO TRUE                            
           END-IF                                                               
           .                                                                    
       9200-WRITE-PROCESSING-EXIT.                                              
           EXIT                                                                 
           .                                                                    
                                                                                
       9300-CHECK-CHANGE-IN-REC.                                                
           INSPECT CARD-EMBOSSED-NAME                                           
           CONVERTING LIT-LOWER                                                 
