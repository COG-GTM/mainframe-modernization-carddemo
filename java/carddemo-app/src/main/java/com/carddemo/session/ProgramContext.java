package com.carddemo.session;

/**
 * Pseudo-conversational program context — mirrors {@code CDEMO-PGM-CONTEXT PIC 9(01)}
 * in copybook {@code COCOM01Y} and its 88-levels.
 *
 * <ul>
 *   <li>{@code CDEMO-PGM-ENTER   VALUE 0} — first entry into a program (initialise state).</li>
 *   <li>{@code CDEMO-PGM-REENTER VALUE 1} — a subsequent turn (process user input, keep state).</li>
 * </ul>
 *
 * <p>In CICS a program {@code RETURN}s with {@code TRANSID} and a COMMAREA; on the next
 * terminal interaction the same program is re-invoked and inspects this flag to decide
 * whether it is being entered for the first time (ENTER) or re-entered (RE-ENTER).</p>
 */
public enum ProgramContext {

    ENTER(0),
    REENTER(1);

    private final int code;

    ProgramContext(int code) {
        this.code = code;
    }

    /** The numeric COBOL code (0 or 1). */
    public int code() {
        return code;
    }

    /** Resolve from the raw numeric code; anything other than 1 is treated as ENTER (0). */
    public static ProgramContext fromCode(int value) {
        return value == 1 ? REENTER : ENTER;
    }
}
