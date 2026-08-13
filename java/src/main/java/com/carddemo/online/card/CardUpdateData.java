package com.carddemo.online.card;

import com.carddemo.model.entity.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDUPC — CCUP-OLD-DETAILS / CCUP-NEW-DETAILS of
 * WS-THIS-PROGCOMMAREA, holding the card data (record layout CVACT02Y) as fetched and
 * as keyed by the user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardUpdateData {

    /** CCUP-x-ACCTID. */
    private String accountId;
    /** CCUP-x-CARDID. */
    private String cardNumber;
    /** CCUP-x-CVV-CD. */
    private String cvvCode;
    /** CCUP-x-CRDNAME. */
    private String embossedName;
    /** CCUP-x-EXPYEAR — CARD-EXPIRAION-DATE(1:4). */
    private String expiryYear;
    /** CCUP-x-EXPMON — CARD-EXPIRAION-DATE(6:2). */
    private String expiryMonth;
    /** CCUP-x-EXPDAY — CARD-EXPIRAION-DATE(9:2). */
    private String expiryDay;
    /** CCUP-x-CRDSTCD. */
    private String activeStatus;

    /** 9000-READ-DATA: CARD-EMBOSSED-NAME is folded to upper case when fetched. */
    public static CardUpdateData fromEntity(Card card) {
        String expiry = card.getExpirationDate() == null ? "" : card.getExpirationDate();
        return CardUpdateData.builder()
                .accountId(card.getAccountId() == null ? null : String.format("%011d", card.getAccountId()))
                .cardNumber(card.getCardNumber())
                .cvvCode(card.getCvvCode() == null ? null : String.format("%03d", card.getCvvCode()))
                .embossedName(card.getEmbossedName() == null ? null : card.getEmbossedName().toUpperCase())
                .expiryYear(part(expiry, 0, 4))
                .expiryMonth(part(expiry, 5, 7))
                .expiryDay(part(expiry, 8, 10))
                .activeStatus(card.getActiveStatus())
                .build();
    }

    /**
     * FUNCTION UPPER-CASE(CCUP-NEW-CARDDATA) EQUAL FUNCTION UPPER-CASE(CCUP-OLD-CARDDATA):
     * the whole card data group is compared, case insensitively.
     */
    public boolean sameAs(CardUpdateData other) {
        if (other == null) {
            return false;
        }
        return equalsIgnoreCase(embossedName, other.embossedName)
                && equalsIgnoreCase(activeStatus, other.activeStatus)
                && equalsIgnoreCase(expiryYear, other.expiryYear)
                && equalsIgnoreCase(expiryMonth, other.expiryMonth)
                && equalsIgnoreCase(expiryDay, other.expiryDay);
    }

    /** CARD-EXPIRAION-DATE built by the STRING of 9200-WRITE-PROCESSING. */
    public String expirationDate() {
        return expiryYear + "-" + expiryMonth + "-" + expiryDay;
    }

    private static boolean equalsIgnoreCase(String left, String right) {
        String a = left == null ? "" : left.trim();
        String b = right == null ? "" : right.trim();
        return a.equalsIgnoreCase(b);
    }

    private static String part(String value, int from, int to) {
        return value.length() >= to ? value.substring(from, to) : null;
    }
}
