package com.cardemo.batch.orchestration.model;

/**
 * Represents JCL-style return codes used for conditional step execution.
 * Maps COBOL RETURN-CODE values to Spring Batch exit statuses.
 *
 * <ul>
 *   <li>0 = Success</li>
 *   <li>4 = Warning (e.g., rejections exist) — log but continue</li>
 *   <li>8 = Error — fail the job</li>
 *   <li>12 = Severe error — fail the job</li>
 *   <li>16 = Critical error — fail the job</li>
 * </ul>
 */
public final class ReturnCode {

    public static final int SUCCESS = 0;
    public static final int WARNING = 4;
    public static final int ERROR = 8;
    public static final int SEVERE = 12;
    public static final int CRITICAL = 16;

    private final int code;
    private final String message;

    public ReturnCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ReturnCode(int code) {
        this(code, descriptionFor(code));
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return code == SUCCESS;
    }

    public boolean isWarning() {
        return code == WARNING;
    }

    public boolean isError() {
        return code > WARNING;
    }

    /**
     * Evaluates the JCL COND parameter logic.
     * In JCL, COND=(code,operator) means: skip the step if the condition is true.
     * This method returns true if execution should CONTINUE (condition NOT met).
     */
    public boolean shouldContinue(int condCode) {
        return code <= condCode;
    }

    private static String descriptionFor(int code) {
        return switch (code) {
            case SUCCESS -> "Successful completion";
            case WARNING -> "Completed with warnings";
            case ERROR -> "Error occurred";
            case SEVERE -> "Severe error occurred";
            case CRITICAL -> "Critical error occurred";
            default -> "Return code: " + code;
        };
    }

    @Override
    public String toString() {
        return "ReturnCode{code=" + code + ", message='" + message + "'}";
    }
}
