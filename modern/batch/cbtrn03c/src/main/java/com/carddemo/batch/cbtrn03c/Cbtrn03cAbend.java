package com.carddemo.batch.cbtrn03c;

/** {@code 9999-ABEND-PROGRAM}: the COBOL program calls {@code CEE3ABD} with abend code 999. */
public final class Cbtrn03cAbend extends RuntimeException {

    public static final int ABEND_CODE = 999;

    public Cbtrn03cAbend(String message) {
        super(message);
    }

    public int abendCode() {
        return ABEND_CODE;
    }
}
