package com.cog.carddemo;

/**
 * Raised where {@code CBACT01C} performs {@code 9999-ABEND-PROGRAM}, i.e. calls
 * {@code CEE3ABD} with abend code 999.
 */
public class AbendException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Abend code moved to {@code ABCODE} by {@code 9999-ABEND-PROGRAM}. Callers that
     * inspect the process exit status see it truncated to eight bits (999 &amp; 255 = 231).
     */
    public static final int ABEND_CODE = 999;

    private final String fileStatus;

    public AbendException(String message, String fileStatus) {
        super(message);
        this.fileStatus = fileStatus;
    }

    /** The two character COBOL file status that caused the abend. */
    public String fileStatus() {
        return fileStatus;
    }
}
