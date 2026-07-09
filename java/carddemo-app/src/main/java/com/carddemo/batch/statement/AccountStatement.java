package com.carddemo.batch.statement;

import java.math.BigDecimal;
import java.util.List;

/**
 * Processed, ready-to-render account statement — the {@code ItemProcessor} output of
 * {@code creastmtJob}. Mirrors the data that legacy {@code CBSTM03A} gathers before writing a
 * statement: the account's basic details, the cardholder name/address (from {@code CUSTFILE})
 * and the account's transaction lines with the exact-scale {@link BigDecimal} total (COBOL
 * {@code WS-TOTAL-AMT}, PIC S9(9)V99).
 *
 * @param acctId       ACCT-ID PIC 9(11) — account id (String, leading zeros preserved)
 * @param cardholderName cardholder full name (CUST-FIRST/MIDDLE/LAST-NAME, COBOL {@code ST-NAME})
 * @param addressLine1 CUST-ADDR-LINE-1 PIC X(50)
 * @param addressLine2 CUST-ADDR-LINE-2 PIC X(50)
 * @param addressLine3 street line 3 + state + country + zip (COBOL {@code ST-ADD3})
 * @param currentBalance ACCT-CURR-BAL PIC S9(10)V99
 * @param ficoScore    CUST-FICO-CREDIT-SCORE PIC 9(03)
 * @param transactions the account's transaction lines for the statement period
 * @param totalAmount  sum of {@link StatementTransaction#amount()} (COBOL {@code WS-TOTAL-AMT})
 */
public record AccountStatement(
        String acctId,
        String cardholderName,
        String addressLine1,
        String addressLine2,
        String addressLine3,
        BigDecimal currentBalance,
        Integer ficoScore,
        List<StatementTransaction> transactions,
        BigDecimal totalAmount) {

    /**
     * A single transaction row on the statement — the fields {@code CBSTM03A} renders per
     * {@code COSTM01} ({@code ST-LINE14}: TRNX-ID, TRNX-DESC, TRNX-AMT).
     *
     * @param tranId      TRNX-ID PIC X(16)
     * @param description TRNX-DESC PIC X(100) (rendered truncated to 49 in the text layout)
     * @param amount      TRNX-AMT PIC S9(09)V99
     */
    public record StatementTransaction(String tranId, String description, BigDecimal amount) {
    }
}
