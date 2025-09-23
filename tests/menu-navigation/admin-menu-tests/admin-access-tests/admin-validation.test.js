const { describe, test, expect, beforeEach } = require('@jest/globals');
const { MockCICSEnvironment } = require('../../shared/mock-cics-environment');
const { CommareaBuilder } = require('../../shared/commarea-helpers/commarea-builder');
const { ErrorMessages } = require('../../shared/test-data-setup/error-messages');
const { MenuOptionsLoader } = require('../../shared/test-data-setup/menu-options-loader');
const { TestUsers } = require('../../shared/test-data-setup/test-users');

describe('Admin Menu Access Tests', () => {
    let mockCICS;
    let adminCommarea;

    beforeEach(() => {
        mockCICS = new MockCICSEnvironment();
        mockCICS.setTransaction('CA00', 'COADM01C');
        mockCICS.setEIBCALEN(100);
        
        adminCommarea = TestUsers.createAdminMenuCommarea('ADMIN_USER');
    });

    describe('Admin Menu Option Validation', () => {
        test('should validate admin menu option range (1-4)', () => {
            const adminMenuCount = MenuOptionsLoader.getAdminMenuCount();
            expect(adminMenuCount).toBe(4);
            
            expect(MenuOptionsLoader.isValidAdminMenuOption(1)).toBe(true);
            expect(MenuOptionsLoader.isValidAdminMenuOption(2)).toBe(true);
            expect(MenuOptionsLoader.isValidAdminMenuOption(3)).toBe(true);
            expect(MenuOptionsLoader.isValidAdminMenuOption(4)).toBe(true);
            
            expect(MenuOptionsLoader.isValidAdminMenuOption(0)).toBe(false);
            expect(MenuOptionsLoader.isValidAdminMenuOption(5)).toBe(false);
            expect(MenuOptionsLoader.isValidAdminMenuOption(99)).toBe(false);
        });

        test('should reject invalid admin menu options with proper error message', () => {
            const invalidOptions = ['0', '5', '99', '-1'];
            
            invalidOptions.forEach(option => {
                const validation = ErrorMessages.validateOptionInput(option, 'admin');
                expect(validation.valid).toBe(false);
                expect(validation.message).toBe('Please enter a valid option number...');
            });
        });

        test('should accept valid admin menu options', () => {
            const validOptions = ['1', '2', '3', '4'];
            
            validOptions.forEach(option => {
                const validation = ErrorMessages.validateOptionInput(option, 'admin');
                expect(validation.valid).toBe(true);
                expect(validation.message).toBe(null);
                expect(validation.optionNumber).toBe(parseInt(option, 10));
            });
        });

        test('should validate all admin menu options exist', () => {
            for (let i = 1; i <= 4; i++) {
                const option = MenuOptionsLoader.getAdminMenuOption(i);
                expect(option).not.toBe(null);
                expect(option.number).toBe(i);
                expect(option.name).toBeDefined();
                expect(option.program).toBeDefined();
            }
        });
    });

    describe('Admin-Only Access Requirements', () => {
        test('should allow admin user access to admin menu', () => {
            expect(adminCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('A');
            
            const builder = new CommareaBuilder();
            expect(builder.isAdminUser(adminCommarea)).toBe(true);
            expect(builder.isRegularUser(adminCommarea)).toBe(false);
        });

        test('should validate admin user can access all admin menu options', () => {
            const adminMenuOptions = MenuOptionsLoader.getAdminMenuOptions();
            
            adminMenuOptions.forEach(option => {
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    option.number, 
                    'A', 
                    'admin'
                );
                
                expect(accessValidation.valid).toBe(true);
                expect(accessValidation.message).toBe(null);
            });
        });

        test('should block regular user from admin menu access', () => {
            const regularUserCommarea = TestUsers.createAdminMenuCommarea('REGULAR_USER');
            expect(regularUserCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('U');
            
            const builder = new CommareaBuilder();
            expect(builder.isRegularUser(regularUserCommarea)).toBe(true);
            expect(builder.isAdminUser(regularUserCommarea)).toBe(false);
        });

        test('should validate admin user COMMAREA structure', () => {
            expect(adminCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID.trim()).toBe('ADMIN001');
            expect(adminCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('A');
            expect(adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COMEN01C');
            
            const builder = new CommareaBuilder();
            expect(() => builder.validateCommarea(adminCommarea)).not.toThrow();
        });
    });

    describe('Admin Menu Program Validation', () => {
        test('should validate all admin menu target programs', () => {
            const adminMenuOptions = MenuOptionsLoader.getAdminMenuOptions();
            const expectedPrograms = ['COUSR00C', 'COUSR01C', 'COUSR02C', 'COUSR03C'];
            
            adminMenuOptions.forEach((option, index) => {
                expect(option.program).toBe(expectedPrograms[index]);
            });
        });

        test('should validate admin menu option names', () => {
            const adminMenuOptions = MenuOptionsLoader.getAdminMenuOptions();
            const expectedNames = [
                'User List (Security)',
                'User Add (Security)',
                'User Update (Security)',
                'User Delete (Security)'
            ];
            
            adminMenuOptions.forEach((option, index) => {
                expect(option.name).toBe(expectedNames[index]);
            });
        });

        test('should get correct target program for each admin option', () => {
            expect(MenuOptionsLoader.getTargetProgram(1, 'admin')).toBe('COUSR00C');
            expect(MenuOptionsLoader.getTargetProgram(2, 'admin')).toBe('COUSR01C');
            expect(MenuOptionsLoader.getTargetProgram(3, 'admin')).toBe('COUSR02C');
            expect(MenuOptionsLoader.getTargetProgram(4, 'admin')).toBe('COUSR03C');
            
            expect(MenuOptionsLoader.getTargetProgram(0, 'admin')).toBe(null);
            expect(MenuOptionsLoader.getTargetProgram(5, 'admin')).toBe(null);
        });
    });

    describe('CICS Environment Integration', () => {
        test('should simulate admin menu option selection', () => {
            mockCICS.simulateEnterKey();
            
            const selectedOption = 1;
            const mapData = {
                OPTIONI: selectedOption.toString(),
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COADM1A', 'COADM01', mapData);
            
            const inputValidation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'admin');
            expect(inputValidation.valid).toBe(true);
            
            const option = MenuOptionsLoader.getAdminMenuOption(selectedOption);
            expect(option.program).toBe('COUSR00C');
            
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CA00';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COADM01C';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = 0;
            
            mockCICS.xctl.xctl(option.program, adminCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(option.program)).toBe(true);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.program).toBe(option.program);
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COADM01C');
        });

        test('should simulate error handling for invalid admin option', () => {
            mockCICS.simulateEnterKey();
            
            const invalidOption = '5';
            const mapData = {
                OPTIONI: invalidOption,
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COADM1A', 'COADM01', mapData);
            
            const inputValidation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'admin');
            expect(inputValidation.valid).toBe(false);
            expect(inputValidation.message).toBe('Please enter a valid option number...');
            
            mapData.ERRMSGO = inputValidation.message;
            mockCICS.sendReceive.send('COADM1A', 'COADM01', mapData);
            
            const sentMap = mockCICS.sendReceive.getLastSentMap();
            expect(sentMap.mapData.ERRMSGO).toBe('Please enter a valid option number...');
            
            expect(mockCICS.xctl.getCallCount()).toBe(0);
        });

        test('should simulate F3 key returning to sign-on screen', () => {
            mockCICS.simulateF3Key();
            
            const expectedTargetProgram = 'COSGN00C';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_TO_PROGRAM = expectedTargetProgram.padEnd(8, ' ');
            
            mockCICS.xctl.xctl(expectedTargetProgram, adminCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(expectedTargetProgram)).toBe(true);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.program).toBe(expectedTargetProgram);
            expect(lastCall.commarea).toEqual(adminCommarea);
        });
    });

    describe('Admin Menu Navigation Flow', () => {
        test('should navigate to all admin menu programs successfully', () => {
            const adminMenuOptions = MenuOptionsLoader.getAdminMenuOptions();
            
            adminMenuOptions.forEach(option => {
                mockCICS.reset();
                mockCICS.setTransaction('CA00', 'COADM01C');
                mockCICS.simulateEnterKey();
                
                const mapData = {
                    OPTIONI: option.number.toString(),
                    ERRMSGO: ''
                };
                
                mockCICS.sendReceive.receive('COADM1A', 'COADM01', mapData);
                
                const inputValidation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'admin');
                expect(inputValidation.valid).toBe(true);
                
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    option.number, 
                    'A', 
                    'admin'
                );
                expect(accessValidation.valid).toBe(true);
                
                const freshCommarea = TestUsers.createAdminMenuCommarea('ADMIN_USER');
                freshCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CA00';
                freshCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COADM01C';
                freshCommarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = 0;
                
                mockCICS.xctl.xctl(option.program, freshCommarea);
                
                expect(mockCICS.xctl.wasProgramCalled(option.program)).toBe(true);
                
                const lastCall = mockCICS.xctl.getLastProgramCall();
                expect(lastCall.program).toBe(option.program);
                expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('A');
            });
        });

        test('should preserve COMMAREA across admin program transfers', () => {
            const originalCommarea = JSON.parse(JSON.stringify(adminCommarea));
            
            mockCICS.simulateEnterKey();
            
            const selectedOption = 2;
            const option = MenuOptionsLoader.getAdminMenuOption(selectedOption);
            
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CA00';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COADM01C';
            
            mockCICS.xctl.xctl(option.program, adminCommarea);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID).toBe(originalCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID);
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe(originalCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE);
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COADM01C');
        });
    });

    describe('Error Handling and Validation', () => {
        test('should handle non-numeric input in admin menu', () => {
            const invalidInputs = ['ABC', '!@#', '1A', '2.5'];
            
            invalidInputs.forEach(input => {
                const validation = ErrorMessages.validateOptionInput(input, 'admin');
                expect(validation.valid).toBe(false);
                expect(validation.message).toBe('Please enter a valid option number...');
                expect(validation.errorType).toBe('NON_NUMERIC');
            });
        });

        test('should handle empty input in admin menu', () => {
            const emptyInputs = ['', '   ', null, undefined];
            
            emptyInputs.forEach(input => {
                const validation = ErrorMessages.validateOptionInput(input, 'admin');
                expect(validation.valid).toBe(false);
                expect(validation.message).toBe('Please enter a valid option number...');
                expect(validation.errorType).toBe('EMPTY_INPUT');
            });
        });

        test('should validate admin menu program names against expected list', () => {
            const adminMenuPrograms = MenuOptionsLoader.getAdminMenuProgramNames();
            const allProgramNames = MenuOptionsLoader.getAllProgramNames();
            
            adminMenuPrograms.forEach(program => {
                expect(mockCICS.xctl.validateTargetProgram(program, allProgramNames)).toBe(true);
            });
        });
    });

    describe('Test User Configuration for Admin Menu', () => {
        test('should validate admin user test configuration', () => {
            const adminUser = TestUsers.getTestUser('ADMIN_USER');
            expect(adminUser.userType).toBe('A');
            expect(adminUser.userId).toBe('ADMIN001');
            
            const validation = TestUsers.validateUserConfiguration('ADMIN_USER');
            expect(validation.valid).toBe(true);
        });

        test('should create proper admin menu COMMAREA', () => {
            const commarea = TestUsers.createAdminMenuCommarea('ADMIN_USER');
            
            expect(commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('A');
            expect(commarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID.trim()).toBe('ADMIN001');
            expect(commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COMEN01C');
            expect(commarea.CDEMO_MORE_INFO.CDEMO_LAST_MAPSET.trim()).toBe('COADM01');
        });
    });
});
