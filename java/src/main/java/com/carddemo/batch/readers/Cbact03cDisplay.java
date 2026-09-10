package com.carddemo.batch.readers;

import com.carddemo.model.entity.CardXref;
import java.util.List;

/**
 * COBOL program: CBACT03C — read and print the card cross reference file (JCL READXREF).
 * Copybook CVACT03Y (CARD-XREF-RECORD, RECLN 50), VSAM file CARDXREF.
 *
 * <p>CBACT03C displays the record twice per read: once in {@code 1000-XREFFILE-GET-NEXT}
 * and once in the main read loop. Both lines are kept so the SYSOUT matches the COBOL.
 */
final class Cbact03cDisplay {

    private Cbact03cDisplay() {
    }

    static List<String> displayLines(CardXref xref) {
        String line = record(xref);
        return List.of(line, line);
    }

    /** The 50 byte CARD-XREF-RECORD as laid out by CVACT03Y, trailing FILLER included. */
    static String record(CardXref xref) {
        return CobolDisplay.text(xref.getCardNumber(), 16)
                + CobolDisplay.number(xref.getCustomerId(), 9)
                + CobolDisplay.number(xref.getAccountId(), 11)
                + " ".repeat(14);
    }
}
