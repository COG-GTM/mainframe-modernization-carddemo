package com.carddemo.billpay.service;

import java.math.BigDecimal;

/**
 * Outcome of the Bill Payment transaction — the observable result of COBIL00C.PROCESS-ENTER-KEY.
 * {@code messageType} drives the screen colour: SUCCESS renders GREEN (DFHGREEN in the map),
 * ERROR/INFO render RED (the map's default ERRMSG colour).
 */
public class BillPaymentResult {

    public enum MessageType { SUCCESS, ERROR, INFO }

    /** Which input field the cursor/error is attached to, mirroring the COBOL {@code MOVE -1 TO ...L}. */
    public enum Field { NONE, ACCT_ID, CONFIRM }

    private final MessageType messageType;
    private final String message;
    private final BigDecimal balance;
    private final Long accountId;
    private final String transactionId;
    private final Field fieldInError;
    private final boolean cleared;

    private BillPaymentResult(MessageType messageType, String message, BigDecimal balance,
                             Long accountId, String transactionId, Field fieldInError, boolean cleared) {
        this.messageType = messageType;
        this.message = message;
        this.balance = balance;
        this.accountId = accountId;
        this.transactionId = transactionId;
        this.fieldInError = fieldInError;
        this.cleared = cleared;
    }

    public static BillPaymentResult error(String message, Field field) {
        return new BillPaymentResult(MessageType.ERROR, message, null, null, null, field, false);
    }

    public static BillPaymentResult error(String message, Field field, BigDecimal balance, Long accountId) {
        return new BillPaymentResult(MessageType.ERROR, message, balance, accountId, null, field, false);
    }

    public static BillPaymentResult info(String message, Field field, BigDecimal balance, Long accountId) {
        return new BillPaymentResult(MessageType.INFO, message, balance, accountId, null, field, false);
    }

    public static BillPaymentResult success(String message, BigDecimal balance, Long accountId, String transactionId) {
        return new BillPaymentResult(MessageType.SUCCESS, message, balance, accountId, transactionId, Field.NONE, false);
    }

    public static BillPaymentResult cleared() {
        return new BillPaymentResult(MessageType.INFO, "", null, null, null, Field.ACCT_ID, true);
    }

    public MessageType getMessageType() { return messageType; }
    public String getMessage() { return message; }
    public BigDecimal getBalance() { return balance; }
    public Long getAccountId() { return accountId; }
    public String getTransactionId() { return transactionId; }
    public Field getFieldInError() { return fieldInError; }
    public boolean isCleared() { return cleared; }
}
