# Data Migration Strategy - Phase 1 Analysis

## Document Information
- **Project**: CardDemo Mainframe Application
- **Repository**: COG-GTM/mainframe-modernization-carddemo
- **Analysis Date**: 2025-12-18
- **Current Format**: 11-digit numeric (PIC 9(11))
- **Target Format**: 14-digit alphanumeric (PIC X(14))

## Executive Summary

This document outlines the strategy for migrating existing 11-digit numeric Account IDs to the new 14-digit alphanumeric format. The migration must preserve data integrity, maintain referential consistency across all related files, and provide a clear rollback path in case of issues.

## 1. Current Data Analysis

### 1.1 Data Volume Assessment

Based on analysis of `app/data/ASCII/acctdata.txt`:

| Metric | Value |
|--------|-------|
| Total Account Records | 50 |
| Record Length | 300 bytes |
| Account ID Position | Bytes 1-11 |
| Account ID Range | 00000000001 to 00000000050 |
| Account ID Pattern | Sequential numeric, zero-padded |

### 1.2 Current Account ID Characteristics

The existing Account IDs follow a consistent pattern:

```
Format: NNNNNNNNNNN (11 numeric digits)
Example: 00000000001
         00000000002
         ...
         00000000050
```

**Observations**:
- All IDs are purely numeric
- All IDs are zero-padded on the left
- Sequential numbering from 1 to 50
- No gaps in the sequence
- Maximum value used: 50 (well below the 11-digit maximum of 99,999,999,999)

### 1.3 Related Data Files

| File | Record Count | Account ID Field | Offset | Relationship |
|------|--------------|------------------|--------|--------------|
| ACCTDATA | 50 | Primary Key | 0 | Master account data |
| CARDDATA | ~50 | Foreign Key | 16 | Card-to-account reference |
| CARDXREF | ~50 | Foreign Key | 25 | Cross-reference lookup |
| TRANCAT | Variable | Foreign Key | 0 | Transaction category balances |
| DALYTRAN | Variable | Foreign Key | - | Daily transactions |

## 2. Conversion Approach Options

### 2.1 Option A: Zero Prefix Padding (RECOMMENDED)

**Description**: Prefix existing 11-digit IDs with three zeros to create 14-digit IDs.

```
Current:  00000000001
New:      00000000000001

Current:  00000000050
New:      00000000000050
```

**Advantages**:
- Simple, deterministic conversion
- Preserves existing sort order
- Easy to implement and verify
- Reversible (can extract original ID)
- Maintains numeric appearance for backward compatibility
- No business logic changes required for ID generation

**Disadvantages**:
- Does not leverage alphanumeric capability
- May require future migration if alphanumeric IDs are needed

**Conversion Logic**:
```cobol
* COBOL Conversion Logic
MOVE ZEROS TO WS-NEW-ACCT-ID
MOVE WS-OLD-ACCT-ID TO WS-NEW-ACCT-ID(4:11)

* Or using STRING
STRING '000' WS-OLD-ACCT-ID DELIMITED BY SIZE
       INTO WS-NEW-ACCT-ID
END-STRING
```

### 2.2 Option B: Alphanumeric Prefix

**Description**: Add a 3-character alphanumeric prefix to identify account type.

```
Current:  00000000001
New:      ACC00000000001

Current:  00000000050
New:      ACC00000000050
```

**Advantages**:
- Leverages alphanumeric capability
- Allows for account type categorization
- Future-proof for different account types

**Disadvantages**:
- Requires validation logic changes
- Changes sort order (alphabetic before numeric)
- More complex conversion and verification
- May affect existing reports and interfaces

### 2.3 Option C: New Alphanumeric Scheme

**Description**: Generate entirely new alphanumeric IDs with a mapping table.

```
Current:  00000000001
New:      CD2025A0000001

Current:  00000000050
New:      CD2025A0000050
```

**Advantages**:
- Maximum flexibility
- Can encode metadata (year, type, region)
- Clean break from legacy format

**Disadvantages**:
- Requires mapping table maintenance
- Complex migration process
- Higher risk of errors
- Difficult to rollback

## 3. Recommended Approach: Option A (Zero Prefix Padding)

### 3.1 Rationale

Option A is recommended for the following reasons:

1. **Simplicity**: The conversion is straightforward and deterministic
2. **Reversibility**: Original IDs can be extracted by removing the prefix
3. **Minimal Risk**: No business logic changes required for ID interpretation
4. **Sort Order Preservation**: Maintains existing ordering for reports and queries
5. **Verification**: Easy to verify conversion accuracy
6. **Rollback**: Simple to reverse if issues are discovered

### 3.2 Conversion Specification

| Attribute | Current | New |
|-----------|---------|-----|
| Length | 11 | 14 |
| Type | Numeric | Alphanumeric |
| Format | PIC 9(11) | PIC X(14) |
| Padding | Left zeros | Left zeros |
| Conversion | N/A | Prefix with '000' |

### 3.3 Sample Conversions

| Original ID | Converted ID | Verification |
|-------------|--------------|--------------|
| 00000000001 | 00000000000001 | Length=14, Prefix='000' |
| 00000000002 | 00000000000002 | Length=14, Prefix='000' |
| 00000000010 | 00000000000010 | Length=14, Prefix='000' |
| 00000000050 | 00000000000050 | Length=14, Prefix='000' |

## 4. Migration Process

### 4.1 Pre-Migration Steps

1. **Complete Backup**: Create full backups of all VSAM files (see Backup Procedures document)
2. **Verification Counts**: Record row counts for all affected files
3. **Checksum Generation**: Generate checksums for data integrity verification
4. **Environment Preparation**: Ensure sufficient disk space for migration files
5. **Downtime Scheduling**: Plan maintenance window for migration

### 4.2 Migration Sequence

The migration must follow a specific sequence to maintain referential integrity:

```
Step 1: Backup all VSAM files
        ↓
Step 2: Export ACCTDATA to sequential file
        ↓
Step 3: Convert Account IDs in export file
        ↓
Step 4: Redefine ACCTDATA VSAM with new key length
        ↓
Step 5: Load converted data into new ACCTDATA
        ↓
Step 6: Repeat Steps 2-5 for CARDDATA
        ↓
Step 7: Repeat Steps 2-5 for CARDXREF
        ↓
Step 8: Repeat Steps 2-5 for TRANCAT (if exists)
        ↓
Step 9: Verify all file counts and relationships
        ↓
Step 10: Update alternate indexes
```

### 4.3 Migration JCL Template

```jcl
//MIGRATE  JOB 'ACCT ID MIGRATION',CLASS=A,MSGCLASS=0,
//         NOTIFY=&SYSUID
//*******************************************************************
//* STEP 1: EXPORT ACCTDATA TO SEQUENTIAL FILE
//*******************************************************************
//EXPORT   EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//INFILE   DD   DSN=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS,DISP=SHR
//OUTFILE  DD   DSN=AWS.M2.CARDDEMO.ACCTDATA.MIGRATE.PS,
//              DISP=(NEW,CATLG,DELETE),
//              SPACE=(CYL,(10,5)),
//              DCB=(RECFM=FB,LRECL=300,BLKSIZE=0)
//SYSIN    DD   *
   REPRO INFILE(INFILE) OUTFILE(OUTFILE)
/*
//*******************************************************************
//* STEP 2: CONVERT ACCOUNT IDS (COBOL PROGRAM)
//*******************************************************************
//CONVERT  EXEC PGM=ACCTCONV
//STEPLIB  DD   DSN=AWS.M2.CARDDEMO.LOADLIB,DISP=SHR
//INFILE   DD   DSN=AWS.M2.CARDDEMO.ACCTDATA.MIGRATE.PS,DISP=SHR
//OUTFILE  DD   DSN=AWS.M2.CARDDEMO.ACCTDATA.CONVERT.PS,
//              DISP=(NEW,CATLG,DELETE),
//              SPACE=(CYL,(10,5)),
//              DCB=(RECFM=FB,LRECL=303,BLKSIZE=0)
//SYSOUT   DD   SYSOUT=*
//*******************************************************************
//* STEP 3: DELETE OLD VSAM FILE
//*******************************************************************
//DELETE   EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DELETE AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS CLUSTER
   IF MAXCC LE 08 THEN SET MAXCC = 0
/*
//*******************************************************************
//* STEP 4: DEFINE NEW VSAM FILE WITH 14-BYTE KEY
//*******************************************************************
//DEFINE   EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   DEFINE CLUSTER (NAME(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) -
          CYLINDERS(1 5) -
          KEYS(14 0) -
          RECORDSIZE(303 303) -
          SHAREOPTIONS(2 3) -
          INDEXED) -
          DATA (NAME(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS.DATA)) -
          INDEX (NAME(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS.INDEX))
/*
//*******************************************************************
//* STEP 5: LOAD CONVERTED DATA
//*******************************************************************
//LOAD     EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//INFILE   DD   DSN=AWS.M2.CARDDEMO.ACCTDATA.CONVERT.PS,DISP=SHR
//OUTFILE  DD   DSN=AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS,DISP=SHR
//SYSIN    DD   *
   REPRO INFILE(INFILE) OUTFILE(OUTFILE)
/*
```

### 4.4 Conversion Program (ACCTCONV)

```cobol
       IDENTIFICATION DIVISION.
       PROGRAM-ID. ACCTCONV.
      *****************************************************************
      * Program to convert 11-digit Account IDs to 14-digit format
      * by prefixing with '000'
      *****************************************************************
       
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT INPUT-FILE ASSIGN TO INFILE
                  FILE STATUS IS WS-INPUT-STATUS.
           SELECT OUTPUT-FILE ASSIGN TO OUTFILE
                  FILE STATUS IS WS-OUTPUT-STATUS.
       
       DATA DIVISION.
       FILE SECTION.
       FD  INPUT-FILE.
       01  INPUT-RECORD.
           05 IN-ACCT-ID              PIC 9(11).
           05 IN-ACCT-DATA            PIC X(289).
       
       FD  OUTPUT-FILE.
       01  OUTPUT-RECORD.
           05 OUT-ACCT-ID             PIC X(14).
           05 OUT-ACCT-DATA           PIC X(289).
       
       WORKING-STORAGE SECTION.
       01  WS-INPUT-STATUS            PIC XX.
       01  WS-OUTPUT-STATUS           PIC XX.
       01  WS-RECORD-COUNT            PIC 9(9) VALUE 0.
       01  WS-EOF-FLAG                PIC X VALUE 'N'.
           88 END-OF-FILE             VALUE 'Y'.
       
       PROCEDURE DIVISION.
       0000-MAIN.
           OPEN INPUT INPUT-FILE
           OPEN OUTPUT OUTPUT-FILE
           
           PERFORM UNTIL END-OF-FILE
               READ INPUT-FILE
                   AT END SET END-OF-FILE TO TRUE
                   NOT AT END PERFORM 1000-CONVERT-RECORD
               END-READ
           END-PERFORM
           
           DISPLAY 'RECORDS CONVERTED: ' WS-RECORD-COUNT
           
           CLOSE INPUT-FILE
           CLOSE OUTPUT-FILE
           STOP RUN.
       
       1000-CONVERT-RECORD.
           MOVE SPACES TO OUT-ACCT-ID
           STRING '000' IN-ACCT-ID DELIMITED BY SIZE
                  INTO OUT-ACCT-ID
           END-STRING
           MOVE IN-ACCT-DATA TO OUT-ACCT-DATA
           WRITE OUTPUT-RECORD
           ADD 1 TO WS-RECORD-COUNT.
```

## 5. Verification Procedures

### 5.1 Pre-Migration Verification

| Check | Expected Result | Action if Failed |
|-------|-----------------|------------------|
| ACCTDATA record count | 50 | Investigate missing records |
| CARDDATA record count | ~50 | Document variance |
| CARDXREF record count | ~50 | Document variance |
| Backup file sizes | Match originals | Re-run backup |

### 5.2 Post-Migration Verification

| Check | Expected Result | Action if Failed |
|-------|-----------------|------------------|
| New ACCTDATA record count | 50 | Rollback and investigate |
| All Account IDs start with '000' | 100% | Rollback and fix conversion |
| All Account IDs are 14 characters | 100% | Rollback and fix conversion |
| Cross-reference integrity | All FKs valid | Rollback and investigate |
| Application connectivity | Successful | Rollback and debug |

### 5.3 Verification Queries

```jcl
//*******************************************************************
//* VERIFY RECORD COUNTS
//*******************************************************************
//VERIFY   EXEC PGM=IDCAMS
//SYSPRINT DD   SYSOUT=*
//SYSIN    DD   *
   PRINT INFILE(ACCTDATA) COUNT(1) -
         CHARACTER
   LISTCAT ENTRIES(AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) ALL
/*
```

## 6. New Account ID Generation

### 6.1 For New Accounts Post-Migration

After migration, new Account IDs should be generated using the 14-digit format:

```cobol
* Generate new 14-digit Account ID
* Assuming WS-LAST-ACCT-NUM contains the last used number

ADD 1 TO WS-LAST-ACCT-NUM
MOVE ZEROS TO WS-NEW-ACCT-ID
MOVE WS-LAST-ACCT-NUM TO WS-NEW-ACCT-ID(4:11)
```

### 6.2 ID Generation Rules

1. New IDs continue the numeric sequence
2. Prefix remains '000' for consistency
3. Maximum value: 00099999999999 (allows for 99,999,999,999 accounts)
4. Future alphanumeric IDs can use different prefixes (e.g., 'ACC', 'BUS', etc.)

## 7. Application Code Changes Summary

### 7.1 Changes Required for Migration Support

| Component | Change Type | Description |
|-----------|-------------|-------------|
| Copybooks | Field length | Change PIC 9(11) to PIC X(14) |
| BMS Maps | Field length | Change LENGTH=11 to LENGTH=14 |
| Validation | Logic removal | Remove IS NUMERIC checks |
| Error Messages | Text update | Change "11 digit" to "14 character" |
| JCL | Key definitions | Change KEYS(11,x) to KEYS(14,x) |
| Working Storage | Variable sizes | Update all account ID variables |

### 7.2 Validation Logic Changes

Current validation (to be removed):
```cobol
IF CC-ACCT-ID IS NOT NUMERIC
   SET INPUT-ERROR TO TRUE
   ...
END-IF
```

New validation (alphanumeric):
```cobol
IF CC-ACCT-ID = SPACES OR CC-ACCT-ID = LOW-VALUES
   SET INPUT-ERROR TO TRUE
   ...
END-IF
```

## 8. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Data corruption during conversion | Low | High | Full backup, verification checks |
| Referential integrity loss | Medium | High | Sequential migration, FK verification |
| Application failures | Medium | High | Comprehensive testing, rollback plan |
| Performance degradation | Low | Medium | Index optimization, monitoring |
| Incomplete migration | Low | High | Automated verification, checksums |

## 9. Timeline Estimate

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| Backup creation | 1 hour | None |
| Data export | 30 minutes | Backup complete |
| Data conversion | 30 minutes | Export complete |
| VSAM redefinition | 1 hour | Conversion complete |
| Data reload | 30 minutes | Redefinition complete |
| Verification | 2 hours | Reload complete |
| Application testing | 4 hours | Verification complete |
| **Total Estimated** | **10 hours** | - |

## 10. Success Criteria

The migration will be considered successful when:

1. All 50 account records are converted and loaded
2. All Account IDs are exactly 14 characters
3. All Account IDs begin with '000'
4. All cross-reference relationships are intact
5. All online CICS programs function correctly
6. All batch programs complete successfully
7. All reports display correct account information
8. No data integrity errors are detected
