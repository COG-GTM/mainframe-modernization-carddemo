       IDENTIFICATION DIVISION.
       PROGRAM-ID.    CBSTM03A.
       AUTHOR.        AWS.
      ******************************************************************
      * Program     : CBSTM03A.CBL
      * Application : CardDemo
      * Type        : BATCH COBOL Program
      * Function    : Print Account Statements from Transaction data
      *               in two formats : 1/plain text and 2/HTML
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
      * This program is to create statement based on the data in
      * transaction file. The following features are excercised
      * to help create excercise modernization tooling
      ******************************************************************
      *  1. Mainframe Control block addressing
      *  2. Alter and GO TO statements
      *  3. COMP and COMP-3 variables
      *  4. 2 dimensional array
      *  5. Call to Subroutine
      ******************************************************************
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT STMT-FILE ASSIGN TO STMTFILE.
           SELECT HTML-FILE ASSIGN TO HTMLFILE.
      *
       DATA DIVISION.
       FILE SECTION.
       FD  STMT-FILE.
       01  FD-STMTFILE-REC         PIC X(80).
       FD  HTML-FILE.
       01  FD-HTMLFILE-REC         PIC X(100).

       WORKING-STORAGE SECTION.

       COPY COSTM01.

       COPY CVACT03Y.

       COPY CUSTREC.

       COPY CVACT01Y.

       01  COMP-VARIABLES          COMP.
           05  CR-CNT              PIC S9(4) VALUE 0.
           05  TR-CNT              PIC S9(4) VALUE 0.
           05  CR-JMP              PIC S9(4) VALUE 0.
           05  TR-JMP              PIC S9(4) VALUE 0.
       01  COMP3-VARIABLES         COMP-3.
           05  WS-TOTAL-AMT        PIC S9(9)V99 VALUE 0.
       01  MISC-VARIABLES.
           05  WS-FL-DD            PIC X(8) VALUE 'TRNXFILE'.
           05  WS-TRN-AMT          PIC S9(9)V99 VALUE 0.
           05  WS-SAVE-CARD VALUE SPACES PIC X(16).
           05  END-OF-FILE         PIC X(01) VALUE 'N'.
       01  WS-M03B-AREA.
           05  WS-M03B-DD          PIC X(08).
           05  WS-M03B-OPER        PIC X(01).
             88  M03B-OPEN       VALUE 'O'.
             88  M03B-CLOSE      VALUE 'C'.
             88  M03B-READ       VALUE 'R'.
             88  M03B-READ-K     VALUE 'K'.
             88  M03B-WRITE      VALUE 'W'.
             88  M03B-REWRITE    VALUE 'Z'.
           05  WS-M03B-RC          PIC X(02).
           05  WS-M03B-KEY         PIC X(25).
           05  WS-M03B-KEY-LN      PIC S9(4).
           05  WS-M03B-FLDT        PIC X(1000).

       01  STATEMENT-LINES.
           05  ST-LINE0.
               10  FILLER  VALUE ALL '*'                PIC X(31).
               10  FILLER  VALUE ALL 'START OF STATEMENT' PIC X(18).
               10  FILLER  VALUE ALL '*'                PIC X(31).
           05  ST-LINE1.
               10  ST-NAME                              PIC X(75).
               10  FILLER  VALUE SPACES                 PIC X(05).
           05  ST-LINE2.
               10  ST-ADD1                              PIC X(50).
               10  FILLER  VALUE SPACES                 PIC X(30).
           05  ST-LINE3.
               10  ST-ADD2                              PIC X(50).
               10  FILLER  VALUE SPACES                 PIC X(30).
           05  ST-LINE4.
               10  ST-ADD3                              PIC X(80).
