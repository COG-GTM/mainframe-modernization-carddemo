                                                   PIC 9(11).                   
           10  CARD-CVV-CD-X                       PIC X(03).                   
           10  CARD-CVV-CD-N REDEFINES  CARD-CVV-CD-X                           
                                                   PIC 9(03).                   
           10  FLG-PROTECT-SELECT-ROWS             PIC X(1).                    
           88  FLG-PROTECT-SELECT-ROWS-NO          VALUE '0'.                   
           88  FLG-PROTECT-SELECT-ROWS-YES         VALUE '1'.                   
      ******************************************************************        
      * Output Message Construction                                             
      ******************************************************************        
         05  WS-LONG-MSG                           PIC X(500).                  
         05  WS-INFO-MSG                           PIC X(45).                   
           88  WS-NO-INFO-MESSAGE                 VALUES                        
                                                  SPACES LOW-VALUES.            
           88  WS-INFORM-REC-ACTIONS          VALUE                             
               'TYPE S FOR DETAIL, U TO UPDATE ANY RECORD'.                     
         05  WS-ERROR-MSG                         PIC X(75).                    
           88  WS-ERROR-MSG-OFF                   VALUE SPACES.                 
           88  WS-EXIT-MESSAGE                     VALUE                        
               'PF03 PRESSED.EXITING'.                                          
           88  WS-NO-RECORDS-FOUND                 VALUE                        
               'NO RECORDS FOUND FOR THIS SEARCH CONDITION.'.                   
           88  WS-MORE-THAN-1-ACTION              VALUE                         
               'PLEASE SELECT ONLY ONE RECORD TO VIEW OR UPDATE'.               
           88  WS-INVALID-ACTION-CODE              VALUE                        
               'INVALID ACTION CODE'.                                           
         05  WS-PFK-FLAG                           PIC X(1).                    
           88  PFK-VALID                           VALUE '0'.                   
           88  PFK-INVALID                         VALUE '1'.                   
         05  WS-CONTEXT-FLAG                       PIC X(1).                    
           88  WS-CONTEXT-FRESH-START              VALUE '0'.                   
           88  WS-CONTEXT-FRESH-START-NO           VALUE '1'.                   
      ******************************************************************        
      * File and data Handling                                                  
      ******************************************************************        
         05 WS-FILE-HANDLING-VARS.                                              
            10  WS-CARD-RID.                                                    
                20  WS-CARD-RID-CARDNUM            PIC X(16).                   
                20  WS-CARD-RID-ACCT-ID            PIC 9(11).                   
                20  WS-CARD-RID-ACCT-ID-X          REDEFINES                    
                    WS-CARD-RID-ACCT-ID            PIC X(11).                   
                                                                                
                                                                                
                                                                                
         05  WS-SCRN-COUNTER               PIC S9(4) COMP VALUE 0.              
                                                                                
         05  WS-FILTER-RECORD-FLAG                 PIC X(1).                    
           88  WS-EXCLUDE-THIS-RECORD               VALUE '0'.                  
           88  WS-DONOT-EXCLUDE-THIS-RECORD         VALUE '1'.                  
         05  WS-RECORDS-TO-PROCESS-FLAG            PIC X(1).                    
           88  READ-LOOP-EXIT                      VALUE '0'.                   
           88  MORE-RECORDS-TO-READ                VALUE '1'.                   
         05  WS-FILE-ERROR-MESSAGE.                                             
           10  FILLER                              PIC X(12)                    
                                                   VALUE 'File Error:'.         
           10  ERROR-OPNAME                        PIC X(8)                     
                                                   VALUE SPACES.                
           10  FILLER                              PIC X(4)                     
                                                   VALUE ' on '.                
           10  ERROR-FILE                          PIC X(9)                     
                                                   VALUE SPACES.                
           10  FILLER                              PIC X(15)                    
                                                   VALUE                        
                                                   ' returned RESP '.           
           10  ERROR-RESP                          PIC X(10)                    
                                                   VALUE SPACES.                
           10  FILLER                              PIC X(7)                     
                                                   VALUE ',RESP2 '.             
           10  ERROR-RESP2                         PIC X(10)                    
                                                   VALUE SPACES.                
          10  FILLER                               PIC X(5).                    
                                                                                
      ******************************************************************
      * Literals and Constants                                                  
      ******************************************************************        
       01 WS-CONSTANTS.                                                         
         05  WS-MAX-SCREEN-LINES                    PIC S9(4) COMP              
                                                    VALUE 7.                    
         05  LIT-THISPGM                            PIC X(8)                    
             VALUE 'COCRDLIC'.                                                  
         05  LIT-THISTRANID                         PIC X(4)                    
             VALUE 'CCLI'.                                                      
         05  LIT-THISMAPSET                         PIC X(7)                    
             VALUE 'COCRDLI'.                                                   
         05  LIT-THISMAP                            PIC X(7)                    
             VALUE 'CCRDLIA'.                                                   
         05  LIT-MENUPGM                            PIC X(8)                    
             VALUE 'COMEN01C'.                                                  
         05  LIT-MENUTRANID                         PIC X(4)                    
             VALUE 'CM00'.                                                      
         05  LIT-MENUMAPSET                         PIC X(7)                    
             VALUE 'COMEN01'.                                                   
         05  LIT-MENUMAP                            PIC X(7)                    
             VALUE 'COMEN1A'.                                                   
         05  LIT-CARDDTLPGM                         PIC X(8)                    
             VALUE 'COCRDSLC'.                                                  
         05  LIT-CARDDTLTRANID                      PIC X(4)                    
             VALUE 'CCDL'.                                                      
         05  LIT-CARDDTLMAPSET                      PIC X(7)                    
             VALUE 'COCRDSL'.                                                   
