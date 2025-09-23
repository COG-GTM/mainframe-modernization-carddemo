const { describe, test, expect, beforeEach } = require('@jest/globals');
const { MockCICSEnvironment } = require('../../shared/mock-cics-environment');
const { CommareaBuilder } = require('../../shared/commarea-helpers/commarea-builder');
const { ErrorMessages } = require('../../shared/test-data-setup/error-messages');
const { MenuOptionsLoader } = require('../../shared/test-data-setup/menu-options-loader');
const { TestUsers } = require('../../shared/test-data-setup/test-users');

describe('Main Menu Navigation Flow Tests', () => {
    let mockCICS;
    let testCommarea;

    beforeEach(() => {
        mockCICS = new MockCICSEnvironment();
        mockCICS.setTransaction('CM00', 'COMEN01C');
        mockCICS.setEIBCALEN(100);
        
        testCommarea = TestUsers.createMainMenuCommarea('REGULAR_USER');
    });

    describe('F3 Key Functionality', () => {
        test('should return to COSGN00C when F3 key is pressed', () => {
            mockCICS.simulateF3Key();
            
            const expectedTargetProgram = 'COSGN00C';
            
            testCommarea.CDEMO_GENERAL_INFO.CDEMO_TO_PROGRAM = expectedTargetProgram.padEnd(8, ' ');
            
            mockCICS.xctl.xctl(expectedTargetProgram, testCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(expectedTargetProgram)).toBe(true);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.program).toBe(expectedTargetProgram);
            expect(lastCall.commarea).toEqual(testCommarea);
        });

        test('should preserve COMMAREA when returning to sign-on screen', () => {
            mockCICS.simulateF3Key();
            
            const originalCommarea = JSON.parse(JSON.stringify(testCommarea));
            
            mockCICS.xctl.xctl('COSGN00C', testCommarea);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.commarea).toEqual(originalCommarea);
        });

        test('should handle F3 key from different program contexts', () => {
            const enterContextCommarea = new CommareaBuilder()
                .withRegularUser()
                .withUserId('USER0001')
                .withEnterContext()
                .build();
            
            mockCICS.simulateF3Key();
            mockCICS.xctl.xctl('COSGN00C', enterContextCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled('COSGN00C')).toBe(true);
            
            const reenterContextCommarea = new CommareaBuilder()
                .withRegularUser()
                .withUserId('USER0001')
                .withReenterContext()
                .build();
            
            mockCICS.reset();
            mockCICS.setTransaction('CM00', 'COMEN01C');
            mockCICS.simulateF3Key();
            mockCICS.xctl.xctl('COSGN00C', reenterContextCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled('COSGN00C')).toBe(true);
        });
    });

    describe('ENTER Key Processing', () => {
        test('should navigate to target program for valid option 1', () => {
            mockCICS.simulateEnterKey();
            
            const optionNumber = 1;
            const mapData = {
                OPTIONI: optionNumber.toString(),
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COMEN1A', 'COMEN01', mapData);
            
            const inputValidation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'main');
            expect(inputValidation.valid).toBe(true);
            
            const option = MenuOptionsLoader.getMainMenuOption(optionNumber);
            expect(option).not.toBe(null);
            expect(option.program).toBe('COACTVWC');
            
            testCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CM00';
            testCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COMEN01C';
            testCommarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = 0;
            
            mockCICS.xctl.xctl(option.program, testCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(option.program)).toBe(true);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.program).toBe(option.program);
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COMEN01C');
        });

        test('should navigate to target program for valid option 5', () => {
            mockCICS.simulateEnterKey();
            
            const optionNumber = 5;
            const option = MenuOptionsLoader.getMainMenuOption(optionNumber);
            expect(option.program).toBe('COCRDUPC');
            
            const mapData = {
                OPTIONI: optionNumber.toString(),
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COMEN1A', 'COMEN01', mapData);
            
            const inputValidation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'main');
            expect(inputValidation.valid).toBe(true);
            
            mockCICS.xctl.xctl(option.program, testCommarea);
            
            expect(mockCICS.xctl.wasProgramCalled(option.program)).toBe(true);
        });

        test('should display error message for invalid option', () => {
            mockCICS.simulateEnterKey();
            
            const invalidOption = '99';
            const mapData = {
                OPTIONI: invalidOption,
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COMEN1A', 'COMEN01', mapData);
            
            const inputValidation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'main');
            expect(inputValidation.valid).toBe(false);
            expect(inputValidation.message).toBe('Please enter a valid option number...');
            
            mapData.ERRMSGO = inputValidation.message;
            mockCICS.sendReceive.send('COMEN1A', 'COMEN01', mapData);
            
            const sentMap = mockCICS.sendReceive.getLastSentMap();
            expect(sentMap.mapData.ERRMSGO).toBe('Please enter a valid option number...');
            
            expect(mockCICS.xctl.getCallCount()).toBe(0);
        });
    });

    describe('XCTL Program Transfers', () => {
        test('should validate all main menu target programs', () => {
            const mainMenuOptions = MenuOptionsLoader.getMainMenuOptions();
            const expectedPrograms = [
                'COACTVWC', 'COACTUPC', 'COCRDLIC', 'COCRDSLC', 'COCRDUPC',
                'COTRN00C', 'COTRN01C', 'COTRN02C', 'CORPT00C', 'COBIL00C'
            ];
            
            mainMenuOptions.forEach((option, index) => {
                expect(option.program).toBe(expectedPrograms[index]);
            });
        });

        test('should transfer to correct program for each menu option', () => {
            const mainMenuOptions = MenuOptionsLoader.getMainMenuOptions();
            
            mainMenuOptions.forEach(option => {
                mockCICS.reset();
                mockCICS.setTransaction('CM00', 'COMEN01C');
                mockCICS.simulateEnterKey();
                
                const mapData = {
                    OPTIONI: option.number.toString(),
                    ERRMSGO: ''
                };
                
                mockCICS.sendReceive.receive('COMEN1A', 'COMEN01', mapData);
                
                const inputValidation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'main');
                expect(inputValidation.valid).toBe(true);
                
                const userType = testCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE;
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    option.number, 
                    userType, 
                    'main'
                );
                
                if (accessValidation.valid) {
                    mockCICS.xctl.xctl(option.program, testCommarea);
                    expect(mockCICS.xctl.wasProgramCalled(option.program)).toBe(true);
                    
                    const lastCall = mockCICS.xctl.getLastProgramCall();
                    expect(lastCall.program).toBe(option.program);
                }
            });
        });

        test('should validate target program names against expected list', () => {
            const allProgramNames = MenuOptionsLoader.getAllProgramNames();
            const mainMenuPrograms = MenuOptionsLoader.getMainMenuProgramNames();
            
            const expectedMainMenuPrograms = [
                'COACTVWC', 'COACTUPC', 'COCRDLIC', 'COCRDSLC', 'COCRDUPC',
                'COTRN00C', 'COTRN01C', 'COTRN02C', 'CORPT00C', 'COBIL00C'
            ];
            
            expect(mainMenuPrograms).toEqual(expectedMainMenuPrograms);
            
            mainMenuPrograms.forEach(program => {
                expect(mockCICS.xctl.validateTargetProgram(program, allProgramNames)).toBe(true);
            });
        });

        test('should handle XCTL failure scenarios', () => {
            mockCICS.xctl.setFailure(true, 'PROGRAM_NOT_FOUND');
            
            expect(() => {
                mockCICS.xctl.xctl('INVALID_PROGRAM', testCommarea);
            }).toThrow('XCTL failed: PROGRAM_NOT_FOUND');
            
            expect(mockCICS.xctl.getCallCount()).toBe(0);
        });
    });

    describe('COMMAREA Preservation', () => {
        test('should preserve COMMAREA data across program transitions', () => {
            const originalCommarea = JSON.parse(JSON.stringify(testCommarea));
            
            mockCICS.simulateEnterKey();
            
            const optionNumber = 3;
            const option = MenuOptionsLoader.getMainMenuOption(optionNumber);
            
            testCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CM00';
            testCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COMEN01C';
            
            mockCICS.xctl.xctl(option.program, testCommarea);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID).toBe(originalCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID);
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe(originalCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE);
        });

        test('should update program context information in COMMAREA', () => {
            mockCICS.simulateEnterKey();
            
            const optionNumber = 2;
            const option = MenuOptionsLoader.getMainMenuOption(optionNumber);
            
            testCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CM00';
            testCommarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COMEN01C';
            testCommarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = 0;
            
            mockCICS.xctl.xctl(option.program, testCommarea);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID.trim()).toBe('CM00');
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM.trim()).toBe('COMEN01C');
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT).toBe(0);
        });

        test('should maintain customer and account information across transfers', () => {
            const commareaWithData = new CommareaBuilder()
                .withRegularUser()
                .withUserId('USER0001')
                .withReenterContext()
                .withCustomerInfo(123456789, 'John', 'M', 'Doe')
                .withAccountInfo(12345678901, 'A')
                .withCardInfo(1234567890123456)
                .build();
            
            mockCICS.simulateEnterKey();
            
            const optionNumber = 1;
            const option = MenuOptionsLoader.getMainMenuOption(optionNumber);
            
            mockCICS.xctl.xctl(option.program, commareaWithData);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.commarea.CDEMO_CUSTOMER_INFO.CDEMO_CUST_ID).toBe(123456789);
            expect(lastCall.commarea.CDEMO_CUSTOMER_INFO.CDEMO_CUST_FNAME.trim()).toBe('John');
            expect(lastCall.commarea.CDEMO_ACCOUNT_INFO.CDEMO_ACCT_ID).toBe(12345678901);
            expect(lastCall.commarea.CDEMO_CARD_INFO.CDEMO_CARD_NUM).toBe(1234567890123456);
        });
    });

    describe('Invalid Key Handling', () => {
        test('should display error message for invalid key press', () => {
            mockCICS.simulateInvalidKey();
            
            const errorMessage = ErrorMessages.getInvalidKeyMessage();
            expect(errorMessage).toBe('Invalid key pressed. Please see below...');
            
            const mapData = {
                OPTIONI: '',
                ERRMSGO: errorMessage
            };
            
            mockCICS.sendReceive.send('COMEN1A', 'COMEN01', mapData);
            
            const sentMap = mockCICS.sendReceive.getLastSentMap();
            expect(sentMap.mapData.ERRMSGO).toBe(errorMessage);
            
            expect(mockCICS.xctl.getCallCount()).toBe(0);
        });

        test('should handle various invalid key scenarios', () => {
            const invalidKeys = ['PF1', 'PF2', 'PF4', 'PF12', 'CLEAR', 'PA1'];
            
            invalidKeys.forEach(key => {
                mockCICS.reset();
                mockCICS.setTransaction('CM00', 'COMEN01C');
                mockCICS.setEIBAID(key);
                
                const errorMessage = ErrorMessages.getInvalidKeyMessage();
                const mapData = {
                    OPTIONI: '',
                    ERRMSGO: errorMessage
                };
                
                mockCICS.sendReceive.send('COMEN1A', 'COMEN01', mapData);
                
                const sentMap = mockCICS.sendReceive.getLastSentMap();
                expect(sentMap.mapData.ERRMSGO).toBe(errorMessage);
            });
        });
    });

    describe('Complete Navigation Flow Integration', () => {
        test('should simulate complete user navigation flow', () => {
            const user = TestUsers.getTestUser('REGULAR_USER');
            const commarea = TestUsers.createMainMenuCommarea('REGULAR_USER');
            
            mockCICS.simulateEnterKey();
            
            const selectedOption = 6;
            const mapData = {
                OPTIONI: selectedOption.toString(),
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COMEN1A', 'COMEN01', mapData);
            
            const inputValidation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'main');
            expect(inputValidation.valid).toBe(true);
            
            const accessValidation = MenuOptionsLoader.validateOptionAccess(
                selectedOption, 
                user.userType, 
                'main'
            );
            expect(accessValidation.valid).toBe(true);
            
            const targetProgram = MenuOptionsLoader.getTargetProgram(selectedOption, 'main');
            expect(targetProgram).toBe('COTRN00C');
            
            commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = 'CM00';
            commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = 'COMEN01C';
            commarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = 0;
            
            mockCICS.xctl.xctl(targetProgram, commarea);
            
            expect(mockCICS.xctl.wasProgramCalled(targetProgram)).toBe(true);
            
            const lastCall = mockCICS.xctl.getLastProgramCall();
            expect(lastCall.program).toBe(targetProgram);
            expect(lastCall.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe(user.userType);
        });
    });
});
