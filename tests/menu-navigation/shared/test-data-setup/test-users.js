const { CommareaBuilder, USER_TYPES } = require('../commarea-helpers/commarea-builder');

const TEST_USERS = {
    ADMIN_USER: {
        userId: 'ADMIN001',
        userType: USER_TYPES.ADMIN,
        description: 'Test admin user with full access'
    },
    REGULAR_USER: {
        userId: 'USER0001',
        userType: USER_TYPES.USER,
        description: 'Test regular user with limited access'
    },
    ADMIN_USER_2: {
        userId: 'ADMIN002',
        userType: USER_TYPES.ADMIN,
        description: 'Second test admin user'
    },
    REGULAR_USER_2: {
        userId: 'USER0002',
        userType: USER_TYPES.USER,
        description: 'Second test regular user'
    }
};

class TestUsers {
    static getTestUser(userKey) {
        const user = TEST_USERS[userKey];
        if (!user) {
            throw new Error(`Unknown test user: ${userKey}`);
        }
        return JSON.parse(JSON.stringify(user));
    }

    static getAllTestUsers() {
        return Object.keys(TEST_USERS).map(key => ({
            key,
            ...TEST_USERS[key]
        }));
    }

    static getAdminUsers() {
        return this.getAllTestUsers().filter(user => user.userType === USER_TYPES.ADMIN);
    }

    static getRegularUsers() {
        return this.getAllTestUsers().filter(user => user.userType === USER_TYPES.USER);
    }

    static createCommareaForUser(userKey, options = {}) {
        const user = this.getTestUser(userKey);
        const builder = new CommareaBuilder()
            .withUserType(user.userType)
            .withUserId(user.userId);

        if (options.programContext !== undefined) {
            builder.withProgramContext(options.programContext);
        } else {
            builder.withReenterContext();
        }

        if (options.fromTransaction) {
            builder.withFromTransaction(options.fromTransaction.tranId, options.fromTransaction.program);
        }

        if (options.toTransaction) {
            builder.withToTransaction(options.toTransaction.tranId, options.toTransaction.program);
        }

        if (options.customerInfo) {
            builder.withCustomerInfo(
                options.customerInfo.custId,
                options.customerInfo.firstName,
                options.customerInfo.middleName,
                options.customerInfo.lastName
            );
        }

        if (options.accountInfo) {
            builder.withAccountInfo(options.accountInfo.acctId, options.accountInfo.status);
        }

        if (options.cardInfo) {
            builder.withCardInfo(options.cardInfo.cardNum);
        }

        if (options.lastMap) {
            builder.withLastMap(options.lastMap.mapName, options.lastMap.mapsetName);
        }

        return builder.build();
    }

    static createMainMenuCommarea(userKey) {
        return this.createCommareaForUser(userKey, {
            fromTransaction: { tranId: 'CC00', program: 'COSGN00C' },
            lastMap: { mapName: 'COMEN1A', mapsetName: 'COMEN01' }
        });
    }

    static createAdminMenuCommarea(userKey) {
        return this.createCommareaForUser(userKey, {
            fromTransaction: { tranId: 'CM00', program: 'COMEN01C' },
            lastMap: { mapName: 'COADM1A', mapsetName: 'COADM01' }
        });
    }

    static createTestScenarios() {
        return {
            mainMenuScenarios: [
                {
                    name: 'Admin user accessing main menu',
                    userKey: 'ADMIN_USER',
                    commarea: this.createMainMenuCommarea('ADMIN_USER'),
                    expectedAccess: 'FULL'
                },
                {
                    name: 'Regular user accessing main menu',
                    userKey: 'REGULAR_USER',
                    commarea: this.createMainMenuCommarea('REGULAR_USER'),
                    expectedAccess: 'LIMITED'
                }
            ],
            adminMenuScenarios: [
                {
                    name: 'Admin user accessing admin menu',
                    userKey: 'ADMIN_USER',
                    commarea: this.createAdminMenuCommarea('ADMIN_USER'),
                    expectedAccess: 'FULL'
                },
                {
                    name: 'Regular user accessing admin menu',
                    userKey: 'REGULAR_USER',
                    commarea: this.createAdminMenuCommarea('REGULAR_USER'),
                    expectedAccess: 'DENIED'
                }
            ],
            navigationScenarios: [
                {
                    name: 'F3 key from main menu',
                    userKey: 'REGULAR_USER',
                    fromProgram: 'COMEN01C',
                    expectedTarget: 'COSGN00C',
                    keyPressed: 'F3'
                },
                {
                    name: 'F3 key from admin menu',
                    userKey: 'ADMIN_USER',
                    fromProgram: 'COADM01C',
                    expectedTarget: 'COSGN00C',
                    keyPressed: 'F3'
                }
            ]
        };
    }

    static validateUserConfiguration(userKey) {
        try {
            const user = this.getTestUser(userKey);
            const commarea = this.createCommareaForUser(userKey);
            
            const builder = new CommareaBuilder();
            builder.validateCommarea(commarea);
            
            return {
                valid: true,
                user,
                commarea
            };
        } catch (error) {
            return {
                valid: false,
                error: error.message
            };
        }
    }
}

module.exports = {
    TestUsers,
    TEST_USERS
};
