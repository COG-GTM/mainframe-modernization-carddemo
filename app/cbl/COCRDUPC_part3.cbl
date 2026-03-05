           88  DID-NOT-FIND-ACCT-IN-CARDXREF       VALUE                        
               'Did not find this account in cards database'.                   
           88  DID-NOT-FIND-ACCTCARD-COMBO         VALUE                        
               'Did not find cards for this search condition'.                  
           88  COULD-NOT-LOCK-FOR-UPDATE           VALUE                        
               'Could not lock record for update'.                              
           88  DATA-WAS-CHANGED-BEFORE-UPDATE      VALUE                        
               'Record changed by some one else. Please review'.                
           88  LOCKED-BUT-UPDATE-FAILED            VALUE                        
               'Update of record failed'.                                       
           88  XREF-READ-ERROR                     VALUE                        
               'Error reading Card Data File'.                                  
           88  CODING-TO-BE-DONE                   VALUE                        
               'Looks Good.... so far'.                                         
      ******************************************************************        
      *      Literals and Constants                                             
      ******************************************************************        
       01 WS-LITERALS.                                                          
          05 LIT-THISPGM                           PIC X(8)                     
                                                   VALUE 'COCRDUPC'.            
          05 LIT-THISTRANID                        PIC X(4)                     
                                                   VALUE 'CCUP'.                
          05 LIT-THISMAPSET                        PIC X(8)                     
                                                   VALUE 'COCRDUP '.            
          05 LIT-THISMAP                           PIC X(7)                     
                                                   VALUE 'CCRDUPA'.             
          05 LIT-CCLISTPGM                         PIC X(8)                     
                                                   VALUE 'COCRDLIC'.            
          05 LIT-CCLISTTRANID                      PIC X(4)                     
                                                   VALUE 'CCLI'.                
          05 LIT-CCLISTMAPSET                      PIC X(7)                     
                                                   VALUE 'COCRDLI'.             
          05 LIT-CCLISTMAP                         PIC X(7)                     
                                                   VALUE 'CCRDSLA'.             
          05 LIT-MENUPGM                           PIC X(8)                     
                                                   VALUE 'COMEN01C'.            
          05 LIT-MENUTRANID                        PIC X(4)                     
                                                   VALUE 'CM00'.                
          05 LIT-MENUMAPSET                        PIC X(7)                     
                                                   VALUE 'COMEN01'.             
          05 LIT-MENUMAP                           PIC X(7)                     
                                                   VALUE 'COMEN1A'.             
          05  LIT-CARDDTLPGM                       PIC X(8)                     
                                                   VALUE 'COCRDSLC'.            
          05  LIT-CARDDTLTRANID                    PIC X(4)                     
                                                   VALUE 'CCDL'.                
          05  LIT-CARDDTLMAPSET                    PIC X(7)                     
                                                   VALUE 'COCRDSL'.             
          05  LIT-CARDDTLMAP                       PIC X(7)                     
                                                   VALUE 'CCRDSLA'.             
          05 LIT-CARDFILENAME                      PIC X(8)                     
                                                   VALUE 'CARDDAT '.            
          05 LIT-CARDFILENAME-ACCT-PATH            PIC X(8)                     
                                                   VALUE 'CARDAIX '.            
          05 LIT-ALL-ALPHA-FROM                    PIC X(52)                    
             VALUE                                                              
             'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.            
          05 LIT-ALL-SPACES-TO                     PIC X(52)                    
                                                   VALUE SPACES.                
          05 LIT-UPPER                             PIC X(26)                    
                                 VALUE 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.            
          05 LIT-LOWER                             PIC X(26)                    
                                 VALUE 'abcdefghijklmnopqrstuvwxyz'.            
                                                                                
      ******************************************************************        
      *Other common working storage Variables                                   
      ******************************************************************        
       COPY CVCRD01Y.                                                           
                                                                                
      ******************************************************************        
      *Application Commmarea Copybook                                           
       COPY COCOM01Y.                                                           
                                                                                
       01 WS-THIS-PROGCOMMAREA.                                                 
          05 CARD-UPDATE-SCREEN-DATA.                                           
             10 CCUP-CHANGE-ACTION                 PIC X(1)                     
                                                   VALUE LOW-VALUES.            
                88 CCUP-DETAILS-NOT-FETCHED        VALUES                       
                                                   LOW-VALUES,                  
                                                   SPACES.                      
                88 CCUP-SHOW-DETAILS               VALUE 'S'.                   
                88 CCUP-CHANGES-MADE               VALUES 'E', 'N'              
                                                        , 'C', 'L'              
                                                        , 'F'.                  
                88 CCUP-CHANGES-NOT-OK             VALUE 'E'.                   
                88 CCUP-CHANGES-OK-NOT-CONFIRMED   VALUE 'N'.                   
                88 CCUP-CHANGES-OKAYED-AND-DONE    VALUE 'C'.                   
                88 CCUP-CHANGES-FAILED             VALUES 'L', 'F'.             
                88 CCUP-CHANGES-OKAYED-LOCK-ERROR  VALUE 'L'.                   
                88 CCUP-CHANGES-OKAYED-BUT-FAILED  VALUE 'F'.                   
          05 CCUP-OLD-DETAILS.                                                  
             10 CCUP-OLD-ACCTID                    PIC X(11).                   
             10 CCUP-OLD-CARDID                    PIC X(16).                   
             10 CCUP-OLD-CVV-CD                    PIC X(3).                    
             10 CCUP-OLD-CARDDATA.                                              
                20 CCUP-OLD-CRDNAME                PIC X(50).                   
                20 CCUP-OLD-EXPIRAION-DATE.                                     
                   25 CCUP-OLD-EXPYEAR             PIC X(4).                    
                   25 CCUP-OLD-EXPMON              PIC X(2).                    
                   25 CCUP-OLD-EXPDAY              PIC X(2).                    
