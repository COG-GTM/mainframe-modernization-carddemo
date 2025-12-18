# Account ID Refactoring Impact Analysis

## Phase 1: Preparation and Impact Analysis

**Document Version:** 1.0  
**Date:** December 18, 2025  
**Project:** CardDemo Mainframe Application  
**Change Request:** Refactor Account ID from 11-digit numeric (PIC 9(11)) to 14-digit alphanumeric (PIC X(14))

---

## 1. Executive Summary

This document provides a comprehensive impact analysis for refactoring the Account ID field in the CardDemo mainframe application from an 11-digit numeric format (`PIC 9(11)`) to a 14-digit alphanumeric format (`PIC X(14)`). The Account ID field is a critical identifier used throughout the application for account management, transaction processing, and cross-reference lookups.

The analysis identifies 213+ references to Account ID across 25+ source files, including COBOL programs, copybooks, BMS screen maps, and JCL job definitions. The change impacts batch processing programs, online CICS programs, screen interfaces, VSAM file structures, and validation logic.

Key findings include:

- The Account ID serves as the primary key in the ACCTDATA VSAM file and as an alternate index key in CARDXREF and CARDDATA files
- Current validation logic enforces numeric-only values with exactly 11 digits
- Error messages throughout the application reference the "11 digit number" requirement
- BMS screen maps define the field with LENGTH=11 and numeric picture clauses
- Data migration will require converting 50+ existing account records from numeric to alphanumeric format

The recommended approach is a phased implementation with comprehensive testing at each stage, starting with copybook modifications, followed by program updates, JCL changes, and finally data migration.

---

## 2. Complete Component Inventory

### 2.1 Data Structure Definitions (File Descriptors)

The following COBOL programs contain File Descriptor (FD) definitions for the Account ID field:

| File | Line | Field Name | Current Definition | Purpose |
|------|------|------------|-------------------|---------|
| `app/cbl/CBTRN01C.cbl` | 88 | `FD-ACCT-ID` | `PIC 9(11)` | Account file record key for transaction posting |
| `app/cbl/CBACT01C.cbl` | 39 | `FD-ACCT-ID` | `PIC 9(11)` | Account file record key for account data printing |
| `app/cbl/CBACT04C.cbl` | 64 | `FD-TRANCAT-ACCT-ID` | `PIC 9(11)` | Transaction category balance key component |
| `app/cbl/CBACT04C.cbl` | 73 | `FD-XREF-ACCT-ID` | `PIC 9(11)` | Cross-reference file alternate key |
| `app/cbl/CBACT04C.cbl` | 86 | `FD-ACCT-ID` | `PIC 9(11)` | Account file record key for interest calculation |
| `app/cbl/CBTRN02C.cbl` | 88 | `FD-ACCT-ID` | `PIC 9(11)` | Account file record key for transaction posting |
| `app/cbl/CBTRN02C.cbl` | 94 | `FD-TRANCAT-ACCT-ID` | `PIC 9(11)` | Transaction category balance key component |
| `app/cbl/CBSTM03B.CBL` | 77 | `FD-ACCT-ID` | `PIC 9(11)` | Account file record key for statement generation |

### 2.2 Copybook Definitions

The following copybooks contain Account ID field definitions that are shared across multiple programs:

| File | Line | Field Name | Current Definition | Usage |
|------|------|------------|-------------------|-------|
| `app/cpy/CVACT01Y.cpy` | 5 | `ACCT-ID` | `PIC 9(11)` | Account record structure - primary account identifier |
| `app/cpy/CVACT02Y.cpy` | 6 | `CARD-ACCT-ID` | `PIC 9(11)` | Card record structure - links card to account |
| `app/cpy/CVACT03Y.cpy` | 7 | `XREF-ACCT-ID` | `PIC 9(11)` | Cross-reference record - links card number to account |
| `app/cpy/CVTRA01Y.cpy` | 6 | `TRANCAT-ACCT-ID` | `PIC 9(11)` | Transaction category balance key component |
| `app/cpy/COCOM01Y.cpy` | 38 | `CDEMO-ACCT-ID` | `PIC 9(11)` | Communication area - passed between programs |
| `app/cpy/CVCRD01Y.cpy` | 34-36 | `CC-ACCT-ID` | `PIC X(11)` | Work area with alphanumeric definition |
| `app/cpy/CVCRD01Y.cpy` | 36 | `CC-ACCT-ID-N` | `PIC 9(11)` | Numeric redefinition for validation |

### 2.3 Screen Interface Definitions (BMS Maps)

#### BMS Map Source Files

| File | Lines | Field Name | Current Definition | Screen |
|------|-------|------------|-------------------|--------|
| `app/bms/COACTVW.bms` | 84-90 | `ACCTSID` | `LENGTH=11, PICIN='99999999999'` | Account View screen |

#### Generated BMS Copybooks

| File | Line | Field Name | Current Definition | Purpose |
|------|------|------------|-------------------|---------|
| `app/cpy-bms/COACTVW.CPY` | 60 | `ACCTSIDI` | `PIC 99999999999` | Account View input field |
| `app/cpy-bms/COACTVW.CPY` | 284 | `ACCTSIDO` | `PIC X(11)` | Account View output field |
| `app/cpy-bms/COCRDLI.CPY` | 66 | `ACCTSIDI` | `PIC X(11)` | Card List input field |
| `app/cpy-bms/COCRDLI.CPY` | 84 | `ACCTNO1I` | `PIC X(11)` | Card List account number row 1 |
| `app/cpy-bms/COCRDLI.CPY` | 114 | `ACCTNO2I` | `PIC X(11)` | Card List account number row 2 |
| `app/cpy-bms/COCRDLI.CPY` | 144 | `ACCTNO3I` | `PIC X(11)` | Card List account number row 3 |
| `app/cpy-bms/COCRDLI.CPY` | 174 | `ACCTNO4I` | `PIC X(11)` | Card List account number row 4 |
| `app/cpy-bms/COCRDLI.CPY` | 204 | `ACCTNO5I` | `PIC X(11)` | Card List account number row 5 |
| `app/cpy-bms/COCRDLI.CPY` | 234 | `ACCTNO6I` | `PIC X(11)` | Card List account number row 6 |
| `app/cpy-bms/COCRDLI.CPY` | 264 | `ACCTNO7I` | `PIC X(11)` | Card List account number row 7 |

### 2.4 Working Storage Variables

The following programs contain Working Storage definitions for Account ID:

| File | Lines | Field Names | Current Definition |
|------|-------|-------------|-------------------|
| `app/cbl/COACTVWC.cbl` | 78-80 | `WS-CARD-RID-ACCT-ID`, `WS-CARD-RID-ACCT-ID-X` | `PIC 9(11)`, `PIC X(11)` |
| `app/cbl/COCRDSLC.cbl` | 73-74 | `CARD-ACCT-ID-X`, `CARD-ACCT-ID-N` | `PIC X(11)`, `PIC 9(11)` |
| `app/cbl/COCRDSLC.cbl` | 99-101 | `WS-CARD-RID-ACCT-ID`, `WS-CARD-RID-ACCT-ID-X` | `PIC 9(11)`, `PIC X(11)` |
| `app/cbl/COCRDLIC.cbl` | 99-100 | `CARD-ACCT-ID-X`, `CARD-ACCT-ID-N` | `PIC X(11)`, `PIC 9(11)` |
| `app/cbl/COCRDLIC.cbl` | 139-141 | `WS-CARD-RID-ACCT-ID`, `WS-CARD-RID-ACCT-ID-X` | `PIC 9(11)`, `PIC X(11)` |
| `app/cbl/COCRDLIC.cbl` | 232, 235 | `WS-CA-LAST-CARD-ACCT-ID`, `WS-CA-FIRST-CARD-ACCT-ID` | `PIC 9(11)` |
| `app/cbl/COCRDUPC.cbl` | 104-105 | `CARD-ACCT-ID-X`, `CARD-ACCT-ID-N` | `PIC X(11)`, `PIC 9(11)` |
| `app/cbl/COCRDUPC.cbl` | 130-132 | `WS-CARD-RID-ACCT-ID`, `WS-CARD-RID-ACCT-ID-X` | `PIC 9(11)`, `PIC X(11)` |
| `app/cbl/COCRDUPC.cbl` | 316 | `CARD-UPDATE-ACCT-ID` | `PIC 9(11)` |
| `app/cbl/COACTUPC.cbl` | 358-359 | `CUST-ACCT-ID-X`, `CUST-ACCT-ID-N` | `PIC X(11)`, `PIC 9(11)` |
| `app/cbl/COACTUPC.cbl` | 381-383 | `WS-CARD-RID-ACCT-ID`, `WS-CARD-RID-ACCT-ID-X` | `PIC 9(11)`, `PIC X(11)` |
| `app/cbl/COACTUPC.cbl` | 671-673 | `ACUP-OLD-ACCT-ID-X`, `ACUP-OLD-ACCT-ID` | `PIC X(11)`, `PIC 9(11)` |
| `app/cbl/COACTUPC.cbl` | 759-761 | `ACUP-NEW-ACCT-ID-X`, `ACUP-NEW-ACCT-ID` | `PIC X(11)`, `PIC 9(11)` |
| `app/cbl/COTRN02C.cbl` | 55 | `WS-ACCT-ID-N` | `PIC 9(11)` |
| `app/cbl/CBACT04C.cbl` | 167 | `WS-LAST-ACCT-NUM` | `PIC X(11)` |
| `app/cbl/CBSTM03A.CBL` | 109 | `ST-ACCT-ID` | `PIC X(20)` |

### 2.5 Validation Logic Locations

The following programs contain validation logic that enforces the 11-digit numeric requirement:

| File | Lines | Validation Type | Current Logic |
|------|-------|-----------------|---------------|
| `app/cbl/COACTVWC.cbl` | 666-680 | Numeric check | `IF CC-ACCT-ID IS NOT NUMERIC OR CC-ACCT-ID EQUAL ZEROES` |
| `app/cbl/COCRDLIC.cbl` | 1007-1029 | Numeric check | `IF CC-ACCT-ID IS NOT NUMERIC` |
| `app/cbl/COCRDSLC.cbl` | 651-678 | Numeric check | `IF CC-ACCT-ID IS NOT NUMERIC` |
| `app/cbl/COCRDUPC.cbl` | 725-755 | Numeric check | `IF CC-ACCT-ID IS NOT NUMERIC` |
| `app/cbl/COACTUPC.cbl` | 1787-1815 | Numeric check | `IF CC-ACCT-ID IS NOT NUMERIC OR CC-ACCT-ID-N EQUAL ZEROS` |

### 2.6 Error Messages to Update

The following error messages reference the 11-digit requirement and must be updated:

| File | Line | Current Message |
|------|------|-----------------|
| `app/cbl/COACTVWC.cbl` | 126-128 | `'Account number must be a non zero 11 digit number'` |
| `app/cbl/COACTVWC.cbl` | 672 | `'Account Filter must be a non-zero 11 digit number'` |
| `app/cbl/COCRDLIC.cbl` | 1022 | `'ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER'` |
| `app/cbl/COCRDSLC.cbl` | 145-147 | `'Account number must be a non zero 11 digit number'` |
| `app/cbl/COCRDSLC.cbl` | 670 | `'ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER'` |
| `app/cbl/COCRDUPC.cbl` | 190-192 | `'Account number must be a non zero 11 digit number'` |
| `app/cbl/COCRDUPC.cbl` | 745 | `'ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER'` |
| `app/cbl/COACTUPC.cbl` | 494-496 | `'Account number must be a non zero 11 digit number'` |
| `app/cbl/COACTUPC.cbl` | 1807 | `'Account Number if supplied must be a 11 digit'` |

### 2.7 VSAM File Definitions (JCL)

The following JCL files define VSAM file structures with Account ID key specifications:

| File | Line | Key Definition | Purpose |
|------|------|----------------|---------|
| `app/jcl/ACCTFILE.jcl` | 40 | `KEYS(11 0)` | Primary key for ACCTDATA VSAM file (11 bytes at offset 0) |
| `app/jcl/CARDFILE.jcl` | 85 | `KEYS(11 16)` | Alternate index key for CARDDATA (11 bytes at offset 16) |
| `app/jcl/XREFFILE.jcl` | 74 | `KEYS(11,25)` | Alternate index key for CARDXREF (11 bytes at offset 25) |
| `app/jcl/TCATBALF.jcl` | 40 | `KEYS(17 0)` | Composite key including Account ID (11 bytes) + type (2 bytes) + category (4 bytes) |
| `app/jcl/PRTCATBL.jcl` | 47 | `TRANCAT-ACCT-ID,1,11,ZD` | Sort/print field definition |

### 2.8 Online CICS Programs Affected

| Program | Function | Account ID Usage |
|---------|----------|------------------|
| `COACTVWC` | Account View | Display and validate account information |
| `COACTUPC` | Account Update | Update account details |
| `COCRDLIC` | Card List | List cards by account filter |
| `COCRDSLC` | Card Select | Select card by account/card combination |
| `COCRDUPC` | Card Update | Update card information |
| `COTRN02C` | Transaction Entry | Enter transactions by account |
| `COBIL00C` | Bill Pay | Process bill payments by account |

### 2.9 Batch Programs Affected

| Program | Function | Account ID Usage |
|---------|----------|------------------|
| `CBACT01C` | Account Print | Read and print account data |
| `CBACT04C` | Interest Calculator | Calculate interest by account |
| `CBTRN01C` | Transaction Lookup | Lookup transactions by account |
| `CBTRN02C` | Transaction Posting | Post transactions to accounts |
| `CBTRN03C` | Transaction Report | Generate transaction reports |
| `CBSTM03A` | Statement Generation | Generate account statements |
| `CBSTM03B` | Statement I/O | File I/O for statement generation |

---

## 3. Data Migration Strategy

### 3.1 Current Data Format Analysis

The current Account ID format in `app/data/ASCII/acctdata.txt` shows:

```
00000000001Y00000001940{...  (Account ID: 00000000001)
00000000002Y00000001580{...  (Account ID: 00000000002)
00000000003Y00000001470{...  (Account ID: 00000000003)
...
00000000050Y00000004920{...  (Account ID: 00000000050)
```

Current characteristics:
- 11-digit numeric format with leading zeros
- Sequential numbering starting from 00000000001
- All existing IDs are purely numeric
- 50 account records in sample data

### 3.2 Conversion Approach

**Recommended Strategy: Prefix Padding with "000"**

Convert existing 11-digit numeric Account IDs to 14-digit alphanumeric format by adding a "000" prefix:

| Current Format (11 digits) | New Format (14 characters) |
|---------------------------|---------------------------|
| `00000000001` | `00000000000001` |
| `00000000002` | `00000000000002` |
| `00000000050` | `00000000000050` |

**Rationale:**
- Preserves existing numeric sequence for backward compatibility
- Maintains sort order consistency
- Allows future alphanumeric IDs to use prefix characters (e.g., "ACC00000000001")
- Minimizes data transformation complexity

### 3.3 New Account ID Format Specification

| Attribute | Specification |
|-----------|---------------|
| Length | 14 characters |
| Format | Alphanumeric (PIC X(14)) |
| Valid Characters | A-Z, 0-9 |
| Case Sensitivity | Uppercase only |
| Leading Zeros | Preserved for numeric-only IDs |
| Validation | Non-blank, non-zero, non-low-values |

### 3.4 Migration Steps

**Step 1: Backup Current Data**
```jcl
//BACKUP   EXEC PGM=IDCAMS
//SYSIN    DD *
   REPRO INFILE(ACCTDATA) OUTFILE(ACCTBKUP)
/*
```

**Step 2: Create Migration Utility Program**
- Read existing VSAM files
- Convert 11-digit Account IDs to 14-character format
- Write to new VSAM files with updated key definitions

**Step 3: Update VSAM File Definitions**
- Modify JCL to define new key lengths (14 instead of 11)
- Update record sizes to accommodate additional 3 bytes
- Rebuild alternate indexes

**Step 4: Data Conversion Execution**
- Execute migration utility in controlled environment
- Validate record counts match
- Verify key integrity

**Step 5: Cross-Reference Updates**
- Update CARDXREF file with new Account ID format
- Update CARDDATA alternate index
- Rebuild all affected indexes

### 3.5 Record Size Impact

| File | Current Record Size | New Record Size | Change |
|------|--------------------|--------------------|--------|
| ACCTDATA | 300 bytes | 303 bytes | +3 bytes |
| CARDDATA | 150 bytes | 153 bytes | +3 bytes |
| CARDXREF | 50 bytes | 53 bytes | +3 bytes |
| TCATBALF | 50 bytes | 53 bytes | +3 bytes |

---

## 4. Risk Assessment

### 4.1 High-Risk Areas

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| VSAM file corruption during migration | Critical | Medium | Full backups, staged migration, validation scripts |
| Cross-reference integrity loss | Critical | Medium | Rebuild all indexes, validate relationships |
| Online transaction failures | High | Medium | Phased deployment, fallback procedures |
| Batch job failures | High | Medium | Test in parallel environment first |
| Data truncation | High | Low | Field expansion (11 to 14) eliminates truncation risk |

### 4.2 Medium-Risk Areas

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Screen display issues | Medium | Medium | Update BMS maps, test all screens |
| Validation logic failures | Medium | Medium | Update all validation routines |
| Report formatting issues | Medium | Low | Update report layouts |
| Performance degradation | Medium | Low | Monitor key access patterns |

### 4.3 Low-Risk Areas

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Copybook compilation errors | Low | Low | Staged compilation, unit testing |
| Error message confusion | Low | Low | Update all error messages |

### 4.4 Impact on VSAM File Structures

**ACCTDATA VSAM File:**
- Primary key changes from 11 to 14 bytes
- Record size increases from 300 to 303 bytes
- All existing records must be migrated
- Index must be rebuilt

**CARDXREF VSAM File:**
- Alternate index key changes from 11 to 14 bytes at offset 25
- Record size increases from 50 to 53 bytes
- Alternate index must be rebuilt

**CARDDATA VSAM File:**
- Alternate index key changes from 11 to 14 bytes at offset 16
- Record size increases from 150 to 153 bytes
- Alternate index must be rebuilt

**TCATBALF VSAM File:**
- Composite key changes from 17 to 20 bytes (Account ID portion: 11 to 14)
- Record size increases from 50 to 53 bytes
- Index must be rebuilt

---

## 5. Rollback Procedures

### 5.1 Pre-Implementation Backup Checklist

Before any changes are made, create backups of:

- [ ] All VSAM data files (ACCTDATA, CARDDATA, CARDXREF, TCATBALF)
- [ ] All COBOL source programs
- [ ] All copybooks
- [ ] All BMS map source and generated copybooks
- [ ] All JCL job definitions
- [ ] CICS resource definitions
- [ ] Current load modules

### 5.2 Rollback by Component Type

#### 5.2.1 Copybook Rollback
```
1. Stop all CICS regions using affected programs
2. Restore original copybook source files
3. Recompile all affected programs
4. Redeploy load modules
5. Restart CICS regions
```

#### 5.2.2 COBOL Program Rollback
```
1. Stop affected CICS transactions
2. Restore original program source files
3. Recompile programs
4. Redeploy load modules
5. Restart transactions
```

#### 5.2.3 BMS Map Rollback
```
1. Stop affected CICS transactions
2. Restore original BMS source files
3. Reassemble BMS maps
4. Redeploy map load modules
5. Restart transactions
```

#### 5.2.4 VSAM File Rollback
```
1. Stop all batch jobs and CICS transactions
2. Close all VSAM files in CICS
3. Delete current VSAM clusters
4. Restore VSAM files from backup
5. Rebuild alternate indexes if needed
6. Reopen files in CICS
7. Restart batch jobs and transactions
```

#### 5.2.5 JCL Rollback
```
1. Restore original JCL source files
2. Resubmit file definition jobs if VSAM structures changed
3. Verify file definitions match backup data
```

### 5.3 Emergency Rollback Procedure

In case of critical failure during production deployment:

```
1. IMMEDIATE: Stop all CICS transactions (CEMT SET TRAN(*) DIS)
2. IMMEDIATE: Cancel all running batch jobs
3. Close all VSAM files (CEMT SET FILE(*) CLO)
4. Restore all VSAM files from pre-migration backup
5. Restore all load modules from pre-migration backup
6. Restore CICS resource definitions
7. Open VSAM files (CEMT SET FILE(*) OPE)
8. Enable transactions (CEMT SET TRAN(*) ENA)
9. Verify system functionality
10. Document incident and root cause
```

---

## 6. External System Dependencies

### 6.1 Identified External Interfaces

Based on the CardDemo application architecture, the following external system interfaces may be affected:

| Interface | Type | Impact | Coordination Required |
|-----------|------|--------|----------------------|
| Statement Generation Output | File | HTML/Text statements include Account ID | Update output formatting |
| Transaction Reports | File | Reports include Account ID fields | Update report layouts |
| Daily Transaction Input | File | Input files may contain Account ID | Coordinate with upstream systems |
| Reject File Output | File | Rejected transactions include Account ID | Update reject record layout |

### 6.2 Reporting and Extract Processes

| Process | File | Impact |
|---------|------|--------|
| `CBSTM03A` | Statement files | Account ID displayed in statements |
| `CBTRN03C` | Transaction reports | Account ID in report headers |
| `PRTCATBL.jcl` | Category balance print | Sort/print by Account ID |

### 6.3 Coordination Requirements

**Pre-Deployment:**
- Notify all downstream systems of Account ID format change
- Provide new field specifications (14-character alphanumeric)
- Coordinate deployment timing with dependent systems
- Update interface documentation

**During Deployment:**
- Synchronize cutover timing with external systems
- Verify interface file formats
- Monitor interface processing

**Post-Deployment:**
- Validate external system processing
- Verify report accuracy
- Confirm statement generation

---

## 7. Recommended Implementation Timeline

### Phase 1: Preparation (Weeks 1-2)

| Week | Task | Deliverable |
|------|------|-------------|
| 1 | Complete impact analysis | This document |
| 1 | Create detailed test plan | Test plan document |
| 1 | Set up parallel test environment | Test environment ready |
| 2 | Create backup procedures | Backup scripts |
| 2 | Develop migration utility | Migration program |
| 2 | Create rollback procedures | Rollback scripts |

### Phase 2: Development (Weeks 3-5)

| Week | Task | Deliverable |
|------|------|-------------|
| 3 | Update copybooks | Modified copybooks |
| 3 | Update BMS maps | Modified BMS source |
| 4 | Update COBOL programs | Modified programs |
| 4 | Update JCL definitions | Modified JCL |
| 5 | Update validation logic | Modified validation |
| 5 | Update error messages | Modified messages |

### Phase 3: Testing (Weeks 6-8)

| Week | Task | Deliverable |
|------|------|-------------|
| 6 | Unit testing | Unit test results |
| 6 | Integration testing | Integration test results |
| 7 | System testing | System test results |
| 7 | Performance testing | Performance metrics |
| 8 | User acceptance testing | UAT sign-off |
| 8 | Regression testing | Regression test results |

### Phase 4: Deployment (Week 9)

| Day | Task | Deliverable |
|-----|------|-------------|
| Mon | Final backups | Backup confirmation |
| Tue | Deploy to production | Deployment checklist |
| Wed | Data migration | Migration verification |
| Thu | Validation and monitoring | Validation report |
| Fri | Post-deployment review | Review document |

### Phase 5: Post-Implementation (Week 10)

| Task | Deliverable |
|------|-------------|
| Monitor system performance | Performance report |
| Address any issues | Issue resolution log |
| Update documentation | Updated system documentation |
| Close project | Project closure report |

---

## Appendix A: Complete File Reference

### COBOL Programs with Account ID References

1. `app/cbl/CBACT01C.cbl` - Account data print
2. `app/cbl/CBACT04C.cbl` - Interest calculator
3. `app/cbl/CBSTM03A.CBL` - Statement generation
4. `app/cbl/CBSTM03B.CBL` - Statement I/O
5. `app/cbl/CBTRN01C.cbl` - Transaction lookup
6. `app/cbl/CBTRN02C.cbl` - Transaction posting
7. `app/cbl/CBTRN03C.cbl` - Transaction report
8. `app/cbl/COACTUPC.cbl` - Account update (online)
9. `app/cbl/COACTVWC.cbl` - Account view (online)
10. `app/cbl/COBIL00C.cbl` - Bill pay (online)
11. `app/cbl/COCRDLIC.cbl` - Card list (online)
12. `app/cbl/COCRDSLC.cbl` - Card select (online)
13. `app/cbl/COCRDUPC.cbl` - Card update (online)
14. `app/cbl/COTRN02C.cbl` - Transaction entry (online)

### Copybooks with Account ID References

1. `app/cpy/COCOM01Y.cpy` - Communication area
2. `app/cpy/CVACT01Y.cpy` - Account record
3. `app/cpy/CVACT02Y.cpy` - Card record
4. `app/cpy/CVACT03Y.cpy` - Cross-reference record
5. `app/cpy/CVCRD01Y.cpy` - Card work areas
6. `app/cpy/CVTRA01Y.cpy` - Transaction category balance

### BMS Maps with Account ID References

1. `app/bms/COACTVW.bms` - Account view screen
2. `app/cpy-bms/COACTVW.CPY` - Account view generated copybook
3. `app/cpy-bms/COCRDLI.CPY` - Card list generated copybook

### JCL with Account ID Key Definitions

1. `app/jcl/ACCTFILE.jcl` - Account file definition
2. `app/jcl/CARDFILE.jcl` - Card file definition
3. `app/jcl/XREFFILE.jcl` - Cross-reference file definition
4. `app/jcl/TCATBALF.jcl` - Transaction category balance file
5. `app/jcl/PRTCATBL.jcl` - Category balance print

---

## Appendix B: Glossary

| Term | Definition |
|------|------------|
| ACCT-ID | Account Identifier - unique identifier for customer accounts |
| BMS | Basic Mapping Support - CICS screen definition facility |
| CICS | Customer Information Control System - IBM transaction server |
| FD | File Descriptor - COBOL file definition section |
| JCL | Job Control Language - mainframe batch job scripting |
| KSDS | Key Sequenced Data Set - indexed VSAM file organization |
| PIC | Picture clause - COBOL data type definition |
| VSAM | Virtual Storage Access Method - mainframe file access method |
| XREF | Cross-reference - lookup table linking related data |

---

*Document prepared for CardDemo Mainframe Modernization Project*  
*COG-GTM/mainframe-modernization-carddemo*
