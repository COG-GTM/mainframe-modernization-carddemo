      ******************************************************************        
              WHEN CCUP-DETAILS-NOT-FETCHED                                     
               AND CDEMO-PGM-ENTER                                              
              WHEN CDEMO-FROM-PROGRAM   EQUAL LIT-MENUPGM                       
               AND NOT CDEMO-PGM-REENTER                                        
                   INITIALIZE WS-THIS-PROGCOMMAREA                              
                   PERFORM 3000-SEND-MAP THRU                                   
                           3000-SEND-MAP-EXIT                                   
                   SET CDEMO-PGM-REENTER        TO TRUE                         
                   SET CCUP-DETAILS-NOT-FETCHED TO TRUE                         
                   GO TO COMMON-RETURN                                          
      ******************************************************************        
      *       CARD DATA CHANGES REVIEWED, OKAYED AND DONE SUCESSFULLY           
      *            RESET THE SEARCH KEYS                                        
      *            ASK THE USER FOR FRESH SEARCH CRITERIA                       
      ******************************************************************        
              WHEN CCUP-CHANGES-OKAYED-AND-DONE                                 
              WHEN CCUP-CHANGES-FAILED                                          
                   INITIALIZE WS-THIS-PROGCOMMAREA                              
                              WS-MISC-STORAGE                                   
                              CDEMO-ACCT-ID                                     
                              CDEMO-CARD-NUM                                    
                   SET CDEMO-PGM-ENTER            TO TRUE                       
                   PERFORM 3000-SEND-MAP THRU                                   
                           3000-SEND-MAP-EXIT                                   
                   SET CDEMO-PGM-REENTER          TO TRUE                       
                   SET CCUP-DETAILS-NOT-FETCHED   TO TRUE                       
                   GO TO COMMON-RETURN                                          
      ******************************************************************        
      *      CARD DATA HAS BEEN PRESENTED TO USER                               
      *            CHECK THE USER INPUTS                                        
      *            DECIDE WHAT TO DO                                            
      *            PRESENT NEXT STEPS TO USER                                   
      ******************************************************************        
              WHEN OTHER                                                        
                   PERFORM 1000-PROCESS-INPUTS                                  
                      THRU 1000-PROCESS-INPUTS-EXIT                             
                   PERFORM 2000-DECIDE-ACTION                                   
                      THRU 2000-DECIDE-ACTION-EXIT                              
                   PERFORM 3000-SEND-MAP                                        
                      THRU 3000-SEND-MAP-EXIT                                   
                   GO TO COMMON-RETURN                                          
           END-EVALUATE                                                         
           .                                                                    
                                                                                
       COMMON-RETURN.                                                           
           MOVE WS-RETURN-MSG     TO CCARD-ERROR-MSG                            
                                                                                
           MOVE  CARDDEMO-COMMAREA    TO WS-COMMAREA                            
           MOVE  WS-THIS-PROGCOMMAREA TO                                        
                  WS-COMMAREA(LENGTH OF CARDDEMO-COMMAREA + 1:                  
                               LENGTH OF WS-THIS-PROGCOMMAREA )                 
                                                                                
           EXEC CICS RETURN                                                     
                TRANSID (LIT-THISTRANID)                                        
                COMMAREA (WS-COMMAREA)                                          
                LENGTH(LENGTH OF WS-COMMAREA)                                   
           END-EXEC                                                             
           .                                                                    
       0000-MAIN-EXIT.                                                          
           EXIT                                                                 
           .                                                                    
                                                                                
       1000-PROCESS-INPUTS.                                                     
           PERFORM 1100-RECEIVE-MAP                                             
              THRU 1100-RECEIVE-MAP-EXIT                                        
           PERFORM 1200-EDIT-MAP-INPUTS                                         
              THRU 1200-EDIT-MAP-INPUTS-EXIT                                    
           MOVE WS-RETURN-MSG  TO CCARD-ERROR-MSG                               
           MOVE LIT-THISPGM    TO CCARD-NEXT-PROG                               
           MOVE LIT-THISMAPSET TO CCARD-NEXT-MAPSET                             
           MOVE LIT-THISMAP    TO CCARD-NEXT-MAP                                
           .                                                                    
                                                                                
       1000-PROCESS-INPUTS-EXIT.                                                
           EXIT                                                                 
           .                                                                    
       1100-RECEIVE-MAP.                                                        
           EXEC CICS RECEIVE MAP(LIT-THISMAP)                                   
                     MAPSET(LIT-THISMAPSET)                                     
                     INTO(CCRDUPAI)                                             
                     RESP(WS-RESP-CD)                                           
                     RESP2(WS-REAS-CD)                                          
           END-EXEC                                                             
                                                                                
           INITIALIZE CCUP-NEW-DETAILS                                          
                                                                                
      *    REPLACE * WITH LOW-VALUES                                            
           IF  ACCTSIDI OF CCRDUPAI = '*'                                       
           OR  ACCTSIDI OF CCRDUPAI = SPACES                                    
               MOVE LOW-VALUES           TO  CC-ACCT-ID                         
                                             CCUP-NEW-ACCTID                    
           ELSE                                                                 
               MOVE ACCTSIDI OF CCRDUPAI TO  CC-ACCT-ID                         
                                             CCUP-NEW-ACCTID                    
           END-IF                                                               
                                                                                
           IF  CARDSIDI OF CCRDUPAI = '*'                                       
           OR  CARDSIDI OF CCRDUPAI = SPACES                                    
               MOVE LOW-VALUES           TO  CC-CARD-NUM                        
