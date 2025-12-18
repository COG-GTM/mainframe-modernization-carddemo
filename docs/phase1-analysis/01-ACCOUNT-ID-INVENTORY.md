# Account ID Field Inventory - Phase 1 Analysis

## Document Information
- **Project**: CardDemo Mainframe Application
- **Repository**: COG-GTM/mainframe-modernization-carddemo
- **Analysis Date**: 2025-12-18
- **Current Format**: 11-digit numeric (PIC 9(11))
- **Target Format**: 14-digit alphanumeric

## Executive Summary

This document provides a comprehensive inventory of all locations where the Account ID field is used in the CardDemo mainframe application. The Account ID serves as a primary key in the ACCTDATA VSAM file and is used throughout the application for account identification, cross-reference lookups, and transaction processing.

**Total Occurrences Identified**: 85+ locations across 35+ files

## 1. Data Structure Files (File Descriptors)

These are the core file descriptor definitions that define how Account ID is stored in VSAM files.

| File | Line | Field Name | Definition | Purpose |
|------|------|------------|------------|---------|
| app/cbl/CBTRN01C.cbl | 88 | FD-ACCT-ID | PIC 9(11) | Account file record key for transaction posting |
| app/cbl/CBACT01C.cbl | 39 | FD-ACCT-ID | PIC 9(11) | Account file record key for account data printing |
| app/cbl/CBACT04C.cbl | 64 | FD-TRANCAT-ACCT-ID | PIC 9(11) | Transaction category file account ID |
| app/cbl/CBACT04C.cbl | 73 | FD-XREF-ACCT-ID | PIC 9(11) | Cross-reference file account ID |
| app/cbl/CBACT04C.cbl | 86 | FD-ACCT-ID | PIC 9(11) | Account file record key for interest calculation |
| app/cbl/CBTRN02C.cbl | 88 | FD-ACCT-ID | PIC 9(11) | Account file record key for transaction posting |
| app/cbl/CBTRN02C.cbl | 94 | FD-TRANCAT-ACCT-ID | PIC 9(11) | Transaction category account ID |
| app/cbl/CBSTM03B.CBL | 77 | FD-ACCT-ID | PIC 9(11) | Account file record key for statement generation |

## 2. Copybook Definitions

Copybooks are reusable data structure definitions included in multiple programs.

### 2.1 Core Data Record Copybooks

| File | Line | Field Name | Definition | Purpose |
|------|------|------------|------------|---------|
| app/cpy/CVACT01Y.cpy | 5 | ACCT-ID | PIC 9(11) | Account master record primary key (300-byte record) |
| app/cpy/CVACT02Y.cpy | 6 | CARD-ACCT-ID | PIC 9(11) | Card record account reference (150-byte record) |
| app/cpy/CVACT03Y.cpy | 7 | XREF-ACCT-ID | PIC 9(11) | Card cross-reference account ID (50-byte record) |
| app/cpy/CVTRA01Y.cpy | 6 | TRANCAT-ACCT-ID | PIC 9(11) | Transaction category balance account ID |

### 2.2 Communication Area Copybooks

| File | Line | Field Name | Definition | Purpose |
|------|------|------------|------------|---------|
| app/cpy/COCOM01Y.cpy | 38 | CDEMO-ACCT-ID | PIC 9(11) | Application commarea account ID for inter-program communication |

### 2.3 Working Storage Copybooks

| File | Line | Field Name | Definition | Purpose |
|------|------|------------|------------|---------|
| app/cpy/CVCRD01Y.cpy | 34 | CC-ACCT-ID | PIC X(11) | Credit card work area (alphanumeric) |
| app/cpy/CVCRD01Y.cpy | 36 | CC-ACCT-ID-N | PIC 9(11) | Credit card work area (numeric redefinition) |

### 2.4 Report Copybooks

| File | Line | Field Name | Definition | Purpose |
|------|------|------------|------------|---------|
| app/cpy/CVTRA07Y.cpy | 18 | TRAN-REPORT-ACCOUNT-ID | PIC X(11) | Transaction report account ID field |

## 3. Screen Interface Definitions (BMS Maps)

BMS (Basic Mapping Support) maps define the terminal screen layouts.

| File | Line | Field Name | Length | Attributes | Purpose |
|------|------|------------|--------|------------|---------|
| app/bms/COACTVW.bms | 84-90 | ACCTSID | 11 | PICIN='99999999999', MUSTFILL | Account view screen - account ID input |
| app/bms/COACTUP.bms | 86 | ACCTSID | 11 | UNPROT | Account update screen - account ID input |
| app/bms/COACTUP.bms | 315 | (second occurrence) | 11 | - | Account update screen - display field |
| app/bms/COTRN02.bms | 88 | ACTIDIN | 11 | UNPROT | Transaction add screen - account ID input |
| app/bms/COCRDSL.bms | 87 | ACCTSID | 11 | - | Card detail screen - account ID |
| app/bms/COCRDLI.bms | 92 | ACCTSID | 11 | - | Card list screen - account ID filter |
| app/bms/COCRDLI.bms | 150, 177, 204, 231, 258, 285, 312 | ACCTNO1-7 | 11 | - | Card list screen - account numbers in list rows |
| app/bms/COCRDUP.bms | 87 | ACCTSID | 11 | - | Card update screen - account ID |
| app/bms/COBIL00.bms | 88 | - | 11 | - | Billing screen - account ID |
| app/bms/COUSR01.bms | 81, 138 | - | 11 | - | User screen - account ID fields |
| app/bms/COUSR02.bms | 77, 100, 142 | - | 11 | - | User screen - account ID fields |
| app/bms/COUSR03.bms | 77, 100, 127 | - | 11 | - | User screen - account ID fields |

## 4. BMS Copybooks (Generated)

These are generated copybooks from BMS map compilation.

| File | Line | Field Name | Definition | Purpose |
|------|------|------------|------------|---------|
| app/cpy-bms/COACTVW.CPY | 60 | ACCTSIDI | PIC 99999999999 | Account view input field |
| app/cpy-bms/COACTVW.CPY | 284 | ACCTSIDO | PIC X(11) | Account view output field |
| app/cpy-bms/COCRDSL.CPY | 60 | ACCTSIDI | PIC X(11) | Card detail input field |
| app/cpy-bms/COCRDSL.CPY | 152 | ACCTSIDO | PIC X(11) | Card detail output field |
| app/cpy-bms/COCRDLI.CPY | 66 | ACCTSIDI | PIC X(11) | Card list account filter input |
| app/cpy-bms/COCRDLI.CPY | 84, 114, 144, 174, 204, 234, 264 | ACCTNO1I-7I | PIC X(11) | Card list row account inputs |
| app/cpy-bms/COCRDLI.CPY | 338 | ACCTSIDO | PIC X(11) | Card list account filter output |
| app/cpy-bms/COCRDLI.CPY | 356, 386, 416, 446, 476, 506, 536 | ACCTNO1O-7O | PIC X(11) | Card list row account outputs |
| app/cpy-bms/COACTUP.CPY | 60 | ACCTSIDI | PIC X(11) | Account update input field |
| app/cpy-bms/COACTUP.CPY | 386 | ACCTSIDO | PIC X(11) | Account update output field |
| app/cpy-bms/COCRDUP.CPY | 60 | ACCTSIDI | PIC X(11) | Card update input field |
| app/cpy-bms/COCRDUP.CPY | 164 | ACCTSIDO | PIC X(11) | Card update output field |

## 5. Validation Logic Programs

These programs contain validation logic that enforces the 11-digit numeric requirement.

### 5.1 COACTVWC.cbl (Account View)

| Line | Code Section | Current Logic |
|------|--------------|---------------|
| 78-80 | Working Storage | WS-CARD-RID-ACCT-ID PIC 9(11) with X(11) redefinition |
| 666-672 | 2210-EDIT-ACCOUNT | Validates CC-ACCT-ID IS NOT NUMERIC, displays error "Account Filter must be a non-zero 11 digit number" |

### 5.2 COCRDLIC.cbl (Card List)

| Line | Code Section | Current Logic |
|------|--------------|---------------|
| 99-101 | Working Storage | CARD-ACCT-ID-X PIC X(11), CARD-ACCT-ID-N PIC 9(11) |
| 139-141 | Working Storage | WS-CARD-RID-ACCT-ID PIC 9(11) with X(11) redefinition |
| 232, 235 | Working Storage | WS-CA-LAST-CARD-ACCT-ID, WS-CA-FIRST-CARD-ACCT-ID PIC 9(11) |
| 258 | Working Storage | WS-ROW-ACCTNO PIC X(11) |
| 1017-1022 | 2210-EDIT-ACCOUNT | Validates CC-ACCT-ID IS NOT NUMERIC, displays error "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER" |

### 5.3 COCRDSLC.cbl (Card Detail)

| Line | Code Section | Current Logic |
|------|--------------|---------------|
| 73-75 | Working Storage | CARD-ACCT-ID-X PIC X(11), CARD-ACCT-ID-N PIC 9(11) |
| 99-101 | Working Storage | WS-CARD-RID-ACCT-ID PIC 9(11) with X(11) redefinition |
| 665-670 | 2210-EDIT-ACCOUNT | Validates CC-ACCT-ID IS NOT NUMERIC, displays error "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER" |

### 5.4 COCRDUPC.cbl (Card Update)

| Line | Code Section | Current Logic |
|------|--------------|---------------|
| 104-106 | Working Storage | CARD-ACCT-ID-X PIC X(11), CARD-ACCT-ID-N PIC 9(11) |
| 130-132 | Working Storage | WS-CARD-RID-ACCT-ID PIC 9(11) with X(11) redefinition |
| 292, 304 | Working Storage | CCUP-OLD-ACCTID, CCUP-NEW-ACCTID PIC X(11) |
| 316 | Working Storage | CARD-UPDATE-ACCT-ID PIC 9(11) |
| 740-745 | 1210-EDIT-ACCOUNT | Validates CC-ACCT-ID IS NOT NUMERIC, displays error "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER" |

### 5.5 COACTUPC.cbl (Account Update)

| Line | Code Section | Current Logic |
|------|--------------|---------------|
| 358-360 | Working Storage | CUST-ACCT-ID-X PIC X(11), CUST-ACCT-ID-N PIC 9(11) |
| 381-383 | Working Storage | WS-CARD-RID-ACCT-ID PIC 9(11) with X(11) redefinition |
| 422 | Working Storage | ACCT-UPDATE-ID PIC 9(11) |
| 671-673 | Working Storage | ACUP-OLD-ACCT-ID-X PIC X(11), ACUP-OLD-ACCT-ID PIC 9(11) |
| 759-761 | Working Storage | ACUP-NEW-ACCT-ID-X PIC X(11), ACUP-NEW-ACCT-ID PIC 9(11) |
| 1787-1815 | Validation | Multiple validation checks for CC-ACCT-ID numeric requirement |

### 5.6 COTRN02C.cbl (Transaction Add)

| Line | Code Section | Current Logic |
|------|--------------|---------------|
| 55 | Working Storage | WS-ACCT-ID-N PIC 9(11) |

## 6. Working Storage Variables (Additional)

| File | Line | Field Name | Definition | Purpose |
|------|------|------------|------------|---------|
| app/cbl/CBACT04C.cbl | 167 | WS-LAST-ACCT-NUM | PIC X(11) | Last processed account number tracking |

## 7. VSAM File Definitions (JCL)

These JCL files define the VSAM file structures including key definitions.

| File | Line | Definition | Current Value | Purpose |
|------|------|------------|---------------|---------|
| app/jcl/ACCTFILE.jcl | 40 | KEYS | (11 0) | ACCTDATA primary key - 11 bytes at offset 0 |
| app/jcl/CARDFILE.jcl | 85 | KEYS | (11 16) | CARDDATA alternate index - 11 bytes at offset 16 |
| app/jcl/XREFFILE.jcl | 74 | KEYS | (11,25) | CARDXREF alternate index - 11 bytes at offset 25 |

## 8. Error Messages

Error messages that reference the 11-digit requirement need to be updated.

| File | Line | Current Message |
|------|------|-----------------|
| app/cbl/COACTVWC.cbl | 126-128 | "Account number must be a non zero 11 digit number" |
| app/cbl/COACTVWC.cbl | 672 | "Account Filter must be a non-zero 11 digit number" |
| app/cbl/COCRDLIC.cbl | 1022 | "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER" |
| app/cbl/COCRDSLC.cbl | 145-147 | "Account number must be a non zero 11 digit number" |
| app/cbl/COCRDSLC.cbl | 670 | "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER" |
| app/cbl/COCRDUPC.cbl | 190-192 | "Account number must be a non zero 11 digit number" |
| app/cbl/COCRDUPC.cbl | 745 | "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER" |

## 9. Data Files

| File | Format | Account ID Position | Sample Values |
|------|--------|---------------------|---------------|
| app/data/ASCII/acctdata.txt | Fixed-length 300 bytes | Bytes 1-11 | 00000000001 through 00000000050 |

## 10. File Dependencies

### 10.1 Copybook Usage Matrix

| Copybook | Used By Programs |
|----------|------------------|
| CVACT01Y.cpy (ACCOUNT-RECORD) | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A |
| CVACT02Y.cpy (CARD-RECORD) | COCRDLIC, COCRDSLC, COCRDUPC, CBTRN02C |
| CVACT03Y.cpy (CARD-XREF-RECORD) | COACTVWC, COTRN02C, CBTRN01C, CBTRN02C, CBSTM03A |
| COCOM01Y.cpy (CARDDEMO-COMMAREA) | All online CICS programs |
| CVCRD01Y.cpy (CC-WORK-AREA) | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC |
| CVTRA01Y.cpy (TRAN-CAT-BAL-RECORD) | CBACT04C, CBTRN02C |

### 10.2 BMS Map to Copybook Relationships

| BMS Map | Generated Copybook | Using Programs |
|---------|-------------------|----------------|
| COACTVW.bms | COACTVW.CPY | COACTVWC.cbl |
| COACTUP.bms | COACTUP.CPY | COACTUPC.cbl |
| COCRDLI.bms | COCRDLI.CPY | COCRDLIC.cbl |
| COCRDSL.bms | COCRDSL.CPY | COCRDSLC.cbl |
| COCRDUP.bms | COCRDUP.CPY | COCRDUPC.cbl |
| COTRN02.bms | COTRN02.CPY | COTRN02C.cbl |

## 11. Impact Summary by Category

| Category | File Count | Occurrence Count | Complexity |
|----------|------------|------------------|------------|
| File Descriptors (FD) | 6 | 8 | High - affects file I/O |
| Copybooks | 7 | 12 | High - affects multiple programs |
| BMS Maps | 10 | 25+ | Medium - screen layout changes |
| BMS Copybooks | 5 | 20+ | Medium - regenerated from BMS |
| Validation Logic | 6 | 15+ | High - business logic changes |
| Working Storage | 6 | 20+ | Medium - variable definitions |
| JCL/VSAM | 3 | 3 | High - file structure changes |
| Error Messages | 4 | 7 | Low - text updates |
| Data Files | 1 | 50 records | High - data migration required |

## 12. Recommended Change Order

1. **Phase 2a - Copybooks**: Update core copybooks first (CVACT01Y, CVACT02Y, CVACT03Y, COCOM01Y, CVCRD01Y)
2. **Phase 2b - BMS Maps**: Update screen definitions and regenerate BMS copybooks
3. **Phase 2c - File Descriptors**: Update FD definitions in batch programs
4. **Phase 2d - Validation Logic**: Remove numeric validation, update error messages
5. **Phase 2e - Working Storage**: Update all working storage variables
6. **Phase 2f - JCL/VSAM**: Update VSAM key definitions
7. **Phase 3 - Data Migration**: Convert existing data to new format
8. **Phase 4 - Testing**: Comprehensive testing of all affected components
