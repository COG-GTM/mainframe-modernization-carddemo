package com.carddemo.session;

import java.util.Collections;
import java.util.Map;

/**
 * Input to a {@link ScreenHandler} turn: the PF key pressed and the raw screen fields.
 * Mirrors the receive-map step of an online program (the BMS input fields plus
 * {@code EIBAID}).
 */
public class ScreenRequest {

    private final PfKey pfKey;
    private final Map<String, String> fields;

    public ScreenRequest(PfKey pfKey, Map<String, String> fields) {
        this.pfKey = pfKey == null ? PfKey.ENTER : pfKey;
        this.fields = fields == null ? Map.of() : Collections.unmodifiableMap(fields);
    }

    public PfKey pfKey() {
        return pfKey;
    }

    public CommonAction action() {
        return pfKey.action();
    }

    public Map<String, String> fields() {
        return fields;
    }

    public String field(String name) {
        return fields.get(name);
    }
}
