package com.cardemo.utility.model;

/**
 * Represents the result of a date validation operation.
 * Maps to WS-EDIT-DATE-FLGS from CSUTLDWY.cpy.
 *
 * <p>Each flag tracks the validation state of a date component:
 * <ul>
 *   <li>{@link FlagStatus#VALID} - LOW-VALUES in COBOL: component is valid</li>
 *   <li>{@link FlagStatus#NOT_OK} - '0' in COBOL: component failed validation</li>
 *   <li>{@link FlagStatus#BLANK} - 'B' in COBOL: component was blank/not supplied</li>
 * </ul>
 */
public class DateValidationResult {

    /**
     * Maps to the 88-level conditions on WS-EDIT-YEAR-FLG, WS-EDIT-MONTH,
     * and WS-EDIT-DAY in CSUTLDWY.cpy.
     */
    public enum FlagStatus {
        /** LOW-VALUES: field is valid */
        VALID,
        /** '0': field not ok */
        NOT_OK,
        /** 'B': field was blank */
        BLANK
    }

    private FlagStatus yearFlag;
    private FlagStatus monthFlag;
    private FlagStatus dayFlag;
    private String errorMessage;

    public DateValidationResult() {
        // WS-EDIT-DATE-IS-INVALID VALUE '000' — all flags NOT_OK initially
        this.yearFlag = FlagStatus.NOT_OK;
        this.monthFlag = FlagStatus.NOT_OK;
        this.dayFlag = FlagStatus.NOT_OK;
        this.errorMessage = null;
    }

    /**
     * Maps to WS-EDIT-DATE-IS-VALID (88-level, VALUE LOW-VALUES):
     * all three flags must be VALID.
     */
    public boolean isValid() {
        return yearFlag == FlagStatus.VALID
                && monthFlag == FlagStatus.VALID
                && dayFlag == FlagStatus.VALID;
    }

    /**
     * Maps to WS-EDIT-DATE-IS-INVALID (88-level, VALUE '000'):
     * all three flags are NOT_OK.
     */
    public boolean isInvalid() {
        return yearFlag == FlagStatus.NOT_OK
                && monthFlag == FlagStatus.NOT_OK
                && dayFlag == FlagStatus.NOT_OK;
    }

    public FlagStatus getYearFlag() {
        return yearFlag;
    }

    public void setYearFlag(FlagStatus yearFlag) {
        this.yearFlag = yearFlag;
    }

    public FlagStatus getMonthFlag() {
        return monthFlag;
    }

    public void setMonthFlag(FlagStatus monthFlag) {
        this.monthFlag = monthFlag;
    }

    public FlagStatus getDayFlag() {
        return dayFlag;
    }

    public void setDayFlag(FlagStatus dayFlag) {
        this.dayFlag = dayFlag;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the COBOL-equivalent character for a flag status.
     * Maps: VALID -> LOW-VALUES (0x00), NOT_OK -> '0', BLANK -> 'B'.
     */
    public static char toCobolFlag(FlagStatus status) {
        return switch (status) {
            case VALID -> '\0';
            case NOT_OK -> '0';
            case BLANK -> 'B';
        };
    }

    /**
     * Returns the 3-character WS-EDIT-DATE-FLGS string representation.
     */
    public String getFlags() {
        return "" + toCobolFlag(yearFlag) + toCobolFlag(monthFlag) + toCobolFlag(dayFlag);
    }

    @Override
    public String toString() {
        return "DateValidationResult{" +
                "valid=" + isValid() +
                ", yearFlag=" + yearFlag +
                ", monthFlag=" + monthFlag +
                ", dayFlag=" + dayFlag +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
