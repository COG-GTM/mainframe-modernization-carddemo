         05  LIT-CARDDTLMAP                         PIC X(7)                    
             VALUE 'CCRDSLA'.                                                   
         05  LIT-CARDUPDPGM                         PIC X(8)                    
             VALUE 'COCRDUPC'.                                                  
         05  LIT-CARDUPDTRANID                      PIC X(4)                    
             VALUE 'CCUP'.                                                      
         05  LIT-CARDUPDMAPSET                      PIC X(7)                    
             VALUE 'COCRDUP'.                                                   
         05  LIT-CARDUPDMAP                         PIC X(7)                    
             VALUE 'CCRDUPA'.                                                   
                                                                                
                                                                                
         05  LIT-CARD-FILE                          PIC X(8)                    
                                                   VALUE 'CARDDAT '.            
         05  LIT-CARD-FILE-ACCT-PATH                PIC X(8)                    
                                                                                
                                                   VALUE 'CARDAIX '.            
      ******************************************************************        
      *Other common working storage Variables                                   
      ******************************************************************        
       COPY CVCRD01Y.                                                           
                                                                                
      ******************************************************************        
      *  Commarea manipulations                                                 
      ******************************************************************        
      *Application Commmarea Copybook                                           
       COPY COCOM01Y.                                                           
                                                                                
       01 WS-THIS-PROGCOMMAREA.                                                 
            10 WS-CA-LAST-CARDKEY.                                              
               15  WS-CA-LAST-CARD-NUM                PIC X(16).                
               15  WS-CA-LAST-CARD-ACCT-ID            PIC 9(11).                
            10 WS-CA-FIRST-CARDKEY.                                             
               15  WS-CA-FIRST-CARD-NUM               PIC X(16).                
               15  WS-CA-FIRST-CARD-ACCT-ID           PIC 9(11).                
                                                                                
            10 WS-CA-SCREEN-NUM                       PIC 9(1).                 
               88 CA-FIRST-PAGE                          VALUE 1.               
            10 WS-CA-LAST-PAGE-DISPLAYED              PIC 9(1).                 
               88 CA-LAST-PAGE-SHOWN                     VALUE 0.               
               88 CA-LAST-PAGE-NOT-SHOWN                 VALUE 9.               
            10 WS-CA-NEXT-PAGE-IND                    PIC X(1).                 
               88 CA-NEXT-PAGE-NOT-EXISTS             VALUE LOW-VALUES.         
               88 CA-NEXT-PAGE-EXISTS                 VALUE 'Y'.                
                                                                                
            10 WS-RETURN-FLAG                        PIC X(1).                  
           88  WS-RETURN-FLAG-OFF                  VALUE LOW-VALUES.            
           88  WS-RETURN-FLAG-ON                   VALUE '1'.                   
      ******************************************************************        
      *  File Data Array         28 CHARS X 7 ROWS = 196                        
      ******************************************************************        
         05 WS-SCREEN-DATA.                                                     
            10 WS-ALL-ROWS                         PIC X(196).                  
            10 FILLER REDEFINES WS-ALL-ROWS.                                    
               15 WS-SCREEN-ROWS OCCURS  7 TIMES.                               
                  20 WS-EACH-ROW.                                               
                     25 WS-EACH-CARD.                                           
                        30 WS-ROW-ACCTNO           PIC X(11).                   
                        30 WS-ROW-CARD-NUM         PIC X(16).                   
                        30 WS-ROW-CARD-STATUS      PIC X(1).                    
                                                                                
       01  WS-COMMAREA                             PIC X(2000).                 
                                                                                
                                                                                
                                                                                
      *IBM SUPPLIED COPYBOOKS                                                   
       COPY DFHBMSCA.                                                           
       COPY DFHAID.                                                             
                                                                                
      *COMMON COPYBOOKS                                                         
      *Screen Titles                                                            
       COPY COTTL01Y.                                                           
      *Credit Card Search Screen Layout                                         
      *COPY COCRDSL.                                                            
      *Credit Card List Screen Layout                                           
       COPY COCRDLI.                                                            
                                                                                
      *Current Date                                                             
       COPY CSDAT01Y.                                                           
      *Common Messages                                                          
       COPY CSMSG01Y.                                                           
      *Abend Variables                                                          
      *COPY CSMSG02Y.                                                           
      *Signed on user data                                                      
       COPY CSUSR01Y.                                                           
                                                                                
      *Dataset layouts                                                          
                                                                                
      *CARD RECORD LAYOUT                                                       
       COPY CVACT02Y.                                                           
                                                                                
       LINKAGE SECTION.                                                         
       01  DFHCOMMAREA.                                                         
         05  FILLER                                PIC X(1)                     
             OCCURS 1 TO 32767 TIMES DEPENDING ON EIBCALEN.                     
                                                                                
       PROCEDURE DIVISION.                                                      
       0000-MAIN.                                                               
                                                                                
           INITIALIZE CC-WORK-AREA                                              
