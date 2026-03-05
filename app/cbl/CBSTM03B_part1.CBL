       IDENTIFICATION DIVISION.
       PROGRAM-ID.    CBSTM03B.
       AUTHOR.        AWS.
      ******************************************************************
      * Program     : CBSTM03B.CBL
      * Application : CardDemo
      * Type        : BATCH COBOL Subroutine
      * Function    : Does file processing related to Transact Report
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
      * This program is to called by the statement create program
      * It does file handling
      ******************************************************************
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TRNX-FILE ASSIGN TO TRNXFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-TRNXS-ID
                  FILE STATUS  IS TRNXFILE-STATUS.

           SELECT XREF-FILE ASSIGN TO   XREFFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-XREF-CARD-NUM
                  FILE STATUS  IS XREFFILE-STATUS.

           SELECT CUST-FILE ASSIGN TO CUSTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-CUST-ID
                  FILE STATUS  IS CUSTFILE-STATUS.

           SELECT ACCT-FILE ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-ACCT-ID
                  FILE STATUS  IS ACCTFILE-STATUS.

      *
       DATA DIVISION.
       FILE SECTION.
       FD  TRNX-FILE.
       01  FD-TRNXFILE-REC.
           05 FD-TRNXS-ID.
              10  FD-TRNX-CARD                  PIC X(16).
              10  FD-TRNX-ID                    PIC X(16).
           05 FD-ACCT-DATA                      PIC X(318).

       FD  XREF-FILE.
       01  FD-XREFFILE-REC.
           05 FD-XREF-CARD-NUM                  PIC X(16).
           05 FD-XREF-DATA                      PIC X(34).

       FD  CUST-FILE.
       01  FD-CUSTFILE-REC.
           05 FD-CUST-ID                        PIC X(09).
           05 FD-CUST-DATA                      PIC X(491).

       FD  ACCT-FILE.
       01  FD-ACCTFILE-REC.
           05 FD-ACCT-ID                        PIC 9(11).
           05 FD-ACCT-DATA                      PIC X(289).

       WORKING-STORAGE SECTION.

      *****************************************************************
       01  TRNXFILE-STATUS.
           05  TRNXFILE-STAT1      PIC X.
           05  TRNXFILE-STAT2      PIC X.

       01  XREFFILE-STATUS.
           05  XREFFILE-STAT1      PIC X.
           05  XREFFILE-STAT2      PIC X.

       01  CUSTFILE-STATUS.
           05  CUSTFILE-STAT1      PIC X.
           05  CUSTFILE-STAT2      PIC X.

       01  ACCTFILE-STATUS.
           05  ACCTFILE-STAT1      PIC X.
           05  ACCTFILE-STAT2      PIC X.

       LINKAGE SECTION.
       01  LK-M03B-AREA.
