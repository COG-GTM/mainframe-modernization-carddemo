package com.carddemo.session;

/**
 * The 3270 attention identifiers ({@code EIBAID}) the online programs inspect, mapped to a
 * logical {@link CommonAction}. In CICS these are the {@code DFHENTER} / {@code DFHPF3} …
 * values; here they arrive on a REST request as a symbolic key name.
 */
public enum PfKey {

    ENTER(CommonAction.SUBMIT),
    PF3(CommonAction.BACK),
    PF4(CommonAction.CLEAR),
    PF7(CommonAction.PAGE_UP),
    PF8(CommonAction.PAGE_DOWN),
    PF12(CommonAction.CANCEL);

    private final CommonAction action;

    PfKey(CommonAction action) {
        this.action = action;
    }

    public CommonAction action() {
        return action;
    }

    /** Resolve from a request value (e.g. {@code "PF3"}, {@code "pf3"}); defaults to ENTER. */
    public static PfKey from(String value) {
        if (value == null || value.isBlank()) {
            return ENTER;
        }
        String v = value.trim().toUpperCase();
        for (PfKey key : values()) {
            if (key.name().equals(v)) {
                return key;
            }
        }
        return ENTER;
    }
}
