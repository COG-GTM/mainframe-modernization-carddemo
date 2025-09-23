const { describe, test, expect, beforeEach } = require('@jest/globals');
const { MockCICSEnvironment } = require('../../shared/mock-cics-environment');
const { CommareaBuilder } = require('../../shared/commarea-helpers/commarea-builder');
const { ErrorMessages } = require('../../shared/test-data-setup/error-messages');
const { MenuOptionsLoader } = require('../../shared/test-data-setup/menu-options-loader');

describe('Main Menu Input Validation Tests', () => {
    let mockCICS;
    let testCommarea;

    beforeEach(() => {
        mockCICS = new MockCICSEnvironment();
        mockCICS.setTransaction('CM00', 'COMEN01C');
        mockCICS.setEIBCALEN(100);
        
        testCommarea = new CommareaBuilder()
            .withRegularUser()
            .withUserId('USER0001')
            .withReenterContext()
            .build();
    });

    describe('Invalid Option Numbers', () => {
        test('should reject option 0 with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('0', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('OUT_OF_RANGE');
        });

        test('should reject option 99 with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('99', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('OUT_OF_RANGE');
        });

        test('should reject negative option -1 with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('-1', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('NON_NUMERIC');
        });

        test('should reject option 11 (above maximum) with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('11', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('OUT_OF_RANGE');
        });
    });

    describe('Non-Numeric Input', () => {
        test('should reject alphabetic input ABC with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('ABC', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('NON_NUMERIC');
        });

        test('should reject special characters !@# with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('!@#', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('NON_NUMERIC');
        });

        test('should reject mixed alphanumeric input 1A with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('1A', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('NON_NUMERIC');
        });

        test('should reject decimal input 1.5 with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('1.5', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('NON_NUMERIC');
        });
    });

    describe('Empty Input', () => {
        test('should reject empty string with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('EMPTY_INPUT');
        });

        test('should reject whitespace-only input with validation error', () => {
            const validation = ErrorMessages.validateOptionInput('   ', 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('EMPTY_INPUT');
        });

        test('should reject null input with validation error', () => {
            const validation = ErrorMessages.validateOptionInput(null, 'main');
            
            expect(validation.valid).toBe(false);
            expect(validation.message).toBe('Please enter a valid option number...');
            expect(validation.errorType).toBe('EMPTY_INPUT');
        });
    });

    describe('Valid Range Checking', () => {
        test('should accept option 1 as valid', () => {
            const validation = ErrorMessages.validateOptionInput('1', 'main');
            
            expect(validation.valid).toBe(true);
            expect(validation.message).toBe(null);
            expect(validation.errorType).toBe(null);
            expect(validation.optionNumber).toBe(1);
        });

        test('should accept option 5 as valid', () => {
            const validation = ErrorMessages.validateOptionInput('5', 'main');
            
            expect(validation.valid).toBe(true);
            expect(validation.message).toBe(null);
            expect(validation.errorType).toBe(null);
            expect(validation.optionNumber).toBe(5);
        });

        test('should accept option 10 (maximum) as valid', () => {
            const validation = ErrorMessages.validateOptionInput('10', 'main');
            
            expect(validation.valid).toBe(true);
            expect(validation.message).toBe(null);
            expect(validation.errorType).toBe(null);
            expect(validation.optionNumber).toBe(10);
        });

        test('should accept option with leading/trailing spaces', () => {
            const validation = ErrorMessages.validateOptionInput(' 5 ', 'main');
            
            expect(validation.valid).toBe(true);
            expect(validation.message).toBe(null);
            expect(validation.errorType).toBe(null);
            expect(validation.optionNumber).toBe(5);
        });
    });

    describe('Menu Option Count Validation', () => {
        test('should validate against correct main menu option count', () => {
            const menuCount = MenuOptionsLoader.getMainMenuCount();
            expect(menuCount).toBe(10);
            
            expect(MenuOptionsLoader.isValidMainMenuOption(1)).toBe(true);
            expect(MenuOptionsLoader.isValidMainMenuOption(10)).toBe(true);
            expect(MenuOptionsLoader.isValidMainMenuOption(0)).toBe(false);
            expect(MenuOptionsLoader.isValidMainMenuOption(11)).toBe(false);
        });

        test('should validate all main menu options exist', () => {
            for (let i = 1; i <= 10; i++) {
                const option = MenuOptionsLoader.getMainMenuOption(i);
                expect(option).not.toBe(null);
                expect(option.number).toBe(i);
                expect(option.name).toBeDefined();
                expect(option.program).toBeDefined();
                expect(option.userType).toBeDefined();
            }
        });
    });

    describe('CICS Environment Integration', () => {
        test('should simulate option validation in CICS environment', () => {
            mockCICS.simulateEnterKey();
            
            const mapData = {
                OPTIONI: '5',
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COMEN1A', 'COMEN01', mapData);
            
            const validation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'main');
            expect(validation.valid).toBe(true);
            
            const receivedMap = mockCICS.sendReceive.getLastReceivedMap();
            expect(receivedMap.mapName).toBe('COMEN1A');
            expect(receivedMap.inputData.OPTIONI).toBe('5');
        });

        test('should simulate error message display for invalid option', () => {
            mockCICS.simulateEnterKey();
            
            const mapData = {
                OPTIONI: 'ABC',
                ERRMSGO: ''
            };
            
            const validation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'main');
            expect(validation.valid).toBe(false);
            
            mapData.ERRMSGO = validation.message;
            mockCICS.sendReceive.send('COMEN1A', 'COMEN01', mapData);
            
            const sentMap = mockCICS.sendReceive.getLastSentMap();
            expect(sentMap.mapData.ERRMSGO).toBe('Please enter a valid option number...');
        });
    });

    describe('Comprehensive Test Scenarios', () => {
        test('should run all invalid input test scenarios', () => {
            const scenarios = ErrorMessages.getTestScenarios();
            
            scenarios.invalidInputs.forEach(scenario => {
                const validation = ErrorMessages.validateOptionInput(scenario.input, 'main');
                expect(validation.valid).toBe(false);
                expect(validation.errorType).toBe(scenario.expectedError);
                expect(validation.message).toBe('Please enter a valid option number...');
            });
        });

        test('should run all valid input test scenarios', () => {
            const scenarios = ErrorMessages.getTestScenarios();
            
            scenarios.validInputs.filter(s => s.menuType === 'main').forEach(scenario => {
                const validation = ErrorMessages.validateOptionInput(scenario.input, 'main');
                expect(validation.valid).toBe(true);
                expect(validation.errorType).toBe(null);
                expect(validation.message).toBe(null);
            });
        });
    });
});
