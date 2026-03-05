                       MOVE LOW-VALUES         TO CRDNAMEO OF CCRDUPAO          
                                                  CRDNAMEO OF CCRDUPAO          
                                                  CRDSTCDO OF CCRDUPAO          
                                                  EXPDAYO  OF CCRDUPAO          
                                                  EXPMONO  OF CCRDUPAO          
                                                  EXPYEARO OF CCRDUPAO          
                  WHEN CCUP-SHOW-DETAILS                                        
                      MOVE CCUP-OLD-CRDNAME    TO CRDNAMEO OF CCRDUPAO          
                      MOVE CCUP-OLD-CRDSTCD    TO CRDSTCDO OF CCRDUPAO          
                      MOVE CCUP-OLD-EXPDAY     TO EXPDAYO  OF CCRDUPAO          
                      MOVE CCUP-OLD-EXPMON     TO EXPMONO  OF CCRDUPAO          
                      MOVE CCUP-OLD-EXPYEAR    TO EXPYEARO OF CCRDUPAO          
                  WHEN CCUP-CHANGES-MADE                                        
                      MOVE CCUP-NEW-CRDNAME    TO CRDNAMEO OF CCRDUPAO          
                      MOVE CCUP-NEW-CRDSTCD    TO CRDSTCDO OF CCRDUPAO          
                      MOVE CCUP-NEW-EXPMON     TO EXPMONO  OF CCRDUPAO          
                      MOVE CCUP-NEW-EXPYEAR    TO EXPYEARO OF CCRDUPAO          
      ******************************************************************        
      *               MOVE OLD VALUES TO NON-DISPLAY FIELDS                     
      *               THAT WE ARE NOT ALLOWING USER TO CHANGE(FOR NOW)          
      *****************************************************************         
      *               MOVE CCUP-NEW-EXPDAY     TO EXPDAYO  OF CCRDUPAO          
                      MOVE CCUP-OLD-EXPDAY     TO EXPDAYO  OF CCRDUPAO          
                  WHEN OTHER                                                    
                      MOVE CCUP-OLD-CRDNAME    TO CRDNAMEO OF CCRDUPAO          
                      MOVE CCUP-OLD-CRDSTCD    TO CRDSTCDO OF CCRDUPAO          
                      MOVE CCUP-OLD-EXPDAY     TO EXPDAYO  OF CCRDUPAO          
                      MOVE CCUP-OLD-EXPMON     TO EXPMONO  OF CCRDUPAO          
                      MOVE CCUP-OLD-EXPYEAR    TO EXPYEARO OF CCRDUPAO          
              END-EVALUATE                                                      
                                                                                
                                                                                
            END-IF                                                              
           .                                                                    
       3200-SETUP-SCREEN-VARS-EXIT.                                             
           EXIT                                                                 
           .                                                                    
       3250-SETUP-INFOMSG.                                                      
      *    SETUP INFORMATION MESSAGE                                            
           EVALUATE TRUE                                                        
               WHEN CDEMO-PGM-ENTER                                             
                    SET  PROMPT-FOR-SEARCH-KEYS TO TRUE                         
               WHEN CCUP-DETAILS-NOT-FETCHED                                    
                   SET PROMPT-FOR-SEARCH-KEYS      TO TRUE                      
               WHEN CCUP-SHOW-DETAILS                                           
                    SET FOUND-CARDS-FOR-ACCOUNT    TO TRUE                      
               WHEN CCUP-CHANGES-NOT-OK                                         
                    SET PROMPT-FOR-CHANGES         TO TRUE                      
               WHEN CCUP-CHANGES-OK-NOT-CONFIRMED                               
                    SET PROMPT-FOR-CONFIRMATION    TO TRUE                      
               WHEN CCUP-CHANGES-OKAYED-AND-DONE                                
                    SET CONFIRM-UPDATE-SUCCESS     TO TRUE                      
               WHEN CCUP-CHANGES-OKAYED-LOCK-ERROR                              
                    SET INFORM-FAILURE             TO TRUE                      
               WHEN CCUP-CHANGES-OKAYED-BUT-FAILED                              
                    SET INFORM-FAILURE             TO TRUE                      
               WHEN WS-NO-INFO-MESSAGE                                          
                   SET PROMPT-FOR-SEARCH-KEYS      TO TRUE                      
           END-EVALUATE                                                         
                                                                                
           MOVE WS-INFO-MSG                    TO INFOMSGO OF CCRDUPAO          
                                                                                
           MOVE WS-RETURN-MSG                  TO ERRMSGO OF CCRDUPAO           
           .                                                                    
       3250-SETUP-INFOMSG-EXIT.                                                 
           EXIT                                                                 
           .                                                                    
       3300-SETUP-SCREEN-ATTRS.                                                 
                                                                                
                                                                                
      *    PROTECT OR UNPROTECT BASED ON CONTEXT                                
           EVALUATE TRUE                                                        
              WHEN CCUP-DETAILS-NOT-FETCHED                                     
                   MOVE DFHBMFSE      TO ACCTSIDA OF CCRDUPAI                   
                                         CARDSIDA OF CCRDUPAI                   
                   MOVE DFHBMPRF      TO CRDNAMEA OF CCRDUPAI                   
                                         CRDSTCDA OF CCRDUPAI                   
      *                                  EXPDAYA  OF CCRDUPAI                   
                                         EXPMONA  OF CCRDUPAI                   
                                         EXPYEARA OF CCRDUPAI                   
              WHEN  CCUP-SHOW-DETAILS                                           
              WHEN  CCUP-CHANGES-NOT-OK                                         
                   MOVE DFHBMPRF      TO ACCTSIDA OF CCRDUPAI                   
                                         CARDSIDA OF CCRDUPAI                   
      *                                  EXPDAYA  OF CCRDUPAI                   
                   MOVE DFHBMFSE      TO CRDNAMEA OF CCRDUPAI                   
                                         CRDSTCDA OF CCRDUPAI                   
                                                                                
                                         EXPMONA  OF CCRDUPAI                   
                                         EXPYEARA OF CCRDUPAI                   
              WHEN CCUP-CHANGES-OK-NOT-CONFIRMED                                
              WHEN CCUP-CHANGES-OKAYED-AND-DONE                                 
                   MOVE DFHBMPRF      TO ACCTSIDA OF CCRDUPAI                   
                                         CARDSIDA OF CCRDUPAI                   
                                         CRDNAMEA OF CCRDUPAI                   
                                         CRDSTCDA OF CCRDUPAI                   
      *                                  EXPDAYA  OF CCRDUPAI                   
                                         EXPMONA  OF CCRDUPAI                   
                                         EXPYEARA OF CCRDUPAI                   
              WHEN OTHER                                                        
