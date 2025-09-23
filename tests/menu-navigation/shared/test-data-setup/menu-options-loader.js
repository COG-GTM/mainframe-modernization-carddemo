const MAIN_MENU_OPTIONS = [
    {
        number: 1,
        name: 'Account View',
        program: 'COACTVWC',
        userType: 'U'
    },
    {
        number: 2,
        name: 'Account Update',
        program: 'COACTUPC',
        userType: 'U'
    },
    {
        number: 3,
        name: 'Credit Card List',
        program: 'COCRDLIC',
        userType: 'U'
    },
    {
        number: 4,
        name: 'Credit Card View',
        program: 'COCRDSLC',
        userType: 'U'
    },
    {
        number: 5,
        name: 'Credit Card Update',
        program: 'COCRDUPC',
        userType: 'U'
    },
    {
        number: 6,
        name: 'Transaction List',
        program: 'COTRN00C',
        userType: 'U'
    },
    {
        number: 7,
        name: 'Transaction View',
        program: 'COTRN01C',
        userType: 'U'
    },
    {
        number: 8,
        name: 'Transaction Add',
        program: 'COTRN02C',
        userType: 'U'
    },
    {
        number: 9,
        name: 'Transaction Reports',
        program: 'CORPT00C',
        userType: 'U'
    },
    {
        number: 10,
        name: 'Bill Payment',
        program: 'COBIL00C',
        userType: 'U'
    }
];

const ADMIN_MENU_OPTIONS = [
    {
        number: 1,
        name: 'User List (Security)',
        program: 'COUSR00C'
    },
    {
        number: 2,
        name: 'User Add (Security)',
        program: 'COUSR01C'
    },
    {
        number: 3,
        name: 'User Update (Security)',
        program: 'COUSR02C'
    },
    {
        number: 4,
        name: 'User Delete (Security)',
        program: 'COUSR03C'
    }
];

class MenuOptionsLoader {
    static getMainMenuOptions() {
        return JSON.parse(JSON.stringify(MAIN_MENU_OPTIONS));
    }

    static getAdminMenuOptions() {
        return JSON.parse(JSON.stringify(ADMIN_MENU_OPTIONS));
    }

    static getMainMenuOption(optionNumber) {
        const option = MAIN_MENU_OPTIONS.find(opt => opt.number === optionNumber);
        return option ? JSON.parse(JSON.stringify(option)) : null;
    }

    static getAdminMenuOption(optionNumber) {
        const option = ADMIN_MENU_OPTIONS.find(opt => opt.number === optionNumber);
        return option ? JSON.parse(JSON.stringify(option)) : null;
    }

    static getMainMenuCount() {
        return MAIN_MENU_OPTIONS.length;
    }

    static getAdminMenuCount() {
        return ADMIN_MENU_OPTIONS.length;
    }

    static getMainMenuProgramNames() {
        return MAIN_MENU_OPTIONS.map(opt => opt.program);
    }

    static getAdminMenuProgramNames() {
        return ADMIN_MENU_OPTIONS.map(opt => opt.program);
    }

    static getAllProgramNames() {
        return [
            ...this.getMainMenuProgramNames(),
            ...this.getAdminMenuProgramNames()
        ];
    }

    static isValidMainMenuOption(optionNumber) {
        return optionNumber >= 1 && optionNumber <= this.getMainMenuCount();
    }

    static isValidAdminMenuOption(optionNumber) {
        return optionNumber >= 1 && optionNumber <= this.getAdminMenuCount();
    }

    static getMainMenuOptionsByUserType(userType) {
        return MAIN_MENU_OPTIONS.filter(opt => opt.userType === userType || userType === 'A');
    }

    static isAdminOnlyOption(optionNumber) {
        const option = this.getMainMenuOption(optionNumber);
        return option && option.userType === 'A';
    }

    static validateOptionAccess(optionNumber, userType, menuType = 'main') {
        if (menuType === 'main') {
            if (!this.isValidMainMenuOption(optionNumber)) {
                return { valid: false, message: 'Please enter a valid option number...' };
            }
            
            const option = this.getMainMenuOption(optionNumber);
            if (userType === 'U' && option.userType === 'A') {
                return { valid: false, message: 'No access - Admin Only option... ' };
            }
        } else if (menuType === 'admin') {
            if (!this.isValidAdminMenuOption(optionNumber)) {
                return { valid: false, message: 'Please enter a valid option number...' };
            }
        }

        return { valid: true, message: null };
    }

    static getTargetProgram(optionNumber, menuType = 'main') {
        if (menuType === 'main') {
            const option = this.getMainMenuOption(optionNumber);
            return option ? option.program : null;
        } else if (menuType === 'admin') {
            const option = this.getAdminMenuOption(optionNumber);
            return option ? option.program : null;
        }
        return null;
    }
}

module.exports = {
    MenuOptionsLoader,
    MAIN_MENU_OPTIONS,
    ADMIN_MENU_OPTIONS
};
