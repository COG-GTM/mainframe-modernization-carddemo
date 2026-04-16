package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing program context from COBOL 88-level conditions in COCOM01Y.
 * <pre>
 *   88 CDEMO-PGM-ENTER   VALUE 0.
 *   88 CDEMO-PGM-REENTER VALUE 1.
 * </pre>
 */
public enum ProgramContext {

    ENTER(0),
    REENTER(1);

    private final int code;

    ProgramContext(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator
    public static ProgramContext fromCode(int code) {
        for (ProgramContext ctx : values()) {
            if (ctx.code == code) {
                return ctx;
            }
        }
        throw new IllegalArgumentException("Unknown ProgramContext code: " + code);
    }
}
