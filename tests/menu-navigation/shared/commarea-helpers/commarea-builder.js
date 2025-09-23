const USER_TYPES = {
    ADMIN: 'A',
    USER: 'U'
};

const PROGRAM_CONTEXT = {
    ENTER: 0,
    REENTER: 1
};

class CommareaBuilder {
    constructor() {
        this.reset();
    }

    reset() {
        this.commarea = {
            CDEMO_GENERAL_INFO: {
                CDEMO_FROM_TRANID: '    ',
                CDEMO_FROM_PROGRAM: '        ',
                CDEMO_TO_TRANID: '    ',
                CDEMO_TO_PROGRAM: '        ',
                CDEMO_USER_ID: '        ',
                CDEMO_USER_TYPE: ' ',
                CDEMO_PGM_CONTEXT: 0
            },
            CDEMO_CUSTOMER_INFO: {
                CDEMO_CUST_ID: 0,
                CDEMO_CUST_FNAME: '                         ',
                CDEMO_CUST_MNAME: '                         ',
                CDEMO_CUST_LNAME: '                         '
            },
            CDEMO_ACCOUNT_INFO: {
                CDEMO_ACCT_ID: 0,
                CDEMO_ACCT_STATUS: ' '
            },
            CDEMO_CARD_INFO: {
                CDEMO_CARD_NUM: 0
            },
            CDEMO_MORE_INFO: {
                CDEMO_LAST_MAP: '       ',
                CDEMO_LAST_MAPSET: '       '
            }
        };
        return this;
    }

    withUserType(userType) {
        if (!Object.values(USER_TYPES).includes(userType)) {
            throw new Error(`Invalid user type: ${userType}. Must be 'A' (Admin) or 'U' (User)`);
        }
        this.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE = userType;
        return this;
    }

    withAdminUser() {
        return this.withUserType(USER_TYPES.ADMIN);
    }

    withRegularUser() {
        return this.withUserType(USER_TYPES.USER);
    }

    withUserId(userId) {
        if (typeof userId !== 'string' || userId.length > 8) {
            throw new Error('User ID must be a string with maximum 8 characters');
        }
        this.commarea.CDEMO_GENERAL_INFO.CDEMO_USER_ID = userId.padEnd(8, ' ');
        return this;
    }

    withProgramContext(context) {
        if (!Object.values(PROGRAM_CONTEXT).includes(context)) {
            throw new Error(`Invalid program context: ${context}. Must be 0 (ENTER) or 1 (REENTER)`);
        }
        this.commarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT = context;
        return this;
    }

    withEnterContext() {
        return this.withProgramContext(PROGRAM_CONTEXT.ENTER);
    }

    withReenterContext() {
        return this.withProgramContext(PROGRAM_CONTEXT.REENTER);
    }

    withFromTransaction(tranId, programName) {
        if (typeof tranId !== 'string' || tranId.length > 4) {
            throw new Error('Transaction ID must be a string with maximum 4 characters');
        }
        if (typeof programName !== 'string' || programName.length > 8) {
            throw new Error('Program name must be a string with maximum 8 characters');
        }
        this.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_TRANID = tranId.padEnd(4, ' ');
        this.commarea.CDEMO_GENERAL_INFO.CDEMO_FROM_PROGRAM = programName.padEnd(8, ' ');
        return this;
    }

    withToTransaction(tranId, programName) {
        if (typeof tranId !== 'string' || tranId.length > 4) {
            throw new Error('Transaction ID must be a string with maximum 4 characters');
        }
        if (typeof programName !== 'string' || programName.length > 8) {
            throw new Error('Program name must be a string with maximum 8 characters');
        }
        this.commarea.CDEMO_GENERAL_INFO.CDEMO_TO_TRANID = tranId.padEnd(4, ' ');
        this.commarea.CDEMO_GENERAL_INFO.CDEMO_TO_PROGRAM = programName.padEnd(8, ' ');
        return this;
    }

    withCustomerInfo(custId, firstName, middleName, lastName) {
        this.commarea.CDEMO_CUSTOMER_INFO.CDEMO_CUST_ID = custId || 0;
        this.commarea.CDEMO_CUSTOMER_INFO.CDEMO_CUST_FNAME = (firstName || '').padEnd(25, ' ');
        this.commarea.CDEMO_CUSTOMER_INFO.CDEMO_CUST_MNAME = (middleName || '').padEnd(25, ' ');
        this.commarea.CDEMO_CUSTOMER_INFO.CDEMO_CUST_LNAME = (lastName || '').padEnd(25, ' ');
        return this;
    }

    withAccountInfo(acctId, status) {
        this.commarea.CDEMO_ACCOUNT_INFO.CDEMO_ACCT_ID = acctId || 0;
        this.commarea.CDEMO_ACCOUNT_INFO.CDEMO_ACCT_STATUS = (status || ' ').charAt(0);
        return this;
    }

    withCardInfo(cardNum) {
        this.commarea.CDEMO_CARD_INFO.CDEMO_CARD_NUM = cardNum || 0;
        return this;
    }

    withLastMap(mapName, mapsetName) {
        this.commarea.CDEMO_MORE_INFO.CDEMO_LAST_MAP = (mapName || '').padEnd(7, ' ');
        this.commarea.CDEMO_MORE_INFO.CDEMO_LAST_MAPSET = (mapsetName || '').padEnd(7, ' ');
        return this;
    }

    build() {
        return JSON.parse(JSON.stringify(this.commarea));
    }

    isAdminUser(commarea = null) {
        const ca = commarea || this.commarea;
        return ca.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE === USER_TYPES.ADMIN;
    }

    isRegularUser(commarea = null) {
        const ca = commarea || this.commarea;
        return ca.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE === USER_TYPES.USER;
    }

    isEnterContext(commarea = null) {
        const ca = commarea || this.commarea;
        return ca.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT === PROGRAM_CONTEXT.ENTER;
    }

    isReenterContext(commarea = null) {
        const ca = commarea || this.commarea;
        return ca.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT === PROGRAM_CONTEXT.REENTER;
    }

    validateCommarea(commarea) {
        const errors = [];

        if (!commarea.CDEMO_GENERAL_INFO) {
            errors.push('Missing CDEMO_GENERAL_INFO section');
        } else {
            if (!Object.values(USER_TYPES).includes(commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE)) {
                errors.push(`Invalid user type: ${commarea.CDEMO_GENERAL_INFO.CDEMO_USER_TYPE}`);
            }
            if (!Object.values(PROGRAM_CONTEXT).includes(commarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT)) {
                errors.push(`Invalid program context: ${commarea.CDEMO_GENERAL_INFO.CDEMO_PGM_CONTEXT}`);
            }
        }

        if (!commarea.CDEMO_CUSTOMER_INFO) {
            errors.push('Missing CDEMO_CUSTOMER_INFO section');
        }

        if (!commarea.CDEMO_ACCOUNT_INFO) {
            errors.push('Missing CDEMO_ACCOUNT_INFO section');
        }

        if (!commarea.CDEMO_CARD_INFO) {
            errors.push('Missing CDEMO_CARD_INFO section');
        }

        if (!commarea.CDEMO_MORE_INFO) {
            errors.push('Missing CDEMO_MORE_INFO section');
        }

        if (errors.length > 0) {
            throw new Error(`COMMAREA validation errors: ${errors.join(', ')}`);
        }

        return true;
    }

    static createTestAdminUser(userId = 'ADMIN001') {
        return new CommareaBuilder()
            .withAdminUser()
            .withUserId(userId)
            .withReenterContext()
            .build();
    }

    static createTestRegularUser(userId = 'USER0001') {
        return new CommareaBuilder()
            .withRegularUser()
            .withUserId(userId)
            .withReenterContext()
            .build();
    }

    static createEmptyCommarea() {
        return new CommareaBuilder().build();
    }
}

module.exports = {
    CommareaBuilder,
    USER_TYPES,
    PROGRAM_CONTEXT
};
