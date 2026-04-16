package com.cardemo.gateway.enums;

/**
 * Maps CDEMO-PGM-CONTEXT from COCOM01Y.cpy.
 * Level-88 conditions: CDEMO-PGM-ENTER VALUE 0, CDEMO-PGM-REENTER VALUE 1.
 *
 * ENTER (0) = Fresh request — program is being invoked for the first time.
 * REENTER (1) = Continuation — program is being re-entered after a previous interaction.
 */
public enum ProgramContext {
    ENTER(0),
    REENTER(1);

    private final int value;

    ProgramContext(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ProgramContext fromValue(int value) {
        for (ProgramContext ctx : values()) {
            if (ctx.value == value) {
                return ctx;
            }
        }
        throw new IllegalArgumentException("Unknown program context value: " + value);
    }
}
