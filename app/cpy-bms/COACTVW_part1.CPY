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
       01  CACTVWAI.
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
           02  ACCTSIDL    COMP  PIC  S9(4).
           02  ACCTSIDF    PICTURE X.
           02  FILLER REDEFINES ACCTSIDF.
             03 ACCTSIDA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  ACCTSIDI  PIC 99999999999.
           02  ACSTTUSL    COMP  PIC  S9(4).
           02  ACSTTUSF    PICTURE X.
           02  FILLER REDEFINES ACSTTUSF.
             03 ACSTTUSA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  ACSTTUSI  PIC X(1).
           02  ADTOPENL    COMP  PIC  S9(4).
           02  ADTOPENF    PICTURE X.
           02  FILLER REDEFINES ADTOPENF.
             03 ADTOPENA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  ADTOPENI  PIC X(10).
           02  ACRDLIML    COMP  PIC  S9(4).
           02  ACRDLIMF    PICTURE X.
           02  FILLER REDEFINES ACRDLIMF.
             03 ACRDLIMA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  ACRDLIMI  PIC X(15).
           02  AEXPDTL    COMP  PIC  S9(4).
           02  AEXPDTF    PICTURE X.
           02  FILLER REDEFINES AEXPDTF.
             03 AEXPDTA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  AEXPDTI  PIC X(10).
           02  ACSHLIML    COMP  PIC  S9(4).
           02  ACSHLIMF    PICTURE X.
           02  FILLER REDEFINES ACSHLIMF.
             03 ACSHLIMA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  ACSHLIMI  PIC X(15).
           02  AREISDTL    COMP  PIC  S9(4).
           02  AREISDTF    PICTURE X.
           02  FILLER REDEFINES AREISDTF.
             03 AREISDTA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  AREISDTI  PIC X(10).
           02  ACURBALL    COMP  PIC  S9(4).
           02  ACURBALF    PICTURE X.
           02  FILLER REDEFINES ACURBALF.
             03 ACURBALA    PICTURE X.
