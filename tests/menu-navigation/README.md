# CardDemo Menu Navigation Test Suite

This test suite provides comprehensive automated testing for the CardDemo mainframe application's menu navigation system, covering both the Main Menu (CM00/COMEN01C) and Admin Menu (CA00/COADM01C) transaction flows.

## Overview

The test framework validates:
- **Input validation**: Option number validation, non-numeric input handling, empty input validation
- **Access control**: User type restrictions and admin-only function protection
- **Navigation flows**: F3 key functionality, ENTER key processing, program transfers
- **Screen rendering**: Menu display, header information, error message positioning

## Directory Structure

```
tests/menu-navigation/
├── main-menu-tests/
│   ├── input-validation-tests/
│   ├── access-control-tests/
│   └── navigation-flow-tests/
├── admin-menu-tests/
│   ├── admin-access-tests/
│   └── admin-navigation-tests/
└── shared/
    ├── commarea-helpers/
    ├── mock-cics-environment/
    └── test-data-setup/
```

## Test Coverage

### Main Menu (COMEN01C) - 10 Options
1. Account View → COACTVWC
2. Account Update → COACTUPC
3. Credit Card List → COCRDLIC
4. Credit Card View → COCRDSLC
5. Credit Card Update → COCRDUPC
6. Transaction List → COTRN00C
7. Transaction View → COTRN01C
8. Transaction Add → COTRN02C
9. Transaction Reports → CORPT00C
10. Bill Payment → COBIL00C

### Admin Menu (COADM01C) - 4 Options
1. User List (Security) → COUSR00C
2. User Add (Security) → COUSR01C
3. User Update (Security) → COUSR02C
4. User Delete (Security) → COUSR03C

## Setup and Execution

### Prerequisites
- Node.js (v14 or higher)
- npm or yarn package manager

### Installation
```bash
cd tests/menu-navigation
npm install
```

### Running Tests
```bash
# Run all tests
npm test

# Run specific test suites
npm run test:main-menu
npm run test:admin-menu
npm run test:input-validation
npm run test:access-control
npm run test:navigation

# Run with coverage
npm run test:coverage
```

## Test Scenarios

### Input Validation Tests
- Invalid option numbers (0, 99, -1) → "Please enter a valid option number..."
- Non-numeric input ('ABC', special characters) → Validation errors
- Empty input → Validation errors
- Valid range checking (Main: 1-10, Admin: 1-4)

### Access Control Tests
- Regular users (CDEMO-USRTYP-USER) blocked from admin functions
- Admin users (CDEMO-USRTYP-ADMIN) have full access
- Proper error messages for unauthorized access

### Navigation Flow Tests
- F3 key returns to COSGN00C (sign-on screen)
- ENTER key with valid options navigates to target programs
- XCTL program transfers with correct program names
- COMMAREA preservation across program transitions

### Screen Rendering Tests
- Menu options display correctly with proper numbering
- Header information (date, time, program name, transaction ID)
- Field positioning and attributes validation
- Error message display in correct screen positions

## Mock Environment

The test suite includes a comprehensive mock CICS environment that simulates:
- **XCTL commands**: Program transfers and target program validation
- **SEND/RECEIVE commands**: BMS map operations for screen handling
- **CICS response codes**: Various response scenarios and error conditions

## COMMAREA Helpers

Utilities for creating and manipulating test data:
- Valid COMMAREA structures with different user types
- COMMAREA preservation validation
- Program context manipulation (ENTER vs REENTER scenarios)

## Key Files Referenced

- **Main Menu Program**: `app/cbl/COMEN01C.cbl`
- **Admin Menu Program**: `app/cbl/COADM01C.cbl`
- **Menu Options**: `app/cpy/COMEN02Y.cpy`, `app/cpy/COADM02Y.cpy`
- **COMMAREA Structure**: `app/cpy/COCOM01Y.cpy`
- **Error Messages**: `app/cpy/CSMSG01Y.cpy`
- **BMS Maps**: `app/bms/COMEN01.bms`, `app/bms/COADM01.bms`

## Contributing

When adding new tests:
1. Follow the existing directory structure
2. Use the shared utilities for COMMAREA and mock CICS operations
3. Include both positive and negative test cases
4. Document any new test scenarios in this README

## JIRA Reference

This test suite was created for JIRA ticket: MBA-216
