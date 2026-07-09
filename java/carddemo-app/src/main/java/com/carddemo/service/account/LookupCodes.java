package com.carddemo.service.account;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * In-memory copy of the CardDemo lookup tables held in copybook {@code CSLKPCDY.cpy} — the
 * North-America phone area codes ({@code VALID-GENERAL-PURP-CODE}), US state codes
 * ({@code VALID-US-STATE-CODE}) and valid state + first-two-of-zip combinations
 * ({@code VALID-US-STATE-ZIP-CD2-COMBO}). The values were extracted verbatim from the
 * copybook into the {@code cs4/*.txt} classpath resources so the ported edits accept
 * exactly the same set the COBOL program did.
 */
@Component
public class LookupCodes {

    private final Set<String> areaCodes;
    private final Set<String> stateCodes;
    private final Set<String> stateZip2;

    public LookupCodes() {
        this.areaCodes = load("cs4/us-area-codes.txt");
        this.stateCodes = load("cs4/us-state-codes.txt");
        this.stateZip2 = load("cs4/us-state-zip2.txt");
    }

    private static Set<String> load(String resource) {
        Set<String> values = new HashSet<>();
        ClassPathResource cp = new ClassPathResource(resource);
        try (InputStream in = cp.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    values.add(trimmed);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load lookup resource " + resource, e);
        }
        return Set.copyOf(values);
    }

    /** {@code VALID-GENERAL-PURP-CODE} — valid North-America general-purpose area code. */
    public boolean isValidAreaCode(String areaCode) {
        return areaCode != null && areaCodes.contains(areaCode.trim());
    }

    /** {@code VALID-US-STATE-CODE} — valid US state code. */
    public boolean isValidStateCode(String stateCode) {
        return stateCode != null && stateCodes.contains(stateCode.trim().toUpperCase());
    }

    /** {@code VALID-US-STATE-ZIP-CD2-COMBO} — state code + first two zip digits combination. */
    public boolean isValidStateZip2(String stateCode, String zip) {
        if (stateCode == null || zip == null) {
            return false;
        }
        String st = stateCode.trim().toUpperCase();
        String zip2 = zip.trim();
        if (st.length() != 2 || zip2.length() < 2) {
            return false;
        }
        return stateZip2.contains(st + zip2.substring(0, 2));
    }
}
