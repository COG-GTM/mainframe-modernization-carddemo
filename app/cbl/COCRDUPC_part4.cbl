                20 CCUP-OLD-CRDSTCD                PIC X(1).                    
                                                                                
          05 CCUP-NEW-DETAILS.                                                  
             10 CCUP-NEW-ACCTID                    PIC X(11).                   
             10 CCUP-NEW-CARDID                    PIC X(16).                   
             10 CCUP-NEW-CVV-CD                    PIC X(3).                    
             10 CCUP-NEW-CARDDATA.                                              
                20 CCUP-NEW-CRDNAME                PIC X(50).                   
                20 CCUP-NEW-EXPIRAION-DATE.                                     
                   25 CCUP-NEW-EXPYEAR             PIC X(4).                    
                   25 CCUP-NEW-EXPMON              PIC X(2).                    
                   25 CCUP-NEW-EXPDAY              PIC X(2).                    
                20 CCUP-NEW-CRDSTCD                PIC X(1).                    
          05 CARD-UPDATE-RECORD.                                                
             10 CARD-UPDATE-NUM                   PIC X(16).                    
             10 CARD-UPDATE-ACCT-ID               PIC 9(11).                    
             10 CARD-UPDATE-CVV-CD                PIC 9(03).                    
             10 CARD-UPDATE-EMBOSSED-NAME         PIC X(50).                    
             10 CARD-UPDATE-EXPIRAION-DATE        PIC X(10).                    
             10 CARD-UPDATE-ACTIVE-STATUS         PIC X(01).                    
             10 FILLER                            PIC X(59).                    
                                                                                
                                                                                
       01  WS-COMMAREA                             PIC X(2000).                 
                                                                                
      *IBM SUPPLIED COPYBOOKS                                                   
       COPY DFHBMSCA.                                                           
       COPY DFHAID.                                                             
                                                                                
      *COMMON COPYBOOKS                                                         
      *Screen Titles                                                            
       COPY COTTL01Y.                                                           
      *Credit Card Update Screen Layout                                         
       COPY COCRDUP.                                                            
                                                                                
      *Current Date                                                             
       COPY CSDAT01Y.                                                           
                                                                                
      *Common Messages                                                          
       COPY CSMSG01Y.                                                           
                                                                                
      *Abend Variables                                                          
       COPY CSMSG02Y.                                                           
                                                                                
      *Signed on user data                                                      
       COPY CSUSR01Y.                                                           
                                                                                
      *Dataset layouts                                                          
      *ACCOUNT RECORD LAYOUT                                                    
      *COPY CVACT01Y.                                                           
                                                                                
      *CARD RECORD LAYOUT                                                       
       COPY CVACT02Y.                                                           
                                                                                
      *CARD XREF LAYOUT                                                         
      *COPY CVACT03Y.                                                           
                                                                                
      *CUSTOMER LAYOUT                                                          
       COPY CVCUS01Y.                                                           
                                                                                
       LINKAGE SECTION.                                                         
       01  DFHCOMMAREA.                                                         
         05  FILLER                                PIC X(1)                     
             OCCURS 1 TO 32767 TIMES DEPENDING ON EIBCALEN.                     
                                                                                
       PROCEDURE DIVISION.                                                      
       0000-MAIN.                                                               
                                                                                
                                                                                
           EXEC CICS HANDLE ABEND                                               
                     LABEL(ABEND-ROUTINE)                                       
           END-EXEC                                                             
                                                                                
           INITIALIZE CC-WORK-AREA                                              
                      WS-MISC-STORAGE                                           
                      WS-COMMAREA                                               
      *****************************************************************         
      * Store our context                                                       
      *****************************************************************         
           MOVE LIT-THISTRANID       TO WS-TRANID                               
      *****************************************************************         
      * Ensure error message is cleared                               *         
      *****************************************************************         
           SET WS-RETURN-MSG-OFF  TO TRUE                                       
      *****************************************************************         
      * Store passed data if  any                *                              
      *****************************************************************         
           IF EIBCALEN IS EQUAL TO 0                                            
               OR (CDEMO-FROM-PROGRAM = LIT-MENUPGM                             
               AND NOT CDEMO-PGM-REENTER)                                       
              INITIALIZE CARDDEMO-COMMAREA                                      
                         WS-THIS-PROGCOMMAREA                                   
              SET CDEMO-PGM-ENTER TO TRUE                                       
              SET CCUP-DETAILS-NOT-FETCHED TO TRUE                              
           ELSE                                                                 
              MOVE DFHCOMMAREA (1:LENGTH OF CARDDEMO-COMMAREA)  TO              
                                CARDDEMO-COMMAREA                               
              MOVE DFHCOMMAREA(LENGTH OF CARDDEMO-COMMAREA + 1:                 
                               LENGTH OF WS-THIS-PROGCOMMAREA ) TO              
                                WS-THIS-PROGCOMMAREA                            
