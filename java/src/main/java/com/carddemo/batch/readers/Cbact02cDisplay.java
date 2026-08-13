package com.carddemo.batch.readers;

import com.carddemo.model.entity.Card;
import java.util.List;

/**
 * COBOL program: CBACT02C — read and print the card master file (JCL READCARD).
 * Copybook CVACT02Y (CARD-RECORD, RECLN 150), VSAM file CARDDATA.
 *
 * <p>CBACT02C has no {@code 1100-} display paragraph: the DISPLAY inside
 * {@code 1000-CARDFILE-GET-NEXT} is commented out, so each record produces exactly one
 * group-level {@code DISPLAY CARD-RECORD} line from the main read loop.
 */
final class Cbact02cDisplay {

    private Cbact02cDisplay() {
    }

    static List<String> displayLines(Card card) {
        return List.of(record(card));
    }

    /** The 150 byte CARD-RECORD as laid out by CVACT02Y, trailing FILLER included. */
    static String record(Card card) {
        return CobolDisplay.text(card.getCardNumber(), 16)
                + CobolDisplay.number(card.getAccountId(), 11)
                + CobolDisplay.number(card.getCvvCode(), 3)
                + CobolDisplay.text(card.getEmbossedName(), 50)
                + CobolDisplay.text(card.getExpirationDate(), 10)
                + CobolDisplay.text(card.getActiveStatus(), 1)
                + " ".repeat(59);
    }
}
