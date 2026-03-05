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
       01  COUSR0AI.
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
           02  USRIDINL    COMP  PIC  S9(4).
           02  USRIDINF    PICTURE X.
           02  FILLER REDEFINES USRIDINF.
             03 USRIDINA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  USRIDINI  PIC X(8).
           02  SEL0001L    COMP  PIC  S9(4).
           02  SEL0001F    PICTURE X.
           02  FILLER REDEFINES SEL0001F.
             03 SEL0001A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  SEL0001I  PIC X(1).
           02  USRID01L    COMP  PIC  S9(4).
           02  USRID01F    PICTURE X.
           02  FILLER REDEFINES USRID01F.
             03 USRID01A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  USRID01I  PIC X(8).
           02  FNAME01L    COMP  PIC  S9(4).
           02  FNAME01F    PICTURE X.
           02  FILLER REDEFINES FNAME01F.
             03 FNAME01A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  FNAME01I  PIC X(20).
           02  LNAME01L    COMP  PIC  S9(4).
           02  LNAME01F    PICTURE X.
           02  FILLER REDEFINES LNAME01F.
             03 LNAME01A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  LNAME01I  PIC X(20).
           02  UTYPE01L    COMP  PIC  S9(4).
           02  UTYPE01F    PICTURE X.
           02  FILLER REDEFINES UTYPE01F.
             03 UTYPE01A    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  UTYPE01I  PIC X(1).
           02  SEL0002L    COMP  PIC  S9(4).
           02  SEL0002F    PICTURE X.
           02  FILLER REDEFINES SEL0002F.
             03 SEL0002A    PICTURE X.
