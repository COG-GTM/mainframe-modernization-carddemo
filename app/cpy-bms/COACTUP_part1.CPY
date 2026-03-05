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
       01  CACTUPAI.
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
           02  ACCTSIDI  PIC X(11).
           02  ACSTTUSL    COMP  PIC  S9(4).
           02  ACSTTUSF    PICTURE X.
           02  FILLER REDEFINES ACSTTUSF.
             03 ACSTTUSA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  ACSTTUSI  PIC X(1).
           02  OPNYEARL    COMP  PIC  S9(4).
           02  OPNYEARF    PICTURE X.
           02  FILLER REDEFINES OPNYEARF.
             03 OPNYEARA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  OPNYEARI  PIC X(4).
           02  OPNMONL    COMP  PIC  S9(4).
           02  OPNMONF    PICTURE X.
           02  FILLER REDEFINES OPNMONF.
             03 OPNMONA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  OPNMONI  PIC X(2).
           02  OPNDAYL    COMP  PIC  S9(4).
           02  OPNDAYF    PICTURE X.
           02  FILLER REDEFINES OPNDAYF.
             03 OPNDAYA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  OPNDAYI  PIC X(2).
           02  ACRDLIML    COMP  PIC  S9(4).
           02  ACRDLIMF    PICTURE X.
           02  FILLER REDEFINES ACRDLIMF.
             03 ACRDLIMA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  ACRDLIMI  PIC X(15).
           02  EXPYEARL    COMP  PIC  S9(4).
           02  EXPYEARF    PICTURE X.
           02  FILLER REDEFINES EXPYEARF.
             03 EXPYEARA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  EXPYEARI  PIC X(4).
           02  EXPMONL    COMP  PIC  S9(4).
           02  EXPMONF    PICTURE X.
           02  FILLER REDEFINES EXPMONF.
             03 EXPMONA    PICTURE X.
