package com.carddemo.enums;

/**
 * Program context enum - mirrors COBOL CDEMO-PGM-CONTEXT field.
 * 0 = ENTER (first entry), 1 = REENTER (returning to program)
 */
public enum ProgramContext {
    ENTER(0),
    REENTER(1);

    private final int code;

    ProgramContext(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ProgramContext fromCode(int code) {
        for (ProgramContext ctx : values()) {
            if (ctx.code == code) {
                return ctx;
            }
        }
        throw new IllegalArgumentException("Unknown program context code: " + code);
    }
}
