                 WHEN SELECT-OK(I)                                              
                   MOVE I TO I-SELECTED                                         
                   IF WS-MORE-THAN-1-ACTION                                     
                      MOVE '1' TO WS-ROW-CRDSELECT-ERROR(I)                     
                   END-IF                                                       
                 WHEN SELECT-BLANK(I)                                           
                   CONTINUE                                                     
                 WHEN OTHER                                                     
                   SET INPUT-ERROR TO TRUE                                      
                   MOVE '1' TO WS-ROW-CRDSELECT-ERROR(I)                        
                   IF WS-ERROR-MSG-OFF                                          
                      SET WS-INVALID-ACTION-CODE TO TRUE                        
                   END-IF                                                       
              END-EVALUATE                                                      
           END-PERFORM                                                          
                                                                                
           .                                                                    
                                                                                
       2250-EDIT-ARRAY-EXIT.                                                    
           EXIT                                                                 
           .                                                                    
                                                                                
       9000-READ-FORWARD.                                                       
           MOVE LOW-VALUES           TO WS-ALL-ROWS                             
                                                                                
      *****************************************************************         
      *    Start Browse                                                         
      *****************************************************************         
           EXEC CICS STARTBR                                                    
                DATASET(LIT-CARD-FILE)                                          
                RIDFLD(WS-CARD-RID-CARDNUM)                                     
                KEYLENGTH(LENGTH OF WS-CARD-RID-CARDNUM)                        
                GTEQ                                                            
                RESP(WS-RESP-CD)                                                
                RESP2(WS-REAS-CD)                                               
           END-EXEC                                                             
      *****************************************************************         
      *    Loop through records and fetch max screen records                    
      *****************************************************************         
           MOVE ZEROES TO WS-SCRN-COUNTER                                       
           SET CA-NEXT-PAGE-EXISTS    TO TRUE                                   
           SET MORE-RECORDS-TO-READ   TO TRUE                                   
                                                                                
           PERFORM UNTIL READ-LOOP-EXIT                                         
                                                                                
           EXEC CICS READNEXT                                                   
                DATASET(LIT-CARD-FILE)                                          
                INTO (CARD-RECORD)                                              
                LENGTH(LENGTH OF CARD-RECORD)                                   
                RIDFLD(WS-CARD-RID-CARDNUM)                                     
                KEYLENGTH(LENGTH OF WS-CARD-RID-CARDNUM)                        
                RESP(WS-RESP-CD)                                                
                RESP2(WS-REAS-CD)                                               
           END-EXEC                                                             
                                                                                
           EVALUATE WS-RESP-CD                                                  
               WHEN DFHRESP(NORMAL)                                             
               WHEN DFHRESP(DUPREC)                                             
                   PERFORM 9500-FILTER-RECORDS                                  
                      THRU 9500-FILTER-RECORDS-EXIT                             
                                                                                
                   IF WS-DONOT-EXCLUDE-THIS-RECORD                              
                      ADD 1             TO WS-SCRN-COUNTER                      
                                                                                
                      MOVE CARD-NUM     TO WS-ROW-CARD-NUM(                     
                      WS-SCRN-COUNTER)                                          
                      MOVE CARD-ACCT-ID TO                                      
                      WS-ROW-ACCTNO(WS-SCRN-COUNTER)                            
                      MOVE CARD-ACTIVE-STATUS                                   
                                        TO WS-ROW-CARD-STATUS(                  
                                        WS-SCRN-COUNTER)                        
                                                                                
                      IF WS-SCRN-COUNTER = 1                                    
                         MOVE CARD-ACCT-ID                                      
                                        TO WS-CA-FIRST-CARD-ACCT-ID             
                         MOVE CARD-NUM  TO WS-CA-FIRST-CARD-NUM                 
                         IF   WS-CA-SCREEN-NUM = 0                              
                           ADD   +1     TO WS-CA-SCREEN-NUM                     
                         ELSE                                                   
                           CONTINUE                                             
                         END-IF                                                 
                      ELSE                                                      
                         CONTINUE                                               
                      END-IF                                                    
                   ELSE                                                         
                       CONTINUE                                                 
                   END-IF                                                       
      ******************************************************************        
      *            Max Screen size                                              
      ******************************************************************        
                   IF WS-SCRN-COUNTER = WS-MAX-SCREEN-LINES                     
                      SET READ-LOOP-EXIT  TO TRUE                               
                                                                                
                      MOVE CARD-ACCT-ID     TO WS-CA-LAST-CARD-ACCT-ID          
                      MOVE CARD-NUM         TO WS-CA-LAST-CARD-NUM              
                                                                                
                      EXEC CICS READNEXT                                        
                        DATASET(LIT-CARD-FILE)                                  
                        INTO (CARD-RECORD)                                      
                        LENGTH(LENGTH OF CARD-RECORD)                           
