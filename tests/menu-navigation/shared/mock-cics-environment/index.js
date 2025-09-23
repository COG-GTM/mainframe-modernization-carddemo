const MockXCTL = require('./mock-xctl');
const MockSendReceive = require('./mock-send-receive');
const { MockCICSResponses, CICS_RESPONSE_CODES, CICS_REASON_CODES } = require('./mock-cics-responses');

class MockCICSEnvironment {
    constructor() {
        this.xctl = new MockXCTL();
        this.sendReceive = new MockSendReceive();
        this.responses = new MockCICSResponses();
        this.eibcalen = 0;
        this.eibaid = null;
        this.currentTransaction = null;
        this.currentProgram = null;
    }

    reset() {
        this.xctl.reset();
        this.sendReceive.reset();
        this.responses.reset();
        this.eibcalen = 0;
        this.eibaid = null;
        this.currentTransaction = null;
        this.currentProgram = null;
    }

    setTransaction(transactionId, programName) {
        this.currentTransaction = transactionId;
        this.currentProgram = programName;
    }

    setEIBCALEN(length) {
        this.eibcalen = length;
    }

    setEIBAID(aid) {
        this.eibaid = aid;
    }

    simulateEnterKey() {
        this.setEIBAID('ENTER');
    }

    simulateF3Key() {
        this.setEIBAID('PF3');
    }

    simulateInvalidKey() {
        this.setEIBAID('INVALID');
    }

    getEnvironmentState() {
        return {
            transaction: this.currentTransaction,
            program: this.currentProgram,
            eibcalen: this.eibcalen,
            eibaid: this.eibaid,
            lastXCTL: this.xctl.getLastProgramCall(),
            lastSentMap: this.sendReceive.getLastSentMap(),
            lastResponse: this.responses.getCurrentResponse()
        };
    }

    validateEnvironmentSetup() {
        const errors = [];
        
        if (!this.currentTransaction) {
            errors.push('Transaction ID not set');
        }
        
        if (!this.currentProgram) {
            errors.push('Program name not set');
        }
        
        if (errors.length > 0) {
            throw new Error(`Environment setup errors: ${errors.join(', ')}`);
        }
        
        return true;
    }
}

module.exports = {
    MockCICSEnvironment,
    MockXCTL,
    MockSendReceive,
    MockCICSResponses,
    CICS_RESPONSE_CODES,
    CICS_REASON_CODES
};
