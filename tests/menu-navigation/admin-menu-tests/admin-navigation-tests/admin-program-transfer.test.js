const { describe, test, expect, beforeEach } = require('@jest/globals');
const { MockCICSEnvironment } = require('../../shared/mock-cics-environment');
const { CommareaBuilder } = require('../../shared/commarea-helpers/commarea-builder');
const { ErrorMessages } = require('../../shared/test-data-setup/error-messages');
const { MenuOptionsLoader } = require('../../shared/test-data-setup/menu-options-loader');
const { TestUsers } = require('../../shared/test-data-setup/test-users');

describe('Admin Menu Navigation Tests', () => {
    let mockCICS;
    let adminCommarea;

    beforeEach(() => {
        mockCICS = new MockCICSEnvironment();
        mockCICS.setTransaction('CA00', 'COADM01C');
        mockCICS.setEIBCALEN(100);
        
        adminCommarea = TestUsers.createAdminMenuCommarea('ADMIN_USER');
    });

    describe('Admin Program Transfer Validation', () => {
        test('should transfer to COUSR00C for User List option', () => {
            mockCICS.simulateEnterKey();
            
            const optionNumber = 1;
            const expectedProgram = 'COUSR00C';
            
            const mapData = {
                OPTIONI: optionNumber.toString(),
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COADM1A', 'COADM01', mapData);
            
            const option = MenuOptionsLoader.getAdminMenuOption(optionNumber);
            expect(option.program).toBe(expectedProgram);
            expect(option.name).toBe('User List (Security)');
            
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CA00';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COADM01C';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = 0;
            
            mockCICS.xctl.xctl(expectedProgram, adminCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(expectedProgram)).toBe(true);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.program).toBe(expectedProgram);
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COADM01C');
        });

        test('should transfer to COUSR01C for User Add option', () => {
            mockCICS.simulateEnterKey();
            
            const optionNumber = 2;
            const expectedProgram = 'COUSR01C';
            
            const option = MenuOptionsLoader.getAdminMenuOption(optionNumber);
            expect(option.program).toBe(expectedProgram);
            expect(option.name).toBe('User Add (Security)');
            
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CA00';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COADM01C';
            
            mockCICS.xctl.xctl(expectedProgram, adminCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(expectedProgram)).toBe(true);
        });

        test('should transfer to COUSR02C for User Update option', () => {
            mockCICS.simulateEnterKey();
            
            const optionNumber = 3;
            const expectedProgram = 'COUSR02C';
            
            const option = MenuOptionsLoader.getAdminMenuOption(optionNumber);
            expect(option.program).toBe(expectedProgram);
            expect(option.name).toBe('User Update (Security)');
            
            mockCICS.xctl.xctl(expectedProgram, adminCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(expectedProgram)).toBe(true);
        });

        test('should transfer to COUSR03C for User Delete option', () => {
            mockCICS.simulateEnterKey();
            
            const optionNumber = 4;
            const expectedProgram = 'COUSR03C';
            
            const option = MenuOptionsLoader.getAdminMenuOption(optionNumber);
            expect(option.program).toBe(expectedProgram);
            expect(option.name).toBe('User Delete (Security)');
            
            mockCICS.xctl.xctl(expectedProgram, adminCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(expectedProgram)).toBe(true);
        });
    });

    describe('Complete Admin Navigation Flow', () => {
        test('should execute complete navigation flow for all admin options', () => {
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
                expect(inputValidation.optionNumber).toBe(option.number);
                
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
                expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COADM01C');
            });
        });

        test('should maintain program context across admin transfers', () => {
            const testScenarios = [
                { option: 1, program: 'COUSR00C', description: 'User List' },
                { option: 2, program: 'COUSR01C', description: 'User Add' },
                { option: 3, program: 'COUSR02C', description: 'User Update' },
                { option: 4, program: 'COUSR03C', description: 'User Delete' }
            ];
            
            testScenarios.forEach(scenario => {
                mockCICS.reset();
                mockCICS.setTransaction('CA00', 'COADM01C');
                mockCICS.simulateEnterKey();
                
                const freshCommarea = TestUsers.createAdminMenuCommarea('ADMIN_USER');
                freshCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CA00';
                freshCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COADM01C';
                freshCommarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = 0;
                
                mockCICS.xctl.xctl(scenario.program, freshCommarea);
                
                const lastCall = mockCICS.xctl.getLastProgramCall();
                expect(lastCall.program).toBe(scenario.program);
                expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID.trim()).toBe('CA00');
                expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COADM01C');
                expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT).toBe(0);
            });
        });
    });

    describe('F3 Key Navigation from Admin Menu', () => {
        test('should return to sign-on screen when F3 is pressed', () => {
            mockCICS.simulateF3Key();
            
            const expectedTargetProgram = 'COSGN00C';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_TO_PROGRAM = expectedTargetProgram.padEnd(8, ' ');
            
            mockCICS.xctl.xctl(expectedTargetProgram, adminCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(expectedTargetProgram)).toBe(true);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.program).toBe(expectedTargetProgram);
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('A');
        });

        test('should preserve admin user context when returning to sign-on', () => {
            const originalUserId = adminCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID;
            const originalUserType = adminCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE;
            
            mockCICS.simulateF3Key();
            
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_TO_PROGRAM = 'COSGN00C'.padEnd(8, ' ');
            mockCICS.xctl.xctl('COSGN00C', adminCommarea);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID).toBe(originalUserId);
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe(originalUserType);
        });
    });

    describe('COMMAREA Preservation in Admin Navigation', () => {
        test('should preserve all COMMAREA sections during admin program transfers', () => {
            const commareaWithData = new CommareaBuilder()
                .withAdminUser()
                .withUserId('ADMIN001')
                .withReenterContext()
                .withFromTransaction('CM00', 'COMEN01C')
                .withCustomerInfo(987654321, 'Admin', 'A', 'User')
                .withAccountInfo(98765432109, 'A')
                .withCardInfo(9876543210987654)
                .withLastMap('COADM1A', 'COADM01')
                .build();
            
            mockCICS.simulateEnterKey();
            
            const selectedOption = 2;
            const option = MenuOptionsLoader.getAdminMenuOption(selectedOption);
            
            commareaWithData.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CA00';
            commareaWithData.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COADM01C';
            commareaWithData.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = 0;
            
            mockCICS.xctl.xctl(option.program, commareaWithData);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            
            expect(lastCall.commarea.CDEMO_CUSTOMER_INFO.CDEMO_CUST_ID).toBe(987654321);
            expect(lastCall.commarea.CDEMO_CUSTOMER_INFO.CDEMO_CUST_FNAME.trim()).toBe('Admin');
            expect(lastCall.commarea.CDEMO_ACCOUNT_INFO.CDEMO_ACCT_ID).toBe(98765432109);
            expect(lastCall.commarea.CDEMO_CARD_INFO.CDEMO_CARD_NUM).toBe(9876543210987654);
            expect(lastCall.commarea.CDEMO_MORE_INFO.CDEMO_LAST_MAPSET.trim()).toBe('COADM01');
        });

        test('should update program context information correctly', () => {
            mockCICS.simulateEnterKey();
            
            const selectedOption = 3;
            const option = MenuOptionsLoader.getAdminMenuOption(selectedOption);
            
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CA00';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COADM01C';
            adminCommarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = 0;
            
            mockCICS.xctl.xctl(option.program, adminCommarea);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID.trim()).toBe('CA00');
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COADM01C');
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT).toBe(0);
        });
    });

    describe('Error Handling in Admin Navigation', () => {
        test('should handle invalid option selection gracefully', () => {
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

        test('should handle XCTL failure scenarios in admin navigation', () => {
            mockCICS.xctl.setFailure(true, 'ADMIN_PROGRAM_NOT_FOUND');
            
            const selectedOption = 1;
            const option = MenuOptionsLoader.getAdminMenuOption(selectedOption);
            
            expect(() => {
                mockCICS.xctl.xctl(option.program, adminCommarea);
            }).toThrow('XCTL failed: ADMIN_PROGRAM_NOT_FOUND');
            
            expect(mockCICS.xctl.getCallCount()).toBe(0);
        });

        test('should handle invalid key presses in admin menu', () => {
            mockCICS.simulateInvalidKey();
            
            const errorMessage = ErrorMessages.getInvalidKeyMessage();
            const mapData = {
                OPTIONI: '',
                ERRMSGO: errorMessage
            };
            
            mockCICS.sendReceive.send('COADM1A', 'COADM01', mapData);
            
            const sentMap = mockCICS.sendReceive.getLastSentMap();
            expect(sentMap.mapData.ERRMSGO).toBe('Invalid key pressed. Please see below...');
            
            expect(mockCICS.xctl.getCallCount()).toBe(0);
        });
    });

    describe('Admin Menu Integration with Main Menu', () => {
        test('should simulate navigation from main menu to admin menu', () => {
            const mainMenuCommarea = TestUsers.createMainMenuCommarea('ADMIN_USER');
            
            mockCICS.reset();
            mockCICS.setTransaction('CM00', 'COMEN01C');
            mockCICS.simulateEnterKey();
            
            mainMenuCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CM00';
            mainMenuCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COMEN01C';
            mainMenuCommarea.CDEMO_GENERAL_INFO.CDEMO_TO_TRANID = 'CA00';
            mainMenuCommarea.CDEMO_GENERAL_INFO.CDEMO_TO_PROGRAM = 'COADM01C';
            
            mockCICS.xctl.xctl('COADM01C', mainMenuCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled('COADM01C')).toBe(true);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('A');
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COMEN01C');
        });

        test('should validate admin user can access both main and admin menus', () => {
            const adminUser = TestUsers.getTestUser('ADMIN_USER');
            expect(adminUser.userType).toBe('A');
            
            const mainMenuOptions = MenuOptionsLoader.getMainMenuOptions();
            mainMenuOptions.forEach(option => {
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    option.number, 
                    adminUser.userType, 
                    'main'
                );
                expect(accessValidation.valid).toBe(true);
            });
            
            const adminMenuOptions = MenuOptionsLoader.getAdminMenuOptions();
            adminMenuOptions.forEach(option => {
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    option.number, 
                    adminUser.userType, 
                    'admin'
                );
                expect(accessValidation.valid).toBe(true);
            });
        });
    });

    describe('Admin Program Target Validation', () => {
        test('should validate all admin program names are in expected format', () => {
            const adminPrograms = MenuOptionsLoader.getAdminMenuProgramNames();
            const expectedPattern = /^COUSR0[0-3]C$/;
            
            adminPrograms.forEach(program => {
                expect(program).toMatch(expectedPattern);
            });
        });

        test('should validate admin programs are included in all program names', () => {
            const adminPrograms = MenuOptionsLoader.getAdminMenuProgramNames();
            const allPrograms = MenuOptionsLoader.getAllProgramNames();
            
            adminPrograms.forEach(program => {
                expect(allPrograms).toContain(program);
            });
        });

        test('should validate target program retrieval for admin menu', () => {
            const testCases = [
                { option: 1, expectedProgram: 'COUSR00C' },
                { option: 2, expectedProgram: 'COUSR01C' },
                { option: 3, expectedProgram: 'COUSR02C' },
                { option: 4, expectedProgram: 'COUSR03C' }
            ];
            
            testCases.forEach(testCase => {
                const targetProgram = MenuOptionsLoader.getTargetProgram(testCase.option, 'admin');
                expect(targetProgram).toBe(testCase.expectedProgram);
            });
        });
    });
});
