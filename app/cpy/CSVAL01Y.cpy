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
      * Common User ID Validation Working Storage Area
      ******************************************************************
       01 CS-USERID-VALIDATION.
         05 CS-CHK-USERID           PIC X(8).
         05 CS-CHK-RESULT-FLAG      PIC X VALUE 'N'.
            88 CS-USERID-VALID             VALUE 'Y'.
            88 CS-USERID-INVALID           VALUE 'N'.
         05 CS-CHAR                 PIC X.
         05 CS-IDX                  PIC 99.
      *
      * Ver: CardDemo_v1.0-15-g27d6c6f-68 Date: 2022-07-19 23:15:59 CDT
      *
