# Backup and Rollback Procedures - Phase 1 Analysis

## Document Information
- **Project**: CardDemo Mainframe Application
- **Repository**: COG-GTM/mainframe-modernization-carddemo
- **Analysis Date**: 2025-12-18
- **Purpose**: Define backup and rollback procedures for Account ID refactoring

## Executive Summary

This document provides comprehensive backup and rollback procedures for the Account ID refactoring project. These procedures ensure data safety during the migration from 11-digit numeric to 14-digit alphanumeric Account IDs and provide a clear path to restore the system to its pre-migration state if issues are encountered.

## 1. VSAM Files Requiring Backup

### 1.1 Primary Data Files

| File Name | Dataset Name | Record Length | Key Length | Key Offset | Priority |
|-----------|--------------|---------------|------------|------------|----------|
| ACCTDATA | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | 300 | 11 | 0 | Critical |
| CARDDATA | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | 150 | 16 | 0 | Critical |
| CARDXREF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | 50 | 16 | 0 | Critical |

### 1.2 Alternate Index Files

| File Name | Dataset Name | Base Cluster | Key Length | Key Offset |
|-----------|--------------|--------------|------------|------------|
| CARDAIX | AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX | CARDDATA | 11 | 16 |
| CXACAIX | AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX | CARDXREF | 11 | 25 |

### 1.3 Transaction Files (If Applicable)

| File Name | Dataset Name | Description |
|-----------|--------------|-------------|
| TRANCAT | AWS.M2.CARDDEMO.TRANCAT.VSAM.KSDS | Transaction category balances |
| DALYTRAN | AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS | Daily transaction file |

### 1.4 Source Code and Configuration

| Component | Location | Backup Method |
|-----------|----------|---------------|
| COBOL Programs | app/cbl/*.cbl | Git version control |
| Copybooks | app/cpy/*.cpy | Git version control |
| BMS Maps | app/bms/*.bms | Git version control |
| BMS Copybooks | app/cpy-bms/*.CPY | Git version control |
| JCL Scripts | app/jcl/*.jcl | Git version control |

## 2. Backup Location and Naming Conventions

### 2.1 Dataset Naming Convention

```
AWS.M2.CARDDEMO.BACKUP.{YYYYMMDD}.{HHMMSS}.{FILENAME}

Example:
AWS.M2.CARDDEMO.BACKUP.20251218.143000.ACCTDATA
AWS.M2.CARDDEMO.BACKUP.20251218.143000.CARDDATA
AWS.M2.CARDDEMO.BACKUP.20251218.143000.CARDXREF
```

### 2.2 Backup Storage Requirements

| File | Original Size | Backup Size (Est.) | Retention Period |
|------|---------------|-------------------|------------------|
| ACCTDATA | 15,000 bytes | 20,000 bytes | 90 days |
| CARDDATA | 7,500 bytes | 10,000 bytes | 90 days |
| CARDXREF | 2,500 bytes | 5,000 bytes | 90 days |
| **Total** | **25,000 bytes** | **35,000 bytes** | - |

### 2.3 Git Branch Naming Convention

```
backup/acct-id-refactor-pre-migration-{YYYYMMDD}

Example:
backup/acct-id-refactor-pre-migration-20251218
```

## 3. Backup Procedures

### 3.1 Pre-Migration Backup Checklist

| Step | Action | Verification | Sign-off |
|------|--------|--------------|----------|
| 1 | Stop all online CICS transactions | No active users | [ ] |
| 2 | Complete all running batch jobs | JES queue empty | [ ] |
| 3 | Close all VSAM files in CICS | CEMT SET FILE CLOSE | [ ] |
| 4 | Record current file statistics | LISTCAT output saved | [ ] |
| 5 | Execute backup JCL | RC=0 for all steps | [ ] |
| 6 | Verify backup file integrity | Compare record counts | [ ] |
| 7 | Create Git backup branch | Branch created | [ ] |
| 8 | Document backup completion | Log entry made | [ ] |

### 3.2 VSAM Backup JCL

```jcl
//BACKUP   JOB 'ACCT ID BACKUP',CLASS=A,MSGCLASS=0,
//         NOTIFY=&SYSUID
//*******************************************************************
//* ACCOUNT ID REFACTORING - PRE-MIGRATION BACKUP
//* DATE: &SYSDATE
//* TIME: &SYSTIME
//*******************************************************************
//*
//* STEP 1: CLOSE CICS FILES
//*******************************************************************
//CLSFILE  EXEC PGM=SDSF
//ISFOUT   DD   SYSOUT=*
//CMDOUT   DD   SYSOUT=*
//ISFIN    DD   *
 /F CICSAWSA,'CEMT SET FIL(ACCTDAT ) CLO'
 /F CICSAWSA,'CEMT SET FIL(CARDDAT ) CLO'
 /F CICSAWSA,'CEMT SET FIL(CARDAIX ) CLO'
 /F CICSAWSA,'CEMT SET FIL(CARDXREF) CLO'
 /F CICSAWSA,'CEMT SET FIL(CXACAIX ) CLO'
/*
//*******************************************************************
//* STEP 2: BACKUP ACCTDATA
//*******************************************************************
//BKACCT   EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//INFILE   DD   DSN=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS,DISP=SHR
//OUTFILE  DD   DSN=AWS.M2.CARDDEMO.BACKUP.&LYYMMDD..ACCTDATA,
//              DISP=(NEW,CATLG,DELETE),
//              SPACE=(CYL,(5,2)),
//              DCB=(RECFM=FB,LRECL=300,BLKSIZE=0)
//SYSIN    DD   *
   REPRO INFILE(INFILE) OUTFILE(OUTFILE)
/*
//*******************************************************************
//* STEP 3: BACKUP CARDDATA
//*******************************************************************
//BKCARD   EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//INFILE   DD   DSN=AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS,DISP=SHR
//OUTFILE  DD   DSN=AWS.M2.CARDDEMO.BACKUP.&LYYMMDD..CARDDATA,
//              DISP=(NEW,CATLG,DELETE),
//              SPACE=(CYL,(5,2)),
//              DCB=(RECFM=FB,LRECL=150,BLKSIZE=0)
//SYSIN    DD   *
   REPRO INFILE(INFILE) OUTFILE(OUTFILE)
/*
//*******************************************************************
//* STEP 4: BACKUP CARDXREF
//*******************************************************************
//BKXREF   EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//INFILE   DD   DSN=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS,DISP=SHR
//OUTFILE  DD   DSN=AWS.M2.CARDDEMO.BACKUP.&LYYMMDD..CARDXREF,
//              DISP=(NEW,CATLG,DELETE),
//              SPACE=(CYL,(5,2)),
//              DCB=(RECFM=FB,LRECL=50,BLKSIZE=0)
//SYSIN    DD   *
   REPRO INFILE(INFILE) OUTFILE(OUTFILE)
/*
//*******************************************************************
//* STEP 5: GENERATE LISTCAT FOR VERIFICATION
//*******************************************************************
//LISTCAT  EXEC PGM=IDCAMS
//SYSPRINT DD   DSN=AWS.M2.CARDDEMO.BACKUP.&LYYMMDD..LISTCAT,
//              DISP=(NEW,CATLG,DELETE),
//              SPACE=(TRK,(5,2)),
//              DCB=(RECFM=FBA,LRECL=133,BLKSIZE=0)
//SYSIN    DD   *
   LISTCAT ENTRIES(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) ALL
   LISTCAT ENTRIES(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) ALL
   LISTCAT ENTRIES(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS) ALL
/*
//*******************************************************************
//* STEP 6: VERIFY BACKUP RECORD COUNTS
//*******************************************************************
//VERIFY   EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   PRINT INFILE(ACCTBK) COUNT(1) CHARACTER
   PRINT INFILE(CARDBK) COUNT(1) CHARACTER
   PRINT INFILE(XREFBK) COUNT(1) CHARACTER
//ACCTBK   DD   DSN=AWS.M2.CARDDEMO.BACKUP.&LYYMMDD..ACCTDATA,DISP=SHR
//CARDBK   DD   DSN=AWS.M2.CARDDEMO.BACKUP.&LYYMMDD..CARDDATA,DISP=SHR
//XREFBK   DD   DSN=AWS.M2.CARDDEMO.BACKUP.&LYYMMDD..CARDXREF,DISP=SHR
/*
```

### 3.3 Git Backup Procedure

```bash
# Create backup branch before any code changes
git checkout main
git pull origin main
git checkout -b backup/acct-id-refactor-pre-migration-20251218
git push origin backup/acct-id-refactor-pre-migration-20251218

# Tag the backup point
git tag -a v1.0-pre-acctid-refactor -m "Pre-Account ID refactoring backup"
git push origin v1.0-pre-acctid-refactor
```

### 3.4 Backup Verification Steps

| Verification | Command/Method | Expected Result |
|--------------|----------------|-----------------|
| ACCTDATA record count | IDCAMS PRINT COUNT | 50 records |
| CARDDATA record count | IDCAMS PRINT COUNT | ~50 records |
| CARDXREF record count | IDCAMS PRINT COUNT | ~50 records |
| Backup file exists | LISTCAT | Catalog entry present |
| Backup file readable | IDCAMS PRINT | First record displays |
| Git branch exists | git branch -a | Branch listed |
| Git tag exists | git tag -l | Tag listed |

## 4. Rollback Procedures

### 4.1 Rollback Decision Criteria

Initiate rollback if ANY of the following conditions occur:

| Condition | Severity | Action |
|-----------|----------|--------|
| Data conversion errors detected | Critical | Immediate rollback |
| Record count mismatch after migration | Critical | Immediate rollback |
| Application startup failures | Critical | Immediate rollback |
| Cross-reference integrity failures | Critical | Immediate rollback |
| More than 5% of transactions failing | High | Evaluate and rollback |
| Performance degradation > 50% | High | Evaluate and rollback |
| User-reported data inconsistencies | Medium | Investigate, then decide |

### 4.2 Rollback Checklist

| Step | Action | Verification | Sign-off |
|------|--------|--------------|----------|
| 1 | Stop all CICS transactions | No active users | [ ] |
| 2 | Stop all batch jobs | JES queue empty | [ ] |
| 3 | Close all VSAM files | CEMT SET FILE CLOSE | [ ] |
| 4 | Delete migrated VSAM files | IDCAMS DELETE | [ ] |
| 5 | Restore original VSAM definitions | IDCAMS DEFINE | [ ] |
| 6 | Reload data from backup | IDCAMS REPRO | [ ] |
| 7 | Rebuild alternate indexes | IDCAMS BLDINDEX | [ ] |
| 8 | Revert code changes | Git checkout | [ ] |
| 9 | Recompile programs | Compile JCL | [ ] |
| 10 | Restart CICS region | CICS startup | [ ] |
| 11 | Verify application functionality | Test transactions | [ ] |
| 12 | Document rollback completion | Log entry | [ ] |

### 4.3 VSAM Rollback JCL

```jcl
//ROLLBACK JOB 'ACCT ID ROLLBACK',CLASS=A,MSGCLASS=0,
//         NOTIFY=&SYSUID
//*******************************************************************
//* ACCOUNT ID REFACTORING - ROLLBACK PROCEDURE
//* RESTORE FROM BACKUP DATE: &BKDATE
//*******************************************************************
//*
//* STEP 1: CLOSE CICS FILES
//*******************************************************************
//CLSFILE  EXEC PGM=SDSF
//ISFOUT   DD   SYSOUT=*
//CMDOUT   DD   SYSOUT=*
//ISFIN    DD   *
 /F CICSAWSA,'CEMT SET FIL(ACCTDAT ) CLO'
 /F CICSAWSA,'CEMT SET FIL(CARDDAT ) CLO'
 /F CICSAWSA,'CEMT SET FIL(CARDAIX ) CLO'
 /F CICSAWSA,'CEMT SET FIL(CARDXREF) CLO'
 /F CICSAWSA,'CEMT SET FIL(CXACAIX ) CLO'
/*
//*******************************************************************
//* STEP 2: DELETE MIGRATED ACCTDATA
//*******************************************************************
//DELACCT  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DELETE AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS CLUSTER
   IF MAXCC LE 08 THEN SET MAXCC = 0
/*
//*******************************************************************
//* STEP 3: REDEFINE ORIGINAL ACCTDATA (11-BYTE KEY)
//*******************************************************************
//DEFACCT  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DEFINE CLUSTER (NAME(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) -
          CYLINDERS(1 5) -
          VOLUMES(AWSHJ1) -
          KEYS(11 0) -
          RECORDSIZE(300 300) -
          SHAREOPTIONS(2 3) -
          ERASE -
          INDEXED) -
          DATA (NAME(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS.DATA)) -
          INDEX (NAME(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS.INDEX))
/*
//*******************************************************************
//* STEP 4: RESTORE ACCTDATA FROM BACKUP
//*******************************************************************
//RSTACCT  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//INFILE   DD   DSN=AWS.M2.CARDDEMO.BACKUP.&BKDATE..ACCTDATA,DISP=SHR
//OUTFILE  DD   DSN=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS,DISP=SHR
//SYSIN    DD   *
   REPRO INFILE(INFILE) OUTFILE(OUTFILE)
/*
//*******************************************************************
//* STEP 5: DELETE MIGRATED CARDDATA
//*******************************************************************
//DELCARD  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DELETE AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS CLUSTER
   DELETE AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX ALTERNATEINDEX
   IF MAXCC LE 08 THEN SET MAXCC = 0
/*
//*******************************************************************
//* STEP 6: REDEFINE ORIGINAL CARDDATA
//*******************************************************************
//DEFCARD  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DEFINE CLUSTER (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) -
          CYLINDERS(1 5) -
          VOLUMES(AWSHJ1) -
          KEYS(16 0) -
          RECORDSIZE(150 150) -
          SHAREOPTIONS(2 3) -
          ERASE -
          INDEXED) -
          DATA (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS.DATA)) -
          INDEX (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS.INDEX))
/*
//*******************************************************************
//* STEP 7: RESTORE CARDDATA FROM BACKUP
//*******************************************************************
//RSTCARD  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//INFILE   DD   DSN=AWS.M2.CARDDEMO.BACKUP.&BKDATE..CARDDATA,DISP=SHR
//OUTFILE  DD   DSN=AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS,DISP=SHR
//SYSIN    DD   *
   REPRO INFILE(INFILE) OUTFILE(OUTFILE)
/*
//*******************************************************************
//* STEP 8: REBUILD CARDDATA ALTERNATE INDEX
//*******************************************************************
//BLDCAIX  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DEFINE ALTERNATEINDEX (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX)-
   RELATE(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS)                    -
   KEYS(11 16)                                                   -
   NONUNIQUEKEY                                                  -
   UPGRADE                                                       -
   RECORDSIZE(150,150)                                           -
   VOLUMES(AWSHJ1)                                               -
   CYLINDERS(5,1))                                               -
   DATA (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.DATA))           -
   INDEX (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.INDEX))
   
   DEFINE PATH                                           -
   (NAME(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH)        -
    PATHENTRY(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX))
   
   BLDINDEX                                                      -
   INDATASET(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS)                 -
   OUTDATASET(AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX)
/*
//*******************************************************************
//* STEP 9: DELETE MIGRATED CARDXREF
//*******************************************************************
//DELXREF  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DELETE AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS CLUSTER
   DELETE AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX ALTERNATEINDEX
   IF MAXCC LE 08 THEN SET MAXCC = 0
/*
//*******************************************************************
//* STEP 10: REDEFINE ORIGINAL CARDXREF
//*******************************************************************
//DEFXREF  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DEFINE CLUSTER (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS) -
          CYLINDERS(1 5) -
          VOLUMES(AWSHJ1) -
          KEYS(16 0) -
          RECORDSIZE(50 50) -
          SHAREOPTIONS(2 3) -
          ERASE -
          INDEXED) -
          DATA (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS.DATA)) -
          INDEX (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS.INDEX))
/*
//*******************************************************************
//* STEP 11: RESTORE CARDXREF FROM BACKUP
//*******************************************************************
//RSTXREF  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//INFILE   DD   DSN=AWS.M2.CARDDEMO.BACKUP.&BKDATE..CARDXREF,DISP=SHR
//OUTFILE  DD   DSN=AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS,DISP=SHR
//SYSIN    DD   *
   REPRO INFILE(INFILE) OUTFILE(OUTFILE)
/*
//*******************************************************************
//* STEP 12: REBUILD CARDXREF ALTERNATE INDEX
//*******************************************************************
//BLDXAIX  EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DEFINE ALTERNATEINDEX (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX)-
   RELATE(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS)                    -
   KEYS(11,25)                                                   -
   NONUNIQUEKEY                                                  -
   UPGRADE                                                       -
   RECORDSIZE(50,50)                                             -
   FREESPACE(10,20)                                              -
   VOLUMES(AWSHJ1)                                               -
   CYLINDERS(5,1))                                               -
   DATA (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.DATA))           -
   INDEX (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.INDEX))
   
   DEFINE PATH                                           -
   (NAME(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH)        -
    PATHENTRY(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX))
   
   BLDINDEX                                                      -
   INDATASET(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS)                 -
   OUTDATASET(AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX)
/*
//*******************************************************************
//* STEP 13: OPEN CICS FILES
//*******************************************************************
//OPNFILE  EXEC PGM=SDSF
//ISFOUT   DD   SYSOUT=*
//CMDOUT   DD   SYSOUT=*
//ISFIN    DD   *
 /F CICSAWSA,'CEMT SET FIL(ACCTDAT ) OPE'
 /F CICSAWSA,'CEMT SET FIL(CARDDAT ) OPE'
 /F CICSAWSA,'CEMT SET FIL(CARDAIX ) OPE'
 /F CICSAWSA,'CEMT SET FIL(CARDXREF) OPE'
 /F CICSAWSA,'CEMT SET FIL(CXACAIX ) OPE'
/*
//*******************************************************************
//* STEP 14: VERIFY RESTORATION
//*******************************************************************
//VERIFY   EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   LISTCAT ENTRIES(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) ALL
   LISTCAT ENTRIES(AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) ALL
   LISTCAT ENTRIES(AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS) ALL
/*
```

### 4.4 Code Rollback Procedure

```bash
# Revert to backup branch
git fetch origin
git checkout main
git reset --hard backup/acct-id-refactor-pre-migration-20251218
git push origin main --force

# Or revert to tag
git checkout v1.0-pre-acctid-refactor
git checkout -b main-rollback
git push origin main-rollback

# Recompile all affected programs after code rollback
# Submit compilation JCL for each program
```

### 4.5 Post-Rollback Verification

| Verification | Method | Expected Result |
|--------------|--------|-----------------|
| ACCTDATA accessible | CICS file inquiry | File open, records readable |
| CARDDATA accessible | CICS file inquiry | File open, records readable |
| CARDXREF accessible | CICS file inquiry | File open, records readable |
| Account View works | CC00 -> CAVW transaction | Account displays correctly |
| Card List works | CC00 -> CCLI transaction | Cards list correctly |
| Batch job runs | Submit POSTTRAN | RC=0 |
| Record counts match | IDCAMS LISTCAT | Match pre-migration counts |

## 5. Testing Criteria for Rollback Decision

### 5.1 Functional Tests

| Test Case | Description | Pass Criteria |
|-----------|-------------|---------------|
| TC-001 | User login | Successful authentication |
| TC-002 | View account by ID | Account details display correctly |
| TC-003 | List cards for account | All associated cards appear |
| TC-004 | Update card details | Changes persist correctly |
| TC-005 | Add new transaction | Transaction posts successfully |
| TC-006 | Generate statement | Statement contains correct data |
| TC-007 | Batch transaction posting | All transactions process |
| TC-008 | Interest calculation | Correct amounts calculated |

### 5.2 Data Integrity Tests

| Test Case | Description | Pass Criteria |
|-----------|-------------|---------------|
| DI-001 | Account record count | Matches pre-migration count |
| DI-002 | Card record count | Matches pre-migration count |
| DI-003 | Cross-reference integrity | All FKs resolve correctly |
| DI-004 | Account ID format | All IDs are 14 characters |
| DI-005 | No orphan records | All cards have valid accounts |
| DI-006 | No duplicate keys | No VSAM duplicate key errors |

### 5.3 Performance Tests

| Test Case | Description | Pass Criteria |
|-----------|-------------|---------------|
| PT-001 | Account lookup response | < 2 seconds |
| PT-002 | Card list response | < 3 seconds |
| PT-003 | Batch job duration | Within 150% of baseline |
| PT-004 | CICS transaction rate | Within 90% of baseline |

## 6. Emergency Contacts

| Role | Name | Contact | Availability |
|------|------|---------|--------------|
| Project Lead | TBD | TBD | Business hours |
| DBA | TBD | TBD | 24/7 on-call |
| CICS Administrator | TBD | TBD | 24/7 on-call |
| Application Support | TBD | TBD | Business hours |

## 7. Rollback Timeline

| Phase | Duration | Cumulative Time |
|-------|----------|-----------------|
| Decision to rollback | 15 minutes | 15 minutes |
| Stop CICS transactions | 10 minutes | 25 minutes |
| Execute rollback JCL | 30 minutes | 55 minutes |
| Verify data restoration | 30 minutes | 1 hour 25 minutes |
| Revert code changes | 15 minutes | 1 hour 40 minutes |
| Recompile programs | 30 minutes | 2 hours 10 minutes |
| Restart CICS | 10 minutes | 2 hours 20 minutes |
| Functional verification | 30 minutes | 2 hours 50 minutes |
| **Total Rollback Time** | - | **~3 hours** |

## 8. Post-Rollback Actions

1. **Document the Issue**: Record the specific problem that triggered the rollback
2. **Root Cause Analysis**: Investigate why the migration failed
3. **Update Migration Plan**: Revise procedures to address identified issues
4. **Schedule Re-attempt**: Plan new migration window after fixes are implemented
5. **Communicate Status**: Notify all stakeholders of rollback and next steps
6. **Retain Backup**: Keep backup files until successful migration is confirmed

## 9. Backup Retention Policy

| Backup Type | Retention Period | Storage Location |
|-------------|------------------|------------------|
| Pre-migration VSAM backup | 90 days | Production DASD |
| Git backup branch | Permanent | Git repository |
| Git tag | Permanent | Git repository |
| Migration logs | 1 year | Archive storage |
| Rollback logs | 1 year | Archive storage |

## 10. Appendix: Quick Reference Commands

### IDCAMS Commands

```
* List catalog entry
LISTCAT ENTRIES(dataset.name) ALL

* Delete cluster
DELETE dataset.name CLUSTER

* Copy data
REPRO INFILE(input) OUTFILE(output)

* Print records
PRINT INFILE(dataset) COUNT(n) CHARACTER
```

### CICS Commands

```
* Close file
CEMT SET FILE(filename) CLOSE

* Open file
CEMT SET FILE(filename) OPEN

* Inquire file status
CEMT INQUIRE FILE(filename)
```

### Git Commands

```bash
# Create backup branch
git checkout -b backup/name

# Create tag
git tag -a tagname -m "message"

# Reset to backup
git reset --hard backup/name

# List branches
git branch -a

# List tags
git tag -l
```
