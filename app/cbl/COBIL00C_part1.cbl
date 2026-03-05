      ******************************************************************        
      * Program     : COBIL00C.CBL
      * Application : CardDemo
      * Type        : CICS COBOL Program
      * Function    : Bill Payment - Pay account balance in full and a
      *               tractionsaction for the online bill payment.
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
       PROGRAM-ID. COBIL00C.
       AUTHOR.     AWS.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.

       DATA DIVISION.
      *----------------------------------------------------------------*
      *                     WORKING STORAGE SECTION
      *----------------------------------------------------------------*
       WORKING-STORAGE SECTION.

       01 WS-VARIABLES.
         05 WS-PGMNAME                 PIC X(08) VALUE 'COBIL00C'.
         05 WS-TRANID                  PIC X(04) VALUE 'CB00'.
         05 WS-MESSAGE                 PIC X(80) VALUE SPACES.
         05 WS-TRANSACT-FILE           PIC X(08) VALUE 'TRANSACT'.
         05 WS-ACCTDAT-FILE            PIC X(08) VALUE 'ACCTDAT '.
         05 WS-CXACAIX-FILE            PIC X(08) VALUE 'CXACAIX '.
         05 WS-ERR-FLG                 PIC X(01) VALUE 'N'.
           88 ERR-FLG-ON                         VALUE 'Y'.
           88 ERR-FLG-OFF                        VALUE 'N'.
         05 WS-RESP-CD                 PIC S9(09) COMP VALUE ZEROS.
         05 WS-REAS-CD                 PIC S9(09) COMP VALUE ZEROS.
         05 WS-USR-MODIFIED            PIC X(01) VALUE 'N'.
           88 USR-MODIFIED-YES                   VALUE 'Y'.
           88 USR-MODIFIED-NO                    VALUE 'N'.
         05 WS-CONF-PAY-FLG            PIC X(01) VALUE 'N'.
           88 CONF-PAY-YES                       VALUE 'Y'.
           88 CONF-PAY-NO                        VALUE 'N'.

         05 WS-TRAN-AMT                PIC +99999999.99.
         05 WS-CURR-BAL                PIC +9999999999.99.
         05 WS-TRAN-ID-NUM             PIC 9(16) VALUE ZEROS.
         05 WS-TRAN-DATE               PIC X(08) VALUE '00/00/00'.
         05 WS-ABS-TIME                PIC S9(15) COMP-3 VALUE 0.
         05 WS-CUR-DATE-X10            PIC X(10) VALUE SPACES.
         05 WS-CUR-TIME-X08            PIC X(08) VALUE SPACES.

       COPY COCOM01Y.
          05 CDEMO-CB00-INFO.
             10 CDEMO-CB00-TRNID-FIRST     PIC X(16).
             10 CDEMO-CB00-TRNID-LAST      PIC X(16).
             10 CDEMO-CB00-PAGE-NUM        PIC 9(08).
             10 CDEMO-CB00-NEXT-PAGE-FLG   PIC X(01) VALUE 'N'.
                88 NEXT-PAGE-YES                     VALUE 'Y'.
                88 NEXT-PAGE-NO                      VALUE 'N'.
             10 CDEMO-CB00-TRN-SEL-FLG     PIC X(01).
             10 CDEMO-CB00-TRN-SELECTED    PIC X(16).

       COPY COBIL00.

       COPY COTTL01Y.
       COPY CSDAT01Y.
       COPY CSMSG01Y.

       COPY CVACT01Y.
       COPY CVACT03Y.
       COPY CVTRA05Y.

       COPY DFHAID.
       COPY DFHBMSCA.

      *----------------------------------------------------------------*
      *                        LINKAGE SECTION
      *----------------------------------------------------------------*
       LINKAGE SECTION.
       01  DFHCOMMAREA.
         05  LK-COMMAREA                           PIC X(01)
             OCCURS 1 TO 32767 TIMES DEPENDING ON EIBCALEN.

      *----------------------------------------------------------------*
      *                       PROCEDURE DIVISION
      *----------------------------------------------------------------*
       PROCEDURE DIVISION.
       MAIN-PARA.

