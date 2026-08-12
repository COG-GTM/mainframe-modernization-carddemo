package com.carddemo.online.common;

/**
 * COBOL program: CSUTLDTC — the {@code LS-RESULT PIC X(80)} answer built from the CEEDAYS
 * feedback code, plus the {@code RETURN-CODE} (WS-SEVERITY-N) it sets.
 *
 * @param severity WS-SEVERITY-N, 0 when the date is valid
 * @param messageNumber WS-MSG-NO-N, the CEEDAYS message number
 * @param resultText WS-RESULT PIC X(15), e.g. {@code "Date is valid"}
 * @param message the whole WS-MESSAGE record (80 characters, fixed width)
 */
public record LanguageEnvironmentDateResult(
        int severity, int messageNumber, String resultText, String message) {

    public boolean isValid() {
        return severity == 0;
    }
}
