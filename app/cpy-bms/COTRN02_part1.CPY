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
       01  COTRN2AI.
           02  FILLER PIC X(12).
           02  TRNNAMEL    COMP  PIC  S9(4).
           02  TRNNAMEF    PICTURE X.
           02  FILLER REDEFINES TRNNAMEF.
             03 TRNNAMEA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TRNNAMEI  PIC X(4).
           02  TITLE01L    COMP  PIC  S9(4).
           02  TITLE01F    PICTURE X.
           02  FILLER REDEFINES TITLE01F.
             03 TITLE01A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TITLE01I  PIC X(40).
           02  CURDATEL    COMP  PIC  S9(4).
           02  CURDATEF    PICTURE X.
           02  FILLER REDEFINES CURDATEF.
             03 CURDATEA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  CURDATEI  PIC X(8).
           02  PGMNAMEL    COMP  PIC  S9(4).
           02  PGMNAMEF    PICTURE X.
           02  FILLER REDEFINES PGMNAMEF.
             03 PGMNAMEA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  PGMNAMEI  PIC X(8).
           02  TITLE02L    COMP  PIC  S9(4).
           02  TITLE02F    PICTURE X.
           02  FILLER REDEFINES TITLE02F.
             03 TITLE02A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TITLE02I  PIC X(40).
           02  CURTIMEL    COMP  PIC  S9(4).
           02  CURTIMEF    PICTURE X.
           02  FILLER REDEFINES CURTIMEF.
             03 CURTIMEA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  CURTIMEI  PIC X(8).
           02  ACTIDINL    COMP  PIC  S9(4).
           02  ACTIDINF    PICTURE X.
           02  FILLER REDEFINES ACTIDINF.
             03 ACTIDINA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  ACTIDINI  PIC X(11).
           02  CARDNINL    COMP  PIC  S9(4).
           02  CARDNINF    PICTURE X.
           02  FILLER REDEFINES CARDNINF.
             03 CARDNINA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  CARDNINI  PIC X(16).
           02  TTYPCDL    COMP  PIC  S9(4).
           02  TTYPCDF    PICTURE X.
           02  FILLER REDEFINES TTYPCDF.
             03 TTYPCDA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TTYPCDI  PIC X(2).
           02  TCATCDL    COMP  PIC  S9(4).
           02  TCATCDF    PICTURE X.
           02  FILLER REDEFINES TCATCDF.
             03 TCATCDA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TCATCDI  PIC X(4).
           02  TRNSRCL    COMP  PIC  S9(4).
           02  TRNSRCF    PICTURE X.
           02  FILLER REDEFINES TRNSRCF.
             03 TRNSRCA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TRNSRCI  PIC X(10).
           02  TDESCL    COMP  PIC  S9(4).
           02  TDESCF    PICTURE X.
           02  FILLER REDEFINES TDESCF.
             03 TDESCA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TDESCI  PIC X(60).
           02  TRNAMTL    COMP  PIC  S9(4).
           02  TRNAMTF    PICTURE X.
           02  FILLER REDEFINES TRNAMTF.
             03 TRNAMTA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TRNAMTI  PIC X(12).
           02  TORIGDTL    COMP  PIC  S9(4).
           02  TORIGDTF    PICTURE X.
           02  FILLER REDEFINES TORIGDTF.
             03 TORIGDTA    PICTURE X.
