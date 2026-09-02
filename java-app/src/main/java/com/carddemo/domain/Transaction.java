package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

import java.math.BigDecimal;

/**
 * TRAN-RECORD / DALYTRAN-RECORD, copybooks CVTRA05Y and CVTRA06Y, LRECL 350.
 *
 * <p>Both layouts are field-for-field identical, so a single class covers TRANSACT records and the
 * daily transaction input file (dailytran.txt).
 */
public class Transaction {

    public static final int RECORD_LENGTH = 350;

    private String transactionId;
    private String typeCode;
    private int categoryCode;
    private String source;
    private String description;
    private BigDecimal amount;
    private String merchantId;
    private String merchantName;
    private String merchantCity;
    private String merchantZip;
    private String cardNumber;
    private String originTimestamp;
    private String processTimestamp;

    public static Transaction parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        Transaction transaction = new Transaction();
        transaction.transactionId = cursor.fixed(16);
        transaction.typeCode = cursor.fixed(2);
        transaction.categoryCode = cursor.integer(4);
        transaction.source = cursor.text(10);
        transaction.description = cursor.text(100);
        transaction.amount = cursor.signed(11, 2);
        transaction.merchantId = cursor.fixed(9);
        transaction.merchantName = cursor.text(50);
        transaction.merchantCity = cursor.text(50);
        transaction.merchantZip = cursor.text(10);
        transaction.cardNumber = cursor.fixed(16);
        transaction.originTimestamp = cursor.text(26);
        transaction.processTimestamp = cursor.text(26);
        return transaction;
    }

    public String format() {
        return CobolCodec.encodeText(transactionId, 16)
                + CobolCodec.encodeText(typeCode, 2)
                + CobolCodec.encodeNumeric(categoryCode, 4)
                + CobolCodec.encodeText(source, 10)
                + CobolCodec.encodeText(description, 100)
                + CobolCodec.encodeSigned(amount, 11, 2)
                + CobolCodec.encodeText(merchantId, 9)
                + CobolCodec.encodeText(merchantName, 50)
                + CobolCodec.encodeText(merchantCity, 50)
                + CobolCodec.encodeText(merchantZip, 10)
                + CobolCodec.encodeText(cardNumber, 16)
                + CobolCodec.encodeText(originTimestamp, 26)
                + CobolCodec.encodeText(processTimestamp, 26)
                + " ".repeat(20);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantCity() {
        return merchantCity;
    }

    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }

    public String getMerchantZip() {
        return merchantZip;
    }

    public void setMerchantZip(String merchantZip) {
        this.merchantZip = merchantZip;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getOriginTimestamp() {
        return originTimestamp;
    }

    public void setOriginTimestamp(String originTimestamp) {
        this.originTimestamp = originTimestamp;
    }

    public String getProcessTimestamp() {
        return processTimestamp;
    }

    public void setProcessTimestamp(String processTimestamp) {
        this.processTimestamp = processTimestamp;
    }

    @Override
    public String toString() {
        return "Transaction[" + transactionId + ", card=" + cardNumber + ", amount=" + amount + "]";
    }
}
