      *****************************************************************         
      * Program:     COCRDLIC.CBL                                     *         
      * Layer:       Business logic                                   *         
      * Function:    List Credit Cards                                          
      *              a) All cards if no context passed and admin user           
      *              b) Only the ones associated with ACCT in COMMAREA          
      *                 if user is not admin                                    
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
           COCRDLIC.                                                            
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
      ******************************************************************        
      * Input edits                                                             
      ******************************************************************        
         05 WS-INPUT-FLAG                          PIC X(1).                    
           88  INPUT-OK                            VALUES '0'                   
                                                          ' '                   
                                                   LOW-VALUES.                  
           88  INPUT-ERROR                         VALUE '1'.                   
         05  WS-EDIT-ACCT-FLAG                     PIC X(1).                    
           88  FLG-ACCTFILTER-NOT-OK               VALUE '0'.                   
           88  FLG-ACCTFILTER-ISVALID             VALUE '1'.                    
           88  FLG-ACCTFILTER-BLANK                VALUE ' '.                   
         05  WS-EDIT-CARD-FLAG                     PIC X(1).                    
           88  FLG-CARDFILTER-NOT-OK               VALUE '0'.                   
           88  FLG-CARDFILTER-ISVALID             VALUE '1'.                    
           88  FLG-CARDFILTER-BLANK                VALUE ' '.                   
         05 WS-EDIT-SELECT-COUNTER                PIC S9(04)                    
                                                  USAGE COMP-3                  
                                                  VALUE 0.                      
         05 WS-EDIT-SELECT-FLAGS                  PIC X(7)                      
                                                  VALUE LOW-VALUES.             
         05 WS-EDIT-SELECT-ARRAY REDEFINES  WS-EDIT-SELECT-FLAGS.               
            10 WS-EDIT-SELECT                      PIC X(1)                     
                                                  OCCURS 7 TIMES.               
               88 SELECT-OK                        VALUES 'S', 'U'.             
               88 VIEW-REQUESTED-ON                VALUE 'S'.                   
               88 UPDATE-REQUESTED-ON              VALUE 'U'.                   
               88 SELECT-BLANK                     VALUES                       
                                                   ' ',                         
                                                   LOW-VALUES.                  
         05 WS-EDIT-SELECT-ERROR-FLAGS             PIC X(7).                    
         05 WS-EDIT-SELECT-ERROR-FLAGX     REDEFINES                            
            WS-EDIT-SELECT-ERROR-FLAGS.                                         
            10 WS-EDIT-SELECT-ERRORS OCCURS 7 TIMES.                            
               20 WS-ROW-CRDSELECT-ERROR          PIC X(1).                     
                  88 WS-ROW-SELECT-ERROR          VALUE '1'.                    
         05 WS-SUBSCRIPT-VARS.                                                  
            10 I                                  PIC S9(4) COMP                
                                                  VALUE 0.                      
            10 I-SELECTED                         PIC S9(4) COMP                
                                                  VALUE 0.                      
               88 DETAIL-WAS-REQUESTED            VALUES 1 THRU 7.              
      ******************************************************************        
      * Output edits                                                            
      ******************************************************************        
         05 CICS-OUTPUT-EDIT-VARS.                                              
           10  CARD-ACCT-ID-X                      PIC X(11).                   
           10  CARD-ACCT-ID-N REDEFINES CARD-ACCT-ID-X                          
