package com.carddemo.web.transaction.dto;

/**
 * Add-transaction request payload for {@code POST /api/transactions} — the input fields of
 * the {@code COTRN02} (Add Transaction) map ({@code COTRN2AI}).
 *
 * <p>Either {@code acctId} or {@code cardNum} identifies the card via the cross-reference
 * ({@code CXACAIX}/{@code CCXREF} in {@code VALIDATE-INPUT-KEY-FIELDS}); the remaining fields
 * are validated exactly as {@code VALIDATE-INPUT-DATA-FIELDS} does. {@code amount} is the raw
 * {@code -99999999.99} string so its format can be checked byte-for-byte like the COBOL;
 * {@code confirm} reproduces the {@code CONFIRMI} Y/N gate.</p>
 *
 * @param acctId        ACTIDINI — account id (PIC 9(11)); alternative key to cardNum
 * @param cardNum       CARDNINI — card number (PIC 9(16)); alternative key to acctId
 * @param typeCd        TTYPCDI  — TRAN-TYPE-CD PIC X(02) (numeric)
 * @param categoryCd    TCATCDI  — TRAN-CAT-CD PIC 9(04)
 * @param source        TRNSRCI  — TRAN-SOURCE PIC X(10)
 * @param description   TDESCI   — TRAN-DESC PIC X(100)
 * @param amount        TRNAMTI  — TRAN-AMT as -99999999.99
 * @param origDate      TORIGDTI — origination date YYYY-MM-DD
 * @param procDate      TPROCDTI — processing date YYYY-MM-DD
 * @param merchantId    MIDI     — TRAN-MERCHANT-ID PIC 9(09)
 * @param merchantName  MNAMEI   — TRAN-MERCHANT-NAME PIC X(50)
 * @param merchantCity  MCITYI   — TRAN-MERCHANT-CITY PIC X(50)
 * @param merchantZip   MZIPI    — TRAN-MERCHANT-ZIP PIC X(10)
 * @param confirm       CONFIRMI — Y to commit the write; N/blank prompts to confirm
 */
public record TransactionAddRequest(
        String acctId,
        String cardNum,
        String typeCd,
        String categoryCd,
        String source,
        String description,
        String amount,
        String origDate,
        String procDate,
        String merchantId,
        String merchantName,
        String merchantCity,
        String merchantZip,
        String confirm) {
}
