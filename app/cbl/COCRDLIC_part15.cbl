                  GO TO 9500-FILTER-RECORDS-EXIT                                
              END-IF                                                            
           ELSE                                                                 
             CONTINUE                                                           
           END-IF                                                               
                                                                                
           .                                                                    
                                                                                
       9500-FILTER-RECORDS-EXIT.                                                
           EXIT                                                                 
           .                                                                    
                                                                                
      *****************************************************************
      *Common code to store PFKey                                      
      *****************************************************************
       COPY 'CSSTRPFY'
           .

      *****************************************************************         
      * Plain text exit - Dont use in production                      *         
      *****************************************************************         
       SEND-PLAIN-TEXT.                                                         
           EXEC CICS SEND TEXT                                                  
                     FROM(WS-ERROR-MSG)                                         
                     LENGTH(LENGTH OF WS-ERROR-MSG)                             
                     ERASE                                                      
                     FREEKB                                                     
           END-EXEC                                                             
                                                                                
           EXEC CICS RETURN                                                     
           END-EXEC                                                             
           .                                                                    
       SEND-PLAIN-TEXT-EXIT.                                                    
           EXIT                                                                 
           .                                                                    
      *****************************************************************         
      * Display Long text and exit                                    *         
      * This is primarily for debugging and should not be used in     *         
      * regular course                                                *         
      *****************************************************************         
       SEND-LONG-TEXT.                                                          
           EXEC CICS SEND TEXT                                                  
                     FROM(WS-LONG-MSG)                                          
                     LENGTH(LENGTH OF WS-LONG-MSG)                              
                     ERASE                                                      
                     FREEKB                                                     
           END-EXEC                                                             
                                                                                
           EXEC CICS RETURN                                                     
           END-EXEC                                                             
           .                                                                    
       SEND-LONG-TEXT-EXIT.                                                     
           EXIT                                                                 
           .                                                                    
                                                                                
                                                                                
      *
      * Ver: CardDemo_v1.0-15-g27d6c6f-68 Date: 2022-07-19 23:12:33 CDT
      *
