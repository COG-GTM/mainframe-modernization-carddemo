//MIGRCARD JOB 'MIGRATE CARD NUM 16 TO 17 DIGITS',CLASS=A,MSGCLASS=0,
//  NOTIFY=&SYSUID
//******************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//*
//* Licensed under the Apache License, Version 2.0 (the "License").
//* You may not use this file except in compliance with the License.
//* You may obtain a copy of the License at
//*
//*    http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing,
//* software distributed under the License is distributed on an
//* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
//* either express or implied. See the License for the specific
//* language governing permissions and limitations under the License
//******************************************************************
//*
//* MBA-1764: VSAM File / Data Migration
//* Migrates VSAM KSDS files from 16-digit to 17-digit card numbers
//* by prepending a leading zero to all card-number fields.
//*
//* Datasets migrated:
//*   - CARDDATA  (CARD-NUM at pos 1-16)
//*   - CARDXREF  (XREF-CARD-NUM at pos 1-16)
//*   - TRANSACT  (TRAN-CARD-NUM at pos 263-278)
//*   - DALYTRAN  (DALYTRAN-CARD-NUM at pos 263-278)
//*
//* Record lengths are preserved; FILLER fields shrink by 1 byte
//* to absorb the extra digit.
//*
//******************************************************************
//*
//*=================================================================
//*  STEP 01: CLOSE VSAM FILES IN CICS REGION
//*=================================================================
//CLCIFIL EXEC PGM=SDSF
//ISFOUT DD SYSOUT=*
//CMDOUT DD SYSOUT=*
//ISFIN  DD *
 /F CICSAWSA,'CEMT SET FIL(CARDDAT ) CLO'
 /F CICSAWSA,'CEMT SET FIL(CARDAIX ) CLO'
 /F CICSAWSA,'CEMT SET FIL(CARDXRF ) CLO'
 /F CICSAWSA,'CEMT SET FIL(TRANSACT) CLO'
 /F CICSAWSA,'CEMT SET FIL(CXACAIX ) CLO'
/*
//*
//*=================================================================
//*  STEP 02: UNLOAD CARDDATA VSAM KSDS TO SEQUENTIAL
//*=================================================================
//STEP02 EXEC PGM=IDCAMS
//SYSPRINT DD  SYSOUT=*
//CARDIN   DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS
//CARDOUT  DD  DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,
//         DCB=(LRECL=150,RECFM=FB,BLKSIZE=0),
//         SPACE=(CYL,(1,1),RLSE),
//         DSN=AWS.M2.CARDDEMO.CARDDATA.UNLOAD
//SYSIN    DD  *
   REPRO INFILE(CARDIN) OUTFILE(CARDOUT)
/*
//*
//*=================================================================
//*  STEP 03: UNLOAD CARDXREF VSAM KSDS TO SEQUENTIAL
//*=================================================================
//STEP03 EXEC PGM=IDCAMS
//SYSPRINT DD  SYSOUT=*
//XREFIN   DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS
//XREFOUT  DD  DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,
//         DCB=(LRECL=50,RECFM=FB,BLKSIZE=0),
//         SPACE=(CYL,(1,1),RLSE),
//         DSN=AWS.M2.CARDDEMO.CARDXREF.UNLOAD
//SYSIN    DD  *
   REPRO INFILE(XREFIN) OUTFILE(XREFOUT)
/*
//*
//*=================================================================
//*  STEP 04: UNLOAD TRANSACT VSAM KSDS TO SEQUENTIAL
//*=================================================================
//STEP04 EXEC PGM=IDCAMS
//SYSPRINT DD  SYSOUT=*
//TRANIN   DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS
//TRANOUT  DD  DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,
//         DCB=(LRECL=350,RECFM=FB,BLKSIZE=0),
//         SPACE=(CYL,(1,1),RLSE),
//         DSN=AWS.M2.CARDDEMO.TRANSACT.UNLOAD
//SYSIN    DD  *
   REPRO INFILE(TRANIN) OUTFILE(TRANOUT)
/*
//*
//*=================================================================
//*  STEP 05: REFORMAT CARDDATA - PAD CARD-NUM TO 17 DIGITS
//*           Old: CARD-NUM(1,16) + rest(17,75) + FILLER(92,59)
//*           New: '0'+CARD-NUM(1,17) + rest(17,75) + FILLER(92,58)
//*=================================================================
//STEP05 EXEC PGM=SORT
//SORTIN   DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.CARDDATA.UNLOAD
//SORTOUT  DD  DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,
//         DCB=(LRECL=150,RECFM=FB,BLKSIZE=0),
//         SPACE=(CYL,(1,1),RLSE),
//         DSN=AWS.M2.CARDDEMO.CARDDATA.MIGRATED
//SYSOUT   DD  SYSOUT=*
//SYSIN    DD  *
 SORT FIELDS=COPY
 OUTREC FIELDS=(C'0',1,91,92,58)
/*
//*
//*=================================================================
//*  STEP 06: REFORMAT CARDXREF - PAD XREF-CARD-NUM TO 17 DIGITS
//*           Old: XREF-CARD-NUM(1,16) + rest(17,20) + FILLER(37,14)
//*           New: '0'+XREF-CARD-NUM(1,17) + rest(17,20) + FILLER(37,13)
//*=================================================================
//STEP06 EXEC PGM=SORT
//SORTIN   DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.CARDXREF.UNLOAD
//SORTOUT  DD  DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,
//         DCB=(LRECL=50,RECFM=FB,BLKSIZE=0),
//         SPACE=(CYL,(1,1),RLSE),
//         DSN=AWS.M2.CARDDEMO.CARDXREF.MIGRATED
//SYSOUT   DD  SYSOUT=*
//SYSIN    DD  *
 SORT FIELDS=COPY
 OUTREC FIELDS=(C'0',1,36,37,13)
/*
//*
//*=================================================================
//*  STEP 07: REFORMAT TRANSACT - PAD TRAN-CARD-NUM TO 17 DIGITS
//*           TRAN-CARD-NUM is at position 263 (length 16)
//*           Old: pre(1,262) + TRAN-CARD-NUM(263,16) + post(279,52)
//*                + FILLER(331,20)
//*           New: pre(1,262) + '0'+TRAN-CARD-NUM(263,17) +
//*                post(279,52) + FILLER(331,19)
//*=================================================================
//STEP07 EXEC PGM=SORT
//SORTIN   DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.TRANSACT.UNLOAD
//SORTOUT  DD  DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,
//         DCB=(LRECL=350,RECFM=FB,BLKSIZE=0),
//         SPACE=(CYL,(1,1),RLSE),
//         DSN=AWS.M2.CARDDEMO.TRANSACT.MIGRATED
//SYSOUT   DD  SYSOUT=*
//SYSIN    DD  *
 SORT FIELDS=COPY
 OUTREC FIELDS=(1,262,C'0',263,16,279,52,331,19)
/*
//*
//*=================================================================
//*  STEP 08: DELETE OLD VSAM CLUSTERS AND ALTERNATE INDEXES
//*=================================================================
//STEP08 EXEC PGM=IDCAMS
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   DELETE AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS -
          CLUSTER
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX -
          ALTERNATEINDEX
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS -
          CLUSTER
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX -
          ALTERNATEINDEX
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS -
          CLUSTER
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX -
          ALTERNATEINDEX
   IF MAXCC LE 08 THEN SET MAXCC = 0
/*
//*
//*=================================================================
//*  STEP 09: REDEFINE CARDDATA VSAM KSDS WITH KEYS(17 0)
//*=================================================================
//STEP09 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   DEFINE CLUSTER (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) -
          CYLINDERS(1 5) -
          VOLUMES(AWSHJ1 -
          ) -
          KEYS(17 0) -
          RECORDSIZE(150 150) -
          SHAREOPTIONS(2 3) -
          ERASE -
          INDEXED -
          ) -
          DATA (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS.DATA) -
          ) -
          INDEX (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS.INDEX) -
          )
/*
//*
//*=================================================================
//*  STEP 10: REDEFINE CARDXREF VSAM KSDS WITH KEYS(17 0)
//*=================================================================
//STEP10 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   DEFINE CLUSTER (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS) -
          CYLINDERS(1 5) -
          VOLUMES(AWSHJ1 -
          ) -
          KEYS(17 0) -
          RECORDSIZE(50 50) -
          SHAREOPTIONS(2 3) -
          ERASE -
          INDEXED -
          ) -
          DATA (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS.DATA) -
          ) -
          INDEX (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS.INDEX) -
          )
/*
//*
//*=================================================================
//*  STEP 11: REDEFINE TRANSACT VSAM KSDS (KEY STAYS 16 0)
//*           Primary key is TRAN-ID (transaction counter), not a
//*           card number, so key length is unchanged.
//*=================================================================
//STEP11 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   DEFINE CLUSTER (NAME(AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS) -
          CYLINDERS(1 5) -
          VOLUMES(AWSHJ1 -
          ) -
          KEYS(16 0) -
          RECORDSIZE(350 350) -
          SHAREOPTIONS(2 3) -
          ERASE -
          INDEXED -
          ) -
          DATA (NAME(AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS.DATA) -
          ) -
          INDEX (NAME(AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS.INDEX) -
          )
/*
//*
//*=================================================================
//*  STEP 12: RELOAD CARDDATA FROM MIGRATED SEQUENTIAL FILE
//*=================================================================
//STEP12 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//CARDDATA DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.CARDDATA.MIGRATED
//CARDVSAM DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS
//SYSIN    DD  *
   REPRO INFILE(CARDDATA) OUTFILE(CARDVSAM)
/*
//*
//*=================================================================
//*  STEP 13: RELOAD CARDXREF FROM MIGRATED SEQUENTIAL FILE
//*=================================================================
//STEP13 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//XREFDATA DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.CARDXREF.MIGRATED
//XREFVSAM DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS
//SYSIN    DD  *
   REPRO INFILE(XREFDATA) OUTFILE(XREFVSAM)
/*
//*
//*=================================================================
//*  STEP 14: RELOAD TRANSACT FROM MIGRATED SEQUENTIAL FILE
//*=================================================================
//STEP14 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//TRANSACT DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.TRANSACT.MIGRATED
//TRANVSAM DD  DISP=SHR,
//         DSN=AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS
//SYSIN    DD  *
   REPRO INFILE(TRANSACT) OUTFILE(TRANVSAM)
/*
//*
//*=================================================================
//*  STEP 15: REBUILD CARDDATA ALTERNATE INDEX (ACCT-ID AT OFFSET 17)
//*=================================================================
//STEP15 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   DEFINE ALTERNATEINDEX (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX)-
   RELATE(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS)                    -
   KEYS(11 17)                                                   -
   NONUNIQUEKEY                                                  -
   UPGRADE                                                       -
   RECORDSIZE(150,150)                                           -
   VOLUMES(AWSHJ1)                                               -
   CYLINDERS(5,1))                                               -
   DATA (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.DATA))           -
   INDEX (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.INDEX))
/*
//STEP15P EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
  DEFINE PATH                                           -
   (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH)        -
    PATHENTRY(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX))
/*
//STEP15B EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   BLDINDEX                                                      -
   INDATASET(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS)                 -
   OUTDATASET(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX)
/*
//*
//*=================================================================
//*  STEP 16: REBUILD CARDXREF ALTERNATE INDEX (ACCT-ID AT OFFSET 26)
//*=================================================================
//STEP16 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   DEFINE ALTERNATEINDEX (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX)-
   RELATE(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS)                    -
   KEYS(11,26)                                                   -
   NONUNIQUEKEY                                                  -
   UPGRADE                                                       -
   RECORDSIZE(50,50)                                             -
   FREESPACE(10,20)                                              -
   VOLUMES(AWSHJ1)                                               -
   CYLINDERS(5,1))                                               -
   DATA (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.DATA))           -
   INDEX (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.INDEX))
/*
//STEP16P EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
  DEFINE PATH                                           -
   (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH)        -
    PATHENTRY(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX))
/*
//STEP16B EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   BLDINDEX                                                      -
   INDATASET(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS)                 -
   OUTDATASET(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX)
/*
//*
//*=================================================================
//*  STEP 17: REBUILD TRANSACT ALTERNATE INDEX (PROC-TS AT OFFSET 305)
//*=================================================================
//STEP17 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   DEFINE ALTERNATEINDEX (NAME(AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX)-
   RELATE(AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS)                    -
   KEYS(26 305)                                                  -
   NONUNIQUEKEY                                                  -
   UPGRADE                                                       -
   RECORDSIZE(350,350)                                           -
   VOLUMES(AWSHJ1)                                               -
   CYLINDERS(5,1))                                               -
   DATA (NAME(AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX.DATA))           -
   INDEX (NAME(AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX.INDEX))
/*
//STEP17P EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
  DEFINE PATH                                           -
   (NAME(AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX.PATH)        -
    PATHENTRY(AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX))
/*
//STEP17B EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   BLDINDEX                                                      -
   INDATASET(AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS)                 -
   OUTDATASET(AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX)
/*
//*
//*=================================================================
//*  STEP 18: REOPEN VSAM FILES IN CICS REGION
//*=================================================================
//OPCIFIL EXEC PGM=SDSF
//ISFOUT DD SYSOUT=*
//CMDOUT DD SYSOUT=*
//ISFIN  DD *
 /F CICSAWSA,'CEMT SET FIL(CARDDAT ) OPE'
 /F CICSAWSA,'CEMT SET FIL(CARDAIX ) OPE'
 /F CICSAWSA,'CEMT SET FIL(CARDXRF ) OPE'
 /F CICSAWSA,'CEMT SET FIL(TRANSACT) OPE'
 /F CICSAWSA,'CEMT SET FIL(CXACAIX ) OPE'
/*
//*
//*=================================================================
//*  STEP 19: CLEAN UP UNLOAD AND MIGRATED SEQUENTIAL FILES
//*=================================================================
//STEP19 EXEC PGM=IDCAMS,COND=(4,LT)
//SYSPRINT DD  SYSOUT=*
//SYSIN    DD  *
   DELETE AWS.M2.CARDDEMO.CARDDATA.UNLOAD
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.CARDXREF.UNLOAD
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.TRANSACT.UNLOAD
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.CARDDATA.MIGRATED
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.CARDXREF.MIGRATED
   IF MAXCC LE 08 THEN SET MAXCC = 0
   DELETE AWS.M2.CARDDEMO.TRANSACT.MIGRATED
   IF MAXCC LE 08 THEN SET MAXCC = 0
/*
//
