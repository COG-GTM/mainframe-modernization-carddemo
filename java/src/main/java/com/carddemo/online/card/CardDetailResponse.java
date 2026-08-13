package com.carddemo.online.card;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COCRDSLC — SEND MAP of map CCRDSLA (mapset COCRDSL), fields taken
 * from CARD-RECORD (copybook CVACT02Y) plus INFOMSGO / ERRMSGO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDetailResponse {

    /** WS-INFO-MSG. */
    private String infoMessage;

    /** WS-RETURN-MSG. */
    private String errorMessage;

    /** FLG-ACCTFILTER-NOT-OK / FLG-CARDFILTER-NOT-OK. */
    private Map<String, String> fieldErrors;

    /** FOUND-CARDS-FOR-ACCOUNT. */
    private boolean cardFound;

    private String accountId;
    private String cardNumber;
    /** CARD-EMBOSSED-NAME. */
    private String embossedName;
    /** CARD-EXPIRAION-DATE(1:4). */
    private String expiryYear;
    /** CARD-EXPIRAION-DATE(6:2). */
    private String expiryMonth;
    /** CARD-ACTIVE-STATUS. */
    private String activeStatus;

    private String nextProgram;
    private String nextTransaction;
    private String nextMapset;
    private String nextMap;
}
