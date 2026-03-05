      ******************************************************************        
      * Program     : CORPT00C.CBL
      * Application : CardDemo
      * Type        : CICS COBOL Program
      * Function    : Print Transaction reports by submitting batch 
      *               job from online using extra partition TDQ.  
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
       PROGRAM-ID. CORPT00C.
       AUTHOR.     AWS.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.

       DATA DIVISION.
      *----------------------------------------------------------------*
      *                     WORKING STORAGE SECTION
      *----------------------------------------------------------------*
       WORKING-STORAGE SECTION.

       01 WS-VARIABLES.
         05 WS-PGMNAME                 PIC X(08) VALUE 'CORPT00C'.
         05 WS-TRANID                  PIC X(04) VALUE 'CR00'.
         05 WS-MESSAGE                 PIC X(80) VALUE SPACES.
         05 WS-TRANSACT-FILE             PIC X(08) VALUE 'TRANSACT'.
         05 WS-ERR-FLG                 PIC X(01) VALUE 'N'.
           88 ERR-FLG-ON                         VALUE 'Y'.
           88 ERR-FLG-OFF                        VALUE 'N'.
         05 WS-TRANSACT-EOF            PIC X(01) VALUE 'N'.
           88 TRANSACT-EOF                       VALUE 'Y'.
           88 TRANSACT-NOT-EOF                   VALUE 'N'.
         05 WS-SEND-ERASE-FLG          PIC X(01) VALUE 'Y'.
           88 SEND-ERASE-YES                     VALUE 'Y'.
           88 SEND-ERASE-NO                      VALUE 'N'.
         05 WS-END-LOOP                PIC X(01) VALUE 'N'.
           88 END-LOOP-YES                       VALUE 'Y'.
           88 END-LOOP-NO                        VALUE 'N'.

         05 WS-RESP-CD                 PIC S9(09) COMP VALUE ZEROS.
         05 WS-REAS-CD                 PIC S9(09) COMP VALUE ZEROS.
         05 WS-REC-COUNT               PIC S9(04) COMP VALUE ZEROS.
         05 WS-IDX                     PIC S9(04) COMP VALUE ZEROS.
         05 WS-REPORT-NAME             PIC X(10) VALUE SPACES.

         05 WS-START-DATE.
            10 WS-START-DATE-YYYY      PIC X(04) VALUE SPACES.
            10 FILLER                  PIC X(01) VALUE '-'.
            10 WS-START-DATE-MM        PIC X(02) VALUE SPACES.
            10 FILLER                  PIC X(01) VALUE '-'.
            10 WS-START-DATE-DD        PIC X(02) VALUE SPACES.
         05 WS-END-DATE.
            10 WS-END-DATE-YYYY        PIC X(04) VALUE SPACES.
            10 FILLER                  PIC X(01) VALUE '-'.
            10 WS-END-DATE-MM          PIC X(02) VALUE SPACES.
            10 FILLER                  PIC X(01) VALUE '-'.
            10 WS-END-DATE-DD          PIC X(02) VALUE SPACES.
         05 WS-DATE-FORMAT             PIC X(10) VALUE 'YYYY-MM-DD'.

         05 WS-NUM-99                  PIC 99   VALUE 0.
         05 WS-NUM-9999                PIC 9999 VALUE 0.

         05 WS-TRAN-AMT                PIC +99999999.99.
         05 WS-TRAN-DATE               PIC X(08) VALUE '00/00/00'.
         05 JCL-RECORD                 PIC X(80) VALUE ' '.

       01 JOB-DATA.
        02 JOB-DATA-1.
         05 FILLER                     PIC X(80) VALUE
         "//TRNRPT00 JOB 'TRAN REPORT',CLASS=A,MSGCLASS=0,".
         05 FILLER                     PIC X(80) VALUE
         "// NOTIFY=&SYSUID".
         05 FILLER                     PIC X(80) VALUE
         "//*".
         05 FILLER                     PIC X(80) VALUE
         "//JOBLIB JCLLIB ORDER=('AWS.M2.CARDDEMO.PROC')".
         05 FILLER                     PIC X(80) VALUE
         "//*".
         05 FILLER                     PIC X(80) VALUE
         "//STEP10 EXEC PROC=TRANREPT".
         05 FILLER                     PIC X(80) VALUE
         "//*".
         05 FILLER                     PIC X(80) VALUE
         "//STEP05R.SYMNAMES DD *".
         05 FILLER                     PIC X(80) VALUE
         "TRAN-CARD-NUM,263,16,ZD".
