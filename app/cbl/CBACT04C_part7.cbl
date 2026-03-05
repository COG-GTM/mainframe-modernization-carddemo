               MOVE 12 TO APPL-RESULT                                           
           END-IF                                                               
           IF  APPL-AOK                                                         
               CONTINUE                                                         
           ELSE                                                                 
               DISPLAY 'ERROR CLOSING TRANSACTION FILE'                         
               MOVE TRANFILE-STATUS  TO IO-STATUS                               
               PERFORM 9910-DISPLAY-IO-STATUS                                   
               PERFORM 9999-ABEND-PROGRAM                                       
           END-IF                                                               
           EXIT.                                                                
                                                                                
       Z-GET-DB2-FORMAT-TIMESTAMP.                                              
           MOVE FUNCTION CURRENT-DATE TO COBOL-TS                               
           MOVE COB-YYYY TO DB2-YYYY                                            
           MOVE COB-MM   TO DB2-MM                                              
           MOVE COB-DD   TO DB2-DD                                              
           MOVE COB-HH   TO DB2-HH                                              
           MOVE COB-MIN  TO DB2-MIN                                             
           MOVE COB-SS   TO DB2-SS                                              
           MOVE COB-MIL  TO DB2-MIL                                             
           MOVE '0000'   TO DB2-REST                                            
           MOVE '-' TO DB2-STREEP-1 DB2-STREEP-2 DB2-STREEP-3                   
           MOVE '.' TO DB2-DOT-1 DB2-DOT-2 DB2-DOT-3                            
      *    DISPLAY 'DB2-TIMESTAMP = ' DB2-FORMAT-TS                             
           EXIT.                                                                
                                                                                
       9999-ABEND-PROGRAM.                                                      
           DISPLAY 'ABENDING PROGRAM'                                           
           MOVE 0 TO TIMING                                                     
           MOVE 999 TO ABCODE                                                   
           CALL 'CEE3ABD'.                                                      
                                                                                
      *****************************************************************         
       9910-DISPLAY-IO-STATUS.                                                  
           IF  IO-STATUS NOT NUMERIC                                            
           OR  IO-STAT1 = '9'                                                   
               MOVE IO-STAT1 TO IO-STATUS-04(1:1)                               
               MOVE 0        TO TWO-BYTES-BINARY                                
               MOVE IO-STAT2 TO TWO-BYTES-RIGHT                                 
               MOVE TWO-BYTES-BINARY TO IO-STATUS-0403                          
               DISPLAY 'FILE STATUS IS: NNNN' IO-STATUS-04                      
           ELSE                                                                 
               MOVE '0000' TO IO-STATUS-04                                      
               MOVE IO-STATUS TO IO-STATUS-04(3:2)                              
               DISPLAY 'FILE STATUS IS: NNNN' IO-STATUS-04                      
           END-IF                                                               
           EXIT.                                                                
                                                                                
      *
      * Ver: CardDemo_v1.0-15-g27d6c6f-68 Date: 2022-07-19 23:12:31 CDT
      *
