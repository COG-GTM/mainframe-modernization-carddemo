# CICS CC00 Login Transaction Test Suite

This directory contains a comprehensive automated test suite for the CICS login transaction CC00 in the CardDemo mainframe application.

## Overview

The test framework simulates CICS terminal interactions, validates BMS screen fields, tests VSAM file operations, and verifies COMMAREA data transfer between programs without requiring an actual mainframe environment.

## Directory Structure

```
tests/
├── fixtures/usrsec_test_data.jcl    # Test data setup for USRSEC file
├── framework/cics_simulator.py      # Core CICS simulation framework
├── scenarios/                       # Test scenario implementations
│   ├── valid_login_tests.py        # Valid login test cases
│   ├── invalid_login_tests.py      # Invalid login test cases
│   └── edge_case_tests.py          # Edge case and boundary tests
└── validation/                      # Validation utilities
    ├── screen_validators.py         # BMS screen field validation
    └── commarea_validators.py       # COMMAREA data validation
```

## Test Framework Components

### CICS Simulator (`framework/cics_simulator.py`)

The core simulation framework that emulates:
- CICS terminal interactions and BMS field handling
- VSAM file operations with proper RESP code simulation
- Screen rendering with cursor positioning and field attributes
- COMMAREA data transfer between programs
- Function key handling (ENTER, F3, CLEAR)

### Validation Components

#### Screen Validators (`validation/screen_validators.py`)
- Validates BMS field attributes and positioning
- Checks cursor positioning (-1 values) for error scenarios
- Verifies error message display at position (23,1) with RED color
- Validates field length constraints and screen layout

#### COMMAREA Validators (`validation/commarea_validators.py`)
- Validates COMMAREA structure and field population
- Checks user authentication and type determination
- Verifies program transfer logic (COADM01C for admin, COMEN01C for regular users)
- Validates field lengths and data integrity

## Test Scenarios

### Valid Login Tests (`scenarios/valid_login_tests.py`)
- **Admin User Login**: ADMIN001/PASSWORD → COADM01C with CDEMO-USRTYP-ADMIN flag
- **Regular User Login**: USER0001/PASSWORD → COMEN01C with CDEMO-USRTYP-USER flag
- **Case Insensitive Login**: Tests various case combinations
- **COMMAREA Field Population**: Validates all required fields are set correctly
- **User Type Determination**: Verifies correct user type assignment

### Invalid Login Tests (`scenarios/invalid_login_tests.py`)
- **Empty USERID**: Verifies "Please enter User ID ..." message with cursor on USERID field
- **Empty PASSWORD**: Verifies "Please enter Password ..." message with cursor on PASSWD field
- **Invalid User**: Tests INVALID01/PASSWORD → "User not found. Try again ..." with RESP code 13
- **Wrong Password**: Tests USER0001/WRONGPWD → "Wrong Password. Try again ..." message
- **Cursor Positioning**: Validates cursor moves to correct fields on errors

### Edge Case Tests (`scenarios/edge_case_tests.py`)
- **F3 Key Functionality**: "Thank you for using CardDemo application" message
- **Invalid AID Keys**: Proper handling of unsupported keys
- **Maximum Length Input**: 8-character USERID/PASSWORD validation
- **Special Characters**: Testing non-alphanumeric characters
- **Leading/Trailing Spaces**: Space handling and trimming
- **Case Sensitivity**: Comprehensive case variation testing
- **Performance Testing**: Multiple login scenarios under load

## Test Data

### USRSEC Test Users
The test framework includes the following predefined users:

| User ID   | Password | Type | Target Program |
|-----------|----------|------|----------------|
| ADMIN001  | PASSWORD | A    | COADM01C       |
| USER0001  | PASSWORD | U    | COMEN01C       |
| TESTUSER  | TESTPASS | U    | COMEN01C       |
| EDGECASE  | 12345678 | U    | COMEN01C       |

### Test Data Isolation
- Each test uses isolated VSAM file simulation
- No interference between test executions
- Cleanup procedures ensure consistent test environment

## Running the Tests

### Prerequisites
- Python 3.7 or higher
- No external dependencies required (uses only Python standard library)

### Execute All Tests
```bash
# From the repository root
python -m pytest tests/ -v

# Or run individual test modules
python -m unittest tests.scenarios.valid_login_tests
python -m unittest tests.scenarios.invalid_login_tests
python -m unittest tests.scenarios.edge_case_tests
```

### Execute Specific Test Categories
```bash
# Valid login scenarios only
python -m unittest tests.scenarios.valid_login_tests.ValidLoginTests

# Invalid login scenarios only
python -m unittest tests.scenarios.invalid_login_tests.InvalidLoginTests

# Edge cases only
python -m unittest tests.scenarios.edge_case_tests.EdgeCaseTests
```

### Performance Requirements
- Each test scenario completes within 30 seconds
- Average login simulation time under 1 second
- Full test suite execution under 5 minutes

## Key Validation Points

### Authentication Flow
1. **Input Validation**: Empty field detection and error messaging
2. **VSAM File Operations**: User lookup with proper RESP code handling
3. **Password Verification**: Case-sensitive password matching
4. **User Type Determination**: Admin (A) vs Regular (U) user classification
5. **Program Transfer**: Correct XCTL to COADM01C or COMEN01C

### Screen Interaction
1. **BMS Field Attributes**: Proper field positioning and colors
2. **Cursor Positioning**: -1 values for error field focus
3. **Error Message Display**: RED color at position (23,1) with 78-character length
4. **Field Length Constraints**: 8-character limits for USERID/PASSWD

### COMMAREA Data Transfer
1. **Field Population**: All required fields properly set
2. **Data Integrity**: Correct user ID, type, transaction origin
3. **Program Context**: CDEMO-PGM-CONTEXT initialized to zeros
4. **Transfer Information**: FROM-TRANID=CC00, FROM-PROGRAM=COSGN00C

## Extending the Framework

### Adding New Test Users
Modify the `_setup_test_data()` method in `CICSSimulator` class:

```python
self.records['NEWUSER1'] = {
    'SEC-USR-ID': 'NEWUSER1',
    'SEC-USR-FNAME': 'NEW',
    'SEC-USR-LNAME': 'USER',
    'SEC-USR-PWD': 'NEWPASS',
    'SEC-USR-TYPE': 'U',
    'SEC-USR-FILLER': ' ' * 23
}
```

### Adding New Test Scenarios
Create new test methods in the appropriate test class:

```python
def test_new_scenario(self):
    result = self.simulator.simulate_login_transaction('USERID', 'PASSWORD')
    self.assertTrue(result['success'])
    # Add specific validations
```

### Testing Other CICS Transactions
The framework can be extended to test other CICS transactions by:
1. Creating new simulator classes for different BMS maps
2. Implementing transaction-specific validation logic
3. Adding new test scenario modules

## Integration with CI/CD

The test suite is designed to integrate with continuous integration pipelines:
- No external dependencies or mainframe connectivity required
- Fast execution suitable for automated testing
- Comprehensive coverage of authentication scenarios
- Clear pass/fail criteria with detailed error reporting

## Troubleshooting

### Common Issues
1. **Test Timeouts**: Check that individual tests complete within 30 seconds
2. **Validation Failures**: Review error messages for specific validation criteria
3. **COMMAREA Issues**: Verify field names match COCOM01Y.cpy structure
4. **Screen Validation**: Ensure BMS field positions match COSGN00.bms definitions

### Debug Mode
Enable detailed logging for troubleshooting:

```python
import logging
logging.basicConfig(level=logging.DEBUG)
```

## References

### Source Files
- **Login Program**: `app/cbl/COSGN00C.cbl`
- **BMS Screen Definition**: `app/bms/COSGN00.bms`
- **BMS Field Structure**: `app/cpy-bms/COSGN00.CPY`
- **COMMAREA Structure**: `app/cpy/COCOM01Y.cpy`
- **User Security Record**: `app/cpy/CSUSR01Y.cpy`
- **Test Data Setup**: `app/jcl/DUSRSECJ.jcl`

### CICS Concepts
- **BMS (Basic Mapping Support)**: Screen definition and field management
- **VSAM (Virtual Storage Access Method)**: File access and record management
- **COMMAREA**: Communication area for data transfer between programs
- **XCTL**: Transfer control between CICS programs
- **AID Keys**: Attention identifier keys (ENTER, PF3, etc.)

This test framework provides comprehensive coverage of the CC00 login transaction while maintaining the flexibility to extend testing to other CICS transactions in the CardDemo application.
