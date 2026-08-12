package com.carddemo.batch.statement;

/**
 * COBOL program: CBSTM03A — replaces 9999-ABEND-PROGRAM, which displays the failing DD name
 * and the file status returned by CBSTM03B before calling CEE3ABD.
 */
public class StatementFileException extends RuntimeException {

    private final String ddName;
    private final String returnCode;

    public StatementFileException(String ddName, String returnCode) {
        super("ERROR READING " + ddName + " RETURN CODE: " + returnCode);
        this.ddName = ddName;
        this.returnCode = returnCode;
    }

    public String getDdName() {
        return ddName;
    }

    public String getReturnCode() {
        return returnCode;
    }
}
