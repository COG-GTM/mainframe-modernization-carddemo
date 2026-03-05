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
       01  COTRN0AI.
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
           02  PAGENUML    COMP  PIC  S9(4).
           02  PAGENUMF    PICTURE X.
           02  FILLER REDEFINES PAGENUMF.
             03 PAGENUMA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  PAGENUMI  PIC X(8).
           02  TRNIDINL    COMP  PIC  S9(4).
           02  TRNIDINF    PICTURE X.
           02  FILLER REDEFINES TRNIDINF.
             03 TRNIDINA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TRNIDINI  PIC X(16).
           02  SEL0001L    COMP  PIC  S9(4).
           02  SEL0001F    PICTURE X.
           02  FILLER REDEFINES SEL0001F.
             03 SEL0001A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  SEL0001I  PIC X(1).
           02  TRNID01L    COMP  PIC  S9(4).
           02  TRNID01F    PICTURE X.
           02  FILLER REDEFINES TRNID01F.
             03 TRNID01A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TRNID01I  PIC X(16).
           02  TDATE01L    COMP  PIC  S9(4).
           02  TDATE01F    PICTURE X.
           02  FILLER REDEFINES TDATE01F.
             03 TDATE01A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TDATE01I  PIC X(8).
           02  TDESC01L    COMP  PIC  S9(4).
           02  TDESC01F    PICTURE X.
           02  FILLER REDEFINES TDESC01F.
             03 TDESC01A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TDESC01I  PIC X(26).
           02  TAMT001L    COMP  PIC  S9(4).
           02  TAMT001F    PICTURE X.
           02  FILLER REDEFINES TAMT001F.
             03 TAMT001A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  TAMT001I  PIC X(12).
           02  SEL0002L    COMP  PIC  S9(4).
           02  SEL0002F    PICTURE X.
           02  FILLER REDEFINES SEL0002F.
             03 SEL0002A    PICTURE X.
