package com.carddemo.billpay.web.dto;

import com.carddemo.billpay.service.BillPaymentResult;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** JSON view of a {@link BillPaymentResult} for the Angular Bill Payment screen. */
public class BillPaymentResponse {

    private String messageType;   // SUCCESS | ERROR | INFO
    private String message;
    private BigDecimal balance;
    private String balanceDisplay; // mirrors COBOL PIC +9999999999.99 (14 chars), e.g. "+0000000123.45"
    private Long accountId;
    private String transactionId;
    private String fieldInError;   // NONE | ACCT_ID | CONFIRM
    private boolean cleared;

    public static BillPaymentResponse from(BillPaymentResult r) {
        BillPaymentResponse resp = new BillPaymentResponse();
        resp.messageType = r.getMessageType().name();
        resp.message = r.getMessage();
        resp.balance = r.getBalance();
        resp.balanceDisplay = formatBalance(r.getBalance());
        resp.accountId = r.getAccountId();
        resp.transactionId = r.getTransactionId();
        resp.fieldInError = r.getFieldInError().name();
        resp.cleared = r.isCleared();
        return resp;
    }

    /** Format like COBOL {@code PIC +9999999999.99}: leading sign, 10 integer digits, 2 decimals. */
    static String formatBalance(BigDecimal value) {
        if (value == null) {
            return "";
        }
        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_UP);
        String sign = scaled.signum() < 0 ? "-" : "+";
        BigDecimal abs = scaled.abs();
        String plain = abs.toPlainString();           // e.g. "123.45"
        int dot = plain.indexOf('.');
        String intPart = plain.substring(0, dot);
        String fracPart = plain.substring(dot + 1);
        String paddedInt = String.format("%010d", Long.parseLong(intPart));
        return sign + paddedInt + "." + fracPart;
    }

    public String getMessageType() { return messageType; }
    public String getMessage() { return message; }
    public BigDecimal getBalance() { return balance; }
    public String getBalanceDisplay() { return balanceDisplay; }
    public Long getAccountId() { return accountId; }
    public String getTransactionId() { return transactionId; }
    public String getFieldInError() { return fieldInError; }
    public boolean isCleared() { return cleared; }
}
