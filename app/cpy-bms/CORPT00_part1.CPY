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
       01  CORPT0AI.
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
           02  MONTHLYL    COMP  PIC  S9(4).
           02  MONTHLYF    PICTURE X.
           02  FILLER REDEFINES MONTHLYF.
             03 MONTHLYA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  MONTHLYI  PIC X(1).
           02  YEARLYL    COMP  PIC  S9(4).
           02  YEARLYF    PICTURE X.
           02  FILLER REDEFINES YEARLYF.
             03 YEARLYA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  YEARLYI  PIC X(1).
           02  CUSTOML    COMP  PIC  S9(4).
           02  CUSTOMF    PICTURE X.
           02  FILLER REDEFINES CUSTOMF.
             03 CUSTOMA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  CUSTOMI  PIC X(1).
           02  SDTMML    COMP  PIC  S9(4).
           02  SDTMMF    PICTURE X.
           02  FILLER REDEFINES SDTMMF.
             03 SDTMMA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  SDTMMI  PIC X(2).
           02  SDTDDL    COMP  PIC  S9(4).
           02  SDTDDF    PICTURE X.
           02  FILLER REDEFINES SDTDDF.
             03 SDTDDA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  SDTDDI  PIC X(2).
           02  SDTYYYYL    COMP  PIC  S9(4).
           02  SDTYYYYF    PICTURE X.
           02  FILLER REDEFINES SDTYYYYF.
             03 SDTYYYYA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  SDTYYYYI  PIC X(4).
           02  EDTMML    COMP  PIC  S9(4).
           02  EDTMMF    PICTURE X.
           02  FILLER REDEFINES EDTMMF.
             03 EDTMMA    PICTURE X.
           02  FILLER   PICTURE X(4).
           02  EDTMMI  PIC X(2).
           02  EDTDDL    COMP  PIC  S9(4).
           02  EDTDDF    PICTURE X.
           02  FILLER REDEFINES EDTDDF.
             03 EDTDDA    PICTURE X.
