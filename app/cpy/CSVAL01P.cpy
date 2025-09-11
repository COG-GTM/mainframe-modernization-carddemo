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
      * Common User ID Validation Paragraph
      * Expects CS-CHK-USERID to be set (preferably already UPPER-CASE)
      * Sets CS-USERID-VALID / CS-USERID-INVALID (88-level)
      ******************************************************************
      *----------------------------------------------------------------*
      *                      CS-VALIDATE-USERID
      *----------------------------------------------------------------*
       CS-VALIDATE-USERID.

           MOVE 'Y' TO CS-CHK-RESULT-FLAG
           PERFORM VARYING CS-IDX FROM 1 BY 1 UNTIL CS-IDX > 8
               MOVE CS-CHK-USERID(CS-IDX:1) TO CS-CHAR
               IF (CS-CHAR < '0')
                    OR (CS-CHAR > '9' AND CS-CHAR < 'A')
                    OR (CS-CHAR > 'Z')
                   MOVE 'N' TO CS-CHK-RESULT-FLAG
                   EXIT PERFORM
               END-IF
           END-PERFORM.

      *
      * Ver: CardDemo_v1.0-15-g27d6c6f-68 Date: 2022-07-19 23:15:59 CDT
      *
