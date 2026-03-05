      *****************************************************************         
      * Program:     COCRDUPC.CBL                                     *         
      * Layer:       Business logic                                   *         
      * Function:    Accept and process credit card detail request    *         
      ******************************************************************
      * Copyright Amazon.com, Inc. or its affiliates.                   
      * All Rights Reserved.                                            
      *                                                                 
      * Licensed under the Apache License, Version 2.0 (the "License"). 
      * You may not use this file except in compliance with the License.
      * You may obtain a copy of the License at                         
      *                                                                 
      *    http://www.apache.org/licenses/LICENSE-2.0                   
      *                                                                 
      * Unless required by applicable law or agreed to in writing,      
      * software distributed under the License is distributed on an     
      * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,    
      * either express or implied. See the License for the specific     
      * language governing permissions and limitations under the License
      ******************************************************************        
                                                                                
       IDENTIFICATION DIVISION.                                                 
       PROGRAM-ID.                                                              
           COCRDUPC.                                                            
       DATE-WRITTEN.                                                            
           April 2022.                                                          
       DATE-COMPILED.                                                           
           Today.                                                               
                                                                                
       ENVIRONMENT DIVISION.                                                    
       INPUT-OUTPUT SECTION.                                                    
                                                                                
       DATA DIVISION.                                                           
                                                                                
       WORKING-STORAGE SECTION.                                                 
       01  WS-MISC-STORAGE.                                                     
      ******************************************************************        
      * General CICS related                                                    
      ******************************************************************        
         05 WS-CICS-PROCESSNG-VARS.                                             
            07 WS-RESP-CD                          PIC S9(09) COMP              
                                                   VALUE ZEROS.                 
            07 WS-REAS-CD                          PIC S9(09) COMP              
                                                   VALUE ZEROS.                 
            07 WS-TRANID                           PIC X(4)                     
                                                   VALUE SPACES.                
            07 WS-UCTRANS                          PIC X(4)                     
                                                   VALUE SPACES.                
      ******************************************************************        
      *      Input edits                                                        
      ******************************************************************        
                                                                                
         05  WS-INPUT-FLAG                         PIC X(1).                    
           88  INPUT-OK                            VALUE '0'.                   
           88  INPUT-ERROR                         VALUE '1'.                   
           88  INPUT-PENDING                       VALUE LOW-VALUES.            
         05  WS-EDIT-ACCT-FLAG                     PIC X(1).                    
           88  FLG-ACCTFILTER-NOT-OK               VALUE '0'.                   
           88  FLG-ACCTFILTER-ISVALID              VALUE '1'.                   
           88  FLG-ACCTFILTER-BLANK                VALUE ' '.                   
         05  WS-EDIT-CARD-FLAG                     PIC X(1).                    
           88  FLG-CARDFILTER-NOT-OK               VALUE '0'.                   
           88  FLG-CARDFILTER-ISVALID             VALUE '1'.                    
           88  FLG-CARDFILTER-BLANK                VALUE ' '.                   
         05  WS-EDIT-CARDNAME-FLAG                 PIC X(1).                    
           88  FLG-CARDNAME-NOT-OK                 VALUE '0'.                   
           88  FLG-CARDNAME-ISVALID                VALUE '1'.                   
           88  FLG-CARDNAME-BLANK                  VALUE ' '.                   
         05  WS-EDIT-CARDSTATUS-FLAG              PIC X(1).                     
           88  FLG-CARDSTATUS-NOT-OK               VALUE '0'.                   
           88  FLG-CARDSTATUS-ISVALID              VALUE '1'.                   
           88  FLG-CARDSTATUS-BLANK                VALUE ' '.                   
         05  WS-EDIT-CARDEXPMON-FLAG              PIC X(1).                     
           88  FLG-CARDEXPMON-NOT-OK               VALUE '0'.                   
           88  FLG-CARDEXPMON-ISVALID              VALUE '1'.                   
           88  FLG-CARDEXPMON-BLANK                VALUE ' '.                   
         05  WS-EDIT-CARDEXPYEAR-FLAG             PIC X(1).                     
           88  FLG-CARDEXPYEAR-NOT-OK              VALUE '0'.                   
           88  FLG-CARDEXPYEAR-ISVALID             VALUE '1'.                   
           88  FLG-CARDEXPYEAR-BLANK               VALUE ' '.                   
         05  WS-RETURN-FLAG                        PIC X(1).                    
           88  WS-RETURN-FLAG-OFF                  VALUE LOW-VALUES.            
           88  WS-RETURN-FLAG-ON                   VALUE '1'.                   
         05  WS-PFK-FLAG                           PIC X(1).                    
           88  PFK-VALID                           VALUE '0'.                   
           88  PFK-INVALID                         VALUE '1'.                   
         05  CARD-NAME-CHECK                       PIC X(50)                    
                                                   VALUE LOW-VALUES.            
         05  FLG-YES-NO-CHECK                      PIC X(1)                     
                                                   VALUE 'N'.                   
           88 FLG-YES-NO-VALID                     VALUES 'Y', 'N'.             
         05  CARD-MONTH-CHECK                      PIC X(2).                    
         05  CARD-MONTH-CHECK-N REDEFINES                                       
             CARD-MONTH-CHECK                      PIC 9(2).                    
             88 VALID-MONTH                        VALUES 1 THRU 12.            
         05  CARD-YEAR-CHECK                      PIC X(4).                     
         05  CARD-YEAR-CHECK-N REDEFINES                                        
             CARD-YEAR-CHECK                      PIC 9(4).                     
             88 VALID-YEAR                        VALUES 1950 THRU 2099.        
      ******************************************************************        
