package com.carddemo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Java replacement for the {@code CSLKPCDY} copybook lookup tables: valid US phone area codes,
 * state codes and state + first two ZIP digits combinations.
 */
public final class LookupTables {

    private static final Set<String> PHONE_AREA_CODES = load("lookup/us-phone-area-codes.txt");
    private static final Set<String> STATE_CODES = load("lookup/us-state-codes.txt");
    private static final Set<String> STATE_ZIP_COMBOS = load("lookup/us-state-zip2-combos.txt");

    private LookupTables() {
    }

    public static boolean isValidPhoneAreaCode(String areaCode) {
        return areaCode != null && PHONE_AREA_CODES.contains(areaCode);
    }

    public static boolean isValidStateCode(String stateCode) {
        return stateCode != null && STATE_CODES.contains(stateCode.toUpperCase());
    }

    /** Checks the state code against the first two digits of the ZIP code, as COBOL did. */
    public static boolean isValidStateZipCombination(String stateCode, String zipCode) {
        if (stateCode == null || zipCode == null || zipCode.length() < 2) {
            return false;
        }
        return STATE_ZIP_COMBOS.contains(stateCode.toUpperCase() + zipCode.substring(0, 2));
    }

    private static Set<String> load(String resource) {
        try (InputStream in = LookupTables.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing lookup resource: " + resource);
            }
            Set<String> values = new LinkedHashSet<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        values.add(trimmed);
                    }
                }
            }
            return Collections.unmodifiableSet(values);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
