package com.cardemo.batch.exception;

/**
 * Exception thrown when a batch report encounters an unrecoverable error.
 * Preserves the ABEND semantics from the original COBOL programs
 * (ABEND code 999 via CEE3ABD).
 */
public class BatchReportException extends RuntimeException {

    private static final int DEFAULT_ABEND_CODE = 999;

    private final int abendCode;
    private final String fileStatus;

    public BatchReportException(String message, String fileStatus) {
        super(message);
        this.abendCode = DEFAULT_ABEND_CODE;
        this.fileStatus = fileStatus;
    }

    public BatchReportException(String message, String fileStatus, Throwable cause) {
        super(message, cause);
        this.abendCode = DEFAULT_ABEND_CODE;
        this.fileStatus = fileStatus;
    }

    public BatchReportException(String message, int abendCode, String fileStatus) {
        super(message);
        this.abendCode = abendCode;
        this.fileStatus = fileStatus;
    }

    public int getAbendCode() {
        return abendCode;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    @Override
    public String toString() {
        return "BatchReportException{" +
                "message='" + getMessage() + '\'' +
                ", abendCode=" + abendCode +
                ", fileStatus='" + fileStatus + '\'' +
                '}';
    }
}
