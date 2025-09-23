const ERROR_MESSAGES = {
    INVALID_KEY: 'Invalid key pressed. Please see below...',
    INVALID_OPTION: 'Please enter a valid option number...',
    ADMIN_ONLY: 'No access - Admin Only option... ',
    THANK_YOU: 'Thank you for using CardDemo application...',
    COMING_SOON: 'is coming soon ...'
};

const VALIDATION_PATTERNS = {
    NUMERIC_ONLY: /^\d+$/,
    OPTION_RANGE_MAIN: /^([1-9]|10)$/,
    OPTION_RANGE_ADMIN: /^[1-4]$/,
    EMPTY_INPUT: /^\s*$/
};

class ErrorMessages {
    static getInvalidKeyMessage() {
        return ERROR_MESSAGES.INVALID_KEY;
    }

    static getInvalidOptionMessage() {
        return ERROR_MESSAGES.INVALID_OPTION;
    }

    static getAdminOnlyMessage() {
        return ERROR_MESSAGES.ADMIN_ONLY;
    }

    static getThankYouMessage() {
        return ERROR_MESSAGES.THANK_YOU;
    }

    static getComingSoonMessage(optionName = '') {
        if (optionName) {
            return `This option ${optionName} ${ERROR_MESSAGES.COMING_SOON}`;
        }
        return `This option ${ERROR_MESSAGES.COMING_SOON}`;
    }

    static validateOptionInput(input, menuType = 'main') {
        if (!input || VALIDATION_PATTERNS.EMPTY_INPUT.test(input)) {
            return {
                valid: false,
                message: this.getInvalidOptionMessage(),
                errorType: 'EMPTY_INPUT'
            };
        }

        if (!VALIDATION_PATTERNS.NUMERIC_ONLY.test(input.trim())) {
            return {
                valid: false,
                message: this.getInvalidOptionMessage(),
                errorType: 'NON_NUMERIC'
            };
        }

        const optionNumber = parseInt(input.trim(), 10);

        if (menuType === 'main') {
            if (!VALIDATION_PATTERNS.OPTION_RANGE_MAIN.test(optionNumber.toString())) {
                return {
                    valid: false,
                    message: this.getInvalidOptionMessage(),
                    errorType: 'OUT_OF_RANGE'
                };
            }
        } else if (menuType === 'admin') {
            if (!VALIDATION_PATTERNS.OPTION_RANGE_ADMIN.test(optionNumber.toString())) {
                return {
                    valid: false,
                    message: this.getInvalidOptionMessage(),
                    errorType: 'OUT_OF_RANGE'
                };
            }
        }

        return {
            valid: true,
            message: null,
            errorType: null,
            optionNumber
        };
    }

    static validateUserAccess(optionNumber, userType, menuType = 'main') {
        if (menuType === 'main' && userType === 'U') {
            const adminOnlyOptions = [];
            if (adminOnlyOptions.includes(optionNumber)) {
                return {
                    valid: false,
                    message: this.getAdminOnlyMessage(),
                    errorType: 'ACCESS_DENIED'
                };
            }
        }

        return {
            valid: true,
            message: null,
            errorType: null
        };
    }

    static getTestScenarios() {
        return {
            invalidInputs: [
                { input: '', expectedError: 'EMPTY_INPUT' },
                { input: '   ', expectedError: 'EMPTY_INPUT' },
                { input: 'ABC', expectedError: 'NON_NUMERIC' },
                { input: '!@#', expectedError: 'NON_NUMERIC' },
                { input: '0', expectedError: 'OUT_OF_RANGE' },
                { input: '99', expectedError: 'OUT_OF_RANGE' },
                { input: '-1', expectedError: 'NON_NUMERIC' }
            ],
            validInputs: [
                { input: '1', menuType: 'main' },
                { input: '5', menuType: 'main' },
                { input: '10', menuType: 'main' },
                { input: '1', menuType: 'admin' },
                { input: '4', menuType: 'admin' }
            ],
            accessControl: [
                { optionNumber: 1, userType: 'U', menuType: 'main', shouldAllow: true },
                { optionNumber: 1, userType: 'A', menuType: 'main', shouldAllow: true },
                { optionNumber: 1, userType: 'A', menuType: 'admin', shouldAllow: true }
            ]
        };
    }
}

module.exports = {
    ErrorMessages,
    ERROR_MESSAGES,
    VALIDATION_PATTERNS
};
