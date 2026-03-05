                 END-EVALUATE                                                   
      ******************************************************************        
      *       DETAILS EDITED , FOUND OK, CONFIRM SAVE REQUESTED                 
      *       CONFIRMATION NOT GIVEN. SO SHOW DETAILS AGAIN                     
      ******************************************************************        
              WHEN CCUP-CHANGES-OK-NOT-CONFIRMED                                
                  CONTINUE                                                      
      ******************************************************************        
      *       SHOW CONFIRMATION. GO BACK TO SQUARE 1                            
      ******************************************************************        
              WHEN CCUP-CHANGES-OKAYED-AND-DONE                                 
                  SET CCUP-SHOW-DETAILS TO TRUE                                 
                  IF CDEMO-FROM-TRANID    EQUAL LOW-VALUES                      
                  OR CDEMO-FROM-TRANID    EQUAL SPACES                          
                     MOVE ZEROES       TO CDEMO-ACCT-ID                         
                                          CDEMO-CARD-NUM                        
                     MOVE LOW-VALUES   TO CDEMO-ACCT-STATUS                     
                  END-IF                                                        
              WHEN OTHER                                                        
                   MOVE LIT-THISPGM    TO ABEND-CULPRIT                         
                   MOVE '0001'         TO ABEND-CODE                            
                   MOVE SPACES         TO ABEND-REASON                          
                   MOVE 'UNEXPECTED DATA SCENARIO'                              
                                       TO ABEND-MSG                             
                   PERFORM ABEND-ROUTINE                                        
                      THRU ABEND-ROUTINE-EXIT                                   
           END-EVALUATE                                                         
           .                                                                    
       2000-DECIDE-ACTION-EXIT.                                                 
           EXIT                                                                 
           .                                                                    
                                                                                
                                                                                
                                                                                
       3000-SEND-MAP.                                                           
           PERFORM 3100-SCREEN-INIT                                             
              THRU 3100-SCREEN-INIT-EXIT                                        
           PERFORM 3200-SETUP-SCREEN-VARS                                       
              THRU 3200-SETUP-SCREEN-VARS-EXIT                                  
           PERFORM 3250-SETUP-INFOMSG                                           
              THRU 3250-SETUP-INFOMSG-EXIT                                      
           PERFORM 3300-SETUP-SCREEN-ATTRS                                      
              THRU 3300-SETUP-SCREEN-ATTRS-EXIT                                 
           PERFORM 3400-SEND-SCREEN                                             
              THRU 3400-SEND-SCREEN-EXIT                                        
           .                                                                    
                                                                                
       3000-SEND-MAP-EXIT.                                                      
           EXIT                                                                 
           .                                                                    
                                                                                
       3100-SCREEN-INIT.                                                        
           MOVE LOW-VALUES TO CCRDUPAO                                          
                                                                                
           MOVE FUNCTION CURRENT-DATE     TO WS-CURDATE-DATA                    
                                                                                
           MOVE CCDA-TITLE01              TO TITLE01O OF CCRDUPAO               
           MOVE CCDA-TITLE02              TO TITLE02O OF CCRDUPAO               
           MOVE LIT-THISTRANID            TO TRNNAMEO OF CCRDUPAO               
           MOVE LIT-THISPGM               TO PGMNAMEO OF CCRDUPAO               
                                                                                
           MOVE FUNCTION CURRENT-DATE     TO WS-CURDATE-DATA                    
                                                                                
           MOVE WS-CURDATE-MONTH          TO WS-CURDATE-MM                      
           MOVE WS-CURDATE-DAY            TO WS-CURDATE-DD                      
           MOVE WS-CURDATE-YEAR(3:2)      TO WS-CURDATE-YY                      
                                                                                
           MOVE WS-CURDATE-MM-DD-YY       TO CURDATEO OF CCRDUPAO               
                                                                                
           MOVE WS-CURTIME-HOURS          TO WS-CURTIME-HH                      
           MOVE WS-CURTIME-MINUTE         TO WS-CURTIME-MM                      
           MOVE WS-CURTIME-SECOND         TO WS-CURTIME-SS                      
                                                                                
           MOVE WS-CURTIME-HH-MM-SS       TO CURTIMEO OF CCRDUPAO               
                                                                                
           .                                                                    
                                                                                
       3100-SCREEN-INIT-EXIT.                                                   
           EXIT                                                                 
           .                                                                    
                                                                                
       3200-SETUP-SCREEN-VARS.                                                  
      *    INITIALIZE SEARCH CRITERIA                                           
           IF CDEMO-PGM-ENTER                                                   
              CONTINUE                                                          
           ELSE                                                                 
              IF CC-ACCT-ID-N = 0                                               
                 MOVE LOW-VALUES          TO ACCTSIDO OF CCRDUPAO               
              ELSE                                                              
                 MOVE CC-ACCT-ID          TO ACCTSIDO OF CCRDUPAO               
              END-IF                                                            
                                                                                
              IF CC-CARD-NUM-N = 0                                              
                MOVE LOW-VALUES           TO CARDSIDO OF CCRDUPAO               
              ELSE                                                              
                MOVE CC-CARD-NUM          TO CARDSIDO OF CCRDUPAO               
              END-IF                                                            
                                                                                
              EVALUATE TRUE                                                     
                  WHEN CCUP-DETAILS-NOT-FETCHED                                 
