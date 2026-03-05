                WHEN CCARD-AID-PFK07                                            
                    AND CA-FIRST-PAGE                                           
                  MOVE 'NO PREVIOUS PAGES TO DISPLAY'                           
                  TO WS-ERROR-MSG                                               
                WHEN CCARD-AID-PFK08                                            
                 AND CA-NEXT-PAGE-NOT-EXISTS                                    
                 AND CA-LAST-PAGE-SHOWN                                         
                  MOVE 'NO MORE PAGES TO DISPLAY'                               
                  TO WS-ERROR-MSG                                               
                WHEN CCARD-AID-PFK08                                            
                 AND CA-NEXT-PAGE-NOT-EXISTS                                    
                  SET WS-INFORM-REC-ACTIONS TO TRUE                             
                  IF  CA-LAST-PAGE-NOT-SHOWN                                    
                  AND CA-NEXT-PAGE-NOT-EXISTS                                   
                      SET CA-LAST-PAGE-SHOWN TO TRUE                            
                  END-IF                                                        
                WHEN WS-NO-INFO-MESSAGE                                         
                WHEN CA-NEXT-PAGE-EXISTS                                        
                  SET WS-INFORM-REC-ACTIONS TO TRUE                             
                WHEN OTHER                                                      
                   SET WS-NO-INFO-MESSAGE TO TRUE                               
           END-EVALUATE                                                         
                                                                                
           MOVE WS-ERROR-MSG          TO ERRMSGO OF CCRDLIAO                    
                                                                                
           IF  NOT WS-NO-INFO-MESSAGE                                           
           AND NOT WS-NO-RECORDS-FOUND                                          
              MOVE WS-INFO-MSG        TO INFOMSGO OF CCRDLIAO                   
              MOVE DFHNEUTR           TO INFOMSGC OF CCRDLIAO                   
           END-IF                                                               
                                                                                
           .                                                                    
       1400-SETUP-MESSAGE-EXIT.                                                 
           EXIT                                                                 
           .                                                                    
                                                                                
                                                                                
       1500-SEND-SCREEN.                                                        
           EXEC CICS SEND MAP(LIT-THISMAP)                                      
                          MAPSET(LIT-THISMAPSET)                                
                          FROM(CCRDLIAO)                                        
                          CURSOR                                                
                          ERASE                                                 
                          RESP(WS-RESP-CD)                                      
                          FREEKB                                                
           END-EXEC                                                             
           .                                                                    
       1500-SEND-SCREEN-EXIT.                                                   
           EXIT                                                                 
           .                                                                    
       2000-RECEIVE-MAP.                                                        
           PERFORM 2100-RECEIVE-SCREEN                                          
              THRU 2100-RECEIVE-SCREEN-EXIT                                     
                                                                                
           PERFORM 2200-EDIT-INPUTS                                             
            THRU   2200-EDIT-INPUTS-EXIT                                        
           .                                                                    
                                                                                
       2000-RECEIVE-MAP-EXIT.                                                   
           EXIT                                                                 
           .                                                                    
       2100-RECEIVE-SCREEN.                                                     
           EXEC CICS RECEIVE MAP(LIT-THISMAP)                                   
                          MAPSET(LIT-THISMAPSET)                                
                          INTO(CCRDLIAI)                                        
                          RESP(WS-RESP-CD)                                      
           END-EXEC                                                             
                                                                                
           MOVE ACCTSIDI OF CCRDLIAI  TO CC-ACCT-ID                             
           MOVE CARDSIDI OF CCRDLIAI  TO CC-CARD-NUM                            
                                                                                
           MOVE CRDSEL1I OF CCRDLIAI  TO WS-EDIT-SELECT(1)                      
           MOVE CRDSEL2I OF CCRDLIAI  TO WS-EDIT-SELECT(2)                      
           MOVE CRDSEL3I OF CCRDLIAI  TO WS-EDIT-SELECT(3)                      
           MOVE CRDSEL4I OF CCRDLIAI  TO WS-EDIT-SELECT(4)                      
           MOVE CRDSEL5I OF CCRDLIAI  TO WS-EDIT-SELECT(5)                      
           MOVE CRDSEL6I OF CCRDLIAI  TO WS-EDIT-SELECT(6)                      
           MOVE CRDSEL7I OF CCRDLIAI  TO WS-EDIT-SELECT(7)                      
           .                                                                    
                                                                                
       2100-RECEIVE-SCREEN-EXIT.                                                
           EXIT                                                                 
           .                                                                    
                                                                                
       2200-EDIT-INPUTS.                                                        
           SET INPUT-OK                   TO TRUE                               
           SET FLG-PROTECT-SELECT-ROWS-NO TO TRUE                               
                                                                                
           PERFORM 2210-EDIT-ACCOUNT                                            
              THRU 2210-EDIT-ACCOUNT-EXIT                                       
                                                                                
           PERFORM 2220-EDIT-CARD                                               
              THRU 2220-EDIT-CARD-EXIT                                          
                                                                                
           PERFORM 2250-EDIT-ARRAY                                              
              THRU 2250-EDIT-ARRAY-EXIT                                         
           .                                                                    
                                                                                
       2200-EDIT-INPUTS-EXIT.                                                   
           EXIT                                                                 
