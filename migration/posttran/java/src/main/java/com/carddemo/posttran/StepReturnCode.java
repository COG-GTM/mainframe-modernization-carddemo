package com.carddemo.posttran;

import org.springframework.boot.ExitCodeGenerator;

/**
 * Carries the COBOL {@code RETURN-CODE} out to the process exit code, so a caller sees the same
 * condition code the JCL step would have set: 4 when any transaction was rejected
 * (CBTRN02C.cbl:229-231).
 */
public class StepReturnCode implements ExitCodeGenerator {

    private volatile int value;

    public void set(int value) {
        this.value = value;
    }

    @Override
    public int getExitCode() {
        return value;
    }
}
