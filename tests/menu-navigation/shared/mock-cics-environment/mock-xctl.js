class MockXCTL {
    constructor() {
        this.programCalls = [];
        this.lastProgram = null;
        this.lastCommarea = null;
        this.shouldFail = false;
        this.failureReason = null;
    }

    reset() {
        this.programCalls = [];
        this.lastProgram = null;
        this.lastCommarea = null;
        this.shouldFail = false;
        this.failureReason = null;
    }

    setFailure(shouldFail, reason = 'XCTL_FAILED') {
        this.shouldFail = shouldFail;
        this.failureReason = reason;
    }

    xctl(programName, commarea) {
        if (this.shouldFail) {
            throw new Error(`XCTL failed: ${this.failureReason}`);
        }

        this.programCalls.push({
            program: programName,
            commarea: JSON.parse(JSON.stringify(commarea)),
            timestamp: new Date()
        });

        this.lastProgram = programName;
        this.lastCommarea = commarea;

        return {
            responseCode: 'NORMAL',
            program: programName,
            commarea: commarea
        };
    }

    getLastProgramCall() {
        return this.programCalls[this.programCalls.length - 1] || null;
    }

    getProgramCallHistory() {
        return [...this.programCalls];
    }

    wasProgramCalled(programName) {
        return this.programCalls.some(call => call.program === programName);
    }

    getCallCount() {
        return this.programCalls.length;
    }

    validateTargetProgram(programName, expectedPrograms) {
        if (!expectedPrograms.includes(programName)) {
            throw new Error(`Invalid target program: ${programName}. Expected one of: ${expectedPrograms.join(', ')}`);
        }
        return true;
    }
}

module.exports = MockXCTL;
