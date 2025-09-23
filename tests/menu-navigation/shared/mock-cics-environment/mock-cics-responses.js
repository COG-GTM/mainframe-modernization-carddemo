const CICS_RESPONSE_CODES = {
    NORMAL: 'NORMAL',
    MAPFAIL: 'MAPFAIL',
    NOTFND: 'NOTFND',
    PGMIDERR: 'PGMIDERR',
    INVREQ: 'INVREQ',
    LENGERR: 'LENGERR',
    TERMIDERR: 'TERMIDERR',
    EXPIRED: 'EXPIRED'
};

const CICS_REASON_CODES = {
    NORMAL: 0,
    INVALID_KEY: 1,
    INVALID_OPTION: 2,
    ACCESS_DENIED: 3,
    PROGRAM_NOT_FOUND: 4,
    MAP_NOT_FOUND: 5
};

class MockCICSResponses {
    constructor() {
        this.responseHistory = [];
        this.currentResponse = CICS_RESPONSE_CODES.NORMAL;
        this.currentReason = CICS_REASON_CODES.NORMAL;
    }

    reset() {
        this.responseHistory = [];
        this.currentResponse = CICS_RESPONSE_CODES.NORMAL;
        this.currentReason = CICS_REASON_CODES.NORMAL;
    }

    setResponse(responseCode, reasonCode = CICS_REASON_CODES.NORMAL) {
        this.currentResponse = responseCode;
        this.currentReason = reasonCode;
        
        this.responseHistory.push({
            responseCode,
            reasonCode,
            timestamp: new Date()
        });
    }

    getCurrentResponse() {
        return {
            responseCode: this.currentResponse,
            reasonCode: this.currentReason
        };
    }

    getResponseHistory() {
        return [...this.responseHistory];
    }

    simulateNormalResponse() {
        this.setResponse(CICS_RESPONSE_CODES.NORMAL, CICS_REASON_CODES.NORMAL);
    }

    simulateMapFailure() {
        this.setResponse(CICS_RESPONSE_CODES.MAPFAIL, CICS_REASON_CODES.MAP_NOT_FOUND);
    }

    simulateProgramError() {
        this.setResponse(CICS_RESPONSE_CODES.PGMIDERR, CICS_REASON_CODES.PROGRAM_NOT_FOUND);
    }

    simulateInvalidRequest() {
        this.setResponse(CICS_RESPONSE_CODES.INVREQ, CICS_REASON_CODES.INVALID_OPTION);
    }

    simulateAccessDenied() {
        this.setResponse(CICS_RESPONSE_CODES.INVREQ, CICS_REASON_CODES.ACCESS_DENIED);
    }

    isNormalResponse() {
        return this.currentResponse === CICS_RESPONSE_CODES.NORMAL;
    }

    isErrorResponse() {
        return this.currentResponse !== CICS_RESPONSE_CODES.NORMAL;
    }

    getLastResponseCode() {
        const lastResponse = this.responseHistory[this.responseHistory.length - 1];
        return lastResponse ? lastResponse.responseCode : CICS_RESPONSE_CODES.NORMAL;
    }

    getLastReasonCode() {
        const lastResponse = this.responseHistory[this.responseHistory.length - 1];
        return lastResponse ? lastResponse.reasonCode : CICS_REASON_CODES.NORMAL;
    }
}

module.exports = {
    MockCICSResponses,
    CICS_RESPONSE_CODES,
    CICS_REASON_CODES
};
