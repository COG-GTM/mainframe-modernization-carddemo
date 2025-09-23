const { describe, test, expect, beforeEach } = require('@jest/globals');
const { MockCICSEnvironment } = require('../../shared/mock-cics-environment');
const { CommareaBuilder, USER_TYPES } = require('../../shared/commarea-helpers/commarea-builder');
const { ErrorMessages } = require('../../shared/test-data-setup/error-messages');
const { MenuOptionsLoader } = require('../../shared/test-data-setup/menu-options-loader');
const { TestUsers } = require('../../shared/test-data-setup/test-users');

describe('Main Menu Access Control Tests', () => {
    let mockCICS;

    beforeEach(() => {
        mockCICS = new MockCICSEnvironment();
        mockCICS.setTransaction('CM00', 'COMEN01C');
        mockCICS.setEIBCALEN(100);
    });

    describe('Regular User Access Control', () => {
        let regularUserCommarea;

        beforeEach(() => {
            regularUserCommarea = TestUsers.createMainMenuCommarea('REGULAR_USER');
        });

        test('should allow regular user access to all standard menu options', () => {
            const mainMenuOptions = MenuOptionsLoader.getMainMenuOptions();
            
            mainMenuOptions.forEach(option => {
                if (option.userType === 'U') {
                    const accessValidation = MenuOptionsLoader.validateOptionAccess(
                        option.number, 
                        'U', 
                        'main'
                    );
                    
                    expect(accessValidation.valid).toBe(true);
                    expect(accessValidation.message).toBe(null);
                }
            });
        });

        test('should block regular user from admin-only options', () => {
            const adminOnlyOptions = MenuOptionsLoader.getMainMenuOptions()
                .filter(option => option.userType === 'A');
            
            adminOnlyOptions.forEach(option => {
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    option.number, 
                    'U', 
                    'main'
                );
                
                expect(accessValidation.valid).toBe(false);
                expect(accessValidation.message).toBe('No access - Admin Only option... ');
            });
        });

        test('should display correct error message for unauthorized access', () => {
            const errorMessage = ErrorMessages.getAdminOnlyMessage();
            expect(errorMessage).toBe('No access - Admin Only option... ');
        });

        test('should validate regular user COMMAREA structure', () => {
            expect(regularUserCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('U');
            expect(regularUserCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID.trim()).toBe('USER0001');
            
            const builder = new CommareaBuilder();
            expect(() => builder.validateCommarea(regularUserCommarea)).not.toThrow();
        });

        test('should simulate regular user menu access in CICS environment', () => {
            mockCICS.simulateEnterKey();
            
            const mapData = {
                OPTIONI: '1',
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COMEN1A', 'COMEN01', mapData);
            
            const option = MenuOptionsLoader.getMainMenuOption(1);
            const userType = regularUserCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE;
            
            const accessValidation = MenuOptionsLoader.validateOptionAccess(1, userType, 'main');
            expect(accessValidation.valid).toBe(true);
            
            if (accessValidation.valid) {
                mockCICS.xctl.xctl(option.program, regularUserCommarea);
                expect(mockCICS.xctl.wasProgramCalled(option.program)).toBe(true);
            }
        });
    });

    describe('Admin User Access Control', () => {
        let adminUserCommarea;

        beforeEach(() => {
            adminUserCommarea = TestUsers.createMainMenuCommarea('ADMIN_USER');
        });

        test('should allow admin user access to all menu options', () => {
            const mainMenuOptions = MenuOptionsLoader.getMainMenuOptions();
            
            mainMenuOptions.forEach(option => {
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    option.number, 
                    'A', 
                    'main'
                );
                
                expect(accessValidation.valid).toBe(true);
                expect(accessValidation.message).toBe(null);
            });
        });

        test('should allow admin user access to admin-only options', () => {
            const adminOnlyOptions = MenuOptionsLoader.getMainMenuOptions()
                .filter(option => option.userType === 'A');
            
            adminOnlyOptions.forEach(option => {
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    option.number, 
                    'A', 
                    'main'
                );
                
                expect(accessValidation.valid).toBe(true);
                expect(accessValidation.message).toBe(null);
            });
        });

        test('should validate admin user COMMAREA structure', () => {
            expect(adminUserCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('A');
            expect(adminUserCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID.trim()).toBe('ADMIN001');
            
            const builder = new CommareaBuilder();
            expect(() => builder.validateCommarea(adminUserCommarea)).not.toThrow();
        });

        test('should simulate admin user accessing all menu options', () => {
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
                
                const userType = adminUserCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE;
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    option.number, 
                    userType, 
                    'main'
                );
                
                expect(accessValidation.valid).toBe(true);
                
                mockCICS.xctl.xctl(option.program, adminUserCommarea);
                expect(mockCICS.xctl.wasProgramCalled(option.program)).toBe(true);
            });
        });
    });

    describe('User Type Validation Logic', () => {
        test('should correctly identify user types from COMMAREA', () => {
            const builder = new CommareaBuilder();
            
            const adminCommarea = builder.reset().withAdminUser().build();
            expect(builder.isAdminUser(adminCommarea)).toBe(true);
            expect(builder.isRegularUser(adminCommarea)).toBe(false);
            
            const userCommarea = builder.reset().withRegularUser().build();
            expect(builder.isAdminUser(userCommarea)).toBe(false);
            expect(builder.isRegularUser(userCommarea)).toBe(true);
        });

        test('should validate user type constants', () => {
            expect(USER_TYPES.ADMIN).toBe('A');
            expect(USER_TYPES.USER).toBe('U');
        });

        test('should handle invalid user types', () => {
            expect(() => {
                new CommareaBuilder().withUserType('X');
            }).toThrow('Invalid user type: X. Must be \'A\' (Admin) or \'U\' (User)');
        });
    });

    describe('Access Control Integration Tests', () => {
        test('should simulate complete access control flow for regular user', () => {
            const regularUser = TestUsers.getTestUser('REGULAR_USER');
            const commarea = TestUsers.createMainMenuCommarea('REGULAR_USER');
            
            mockCICS.simulateEnterKey();
            
            const testOption = 1;
            const mapData = {
                OPTIONI: testOption.toString(),
                ERRMSGO: ''
            };
            
            mockCICS.sendReceive.receive('COMEN1A', 'COMEN01', mapData);
            
            const inputValidation = ErrorMessages.validateOptionInput(mapData.OPTIONI, 'main');
            expect(inputValidation.valid).toBe(true);
            
            const accessValidation = MenuOptionsLoader.validateOptionAccess(
                testOption, 
                regularUser.userType, 
                'main'
            );
            expect(accessValidation.valid).toBe(true);
            
            const targetProgram = MenuOptionsLoader.getTargetProgram(testOption, 'main');
            mockCICS.xctl.xctl(targetProgram, commarea);
            
            expect(mockCICS.xctl.wasProgramCalled(targetProgram)).toBe(true);
            expect(mockCICS.xctl.getLastProgramCall().commarea).toEqual(commarea);
        });

        test('should simulate access denial for regular user on admin option', () => {
            const regularUser = TestUsers.getTestUser('REGULAR_USER');
            
            const adminOnlyOptions = MenuOptionsLoader.getMainMenuOptions()
                .filter(option => option.userType === 'A');
            
            if (adminOnlyOptions.length > 0) {
                const adminOption = adminOnlyOptions[0];
                
                mockCICS.simulateEnterKey();
                
                const mapData = {
                    OPTIONI: adminOption.number.toString(),
                    ERRMSGO: ''
                };
                
                const accessValidation = MenuOptionsLoader.validateOptionAccess(
                    adminOption.number, 
                    regularUser.userType, 
                    'main'
                );
                
                expect(accessValidation.valid).toBe(false);
                expect(accessValidation.message).toBe('No access - Admin Only option... ');
                
                mapData.ERRMSGO = accessValidation.message;
                mockCICS.sendReceive.send('COMEN1A', 'COMEN01', mapData);
                
                const sentMap = mockCICS.sendReceive.getLastSentMap();
                expect(sentMap.mapData.ERRMSGO).toBe('No access - Admin Only option... ');
                
                expect(mockCICS.xctl.getCallCount()).toBe(0);
            }
        });
    });

    describe('Test User Configuration Validation', () => {
        test('should validate all test user configurations', () => {
            const testUsers = TestUsers.getAllTestUsers();
            
            testUsers.forEach(user => {
                const validation = TestUsers.validateUserConfiguration(user.key);
                expect(validation.valid).toBe(true);
                expect(validation.user).toBeDefined();
                expect(validation.commarea).toBeDefined();
            });
        });

        test('should create proper COMMAREA for different user types', () => {
            const adminCommarea = TestUsers.createMainMenuCommarea('ADMIN_USER');
            expect(adminCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('A');
            
            const userCommarea = TestUsers.createMainMenuCommarea('REGULAR_USER');
            expect(userCommarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE).toBe('U');
        });
    });
});
