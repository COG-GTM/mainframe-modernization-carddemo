package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

import java.math.BigDecimal;

/**
 * TRAN-CAT-BAL-RECORD, copybook CVTRA01Y, LRECL 50 (TCATBALF / tcatbal.txt).
 *
 * <p>Mutable because the posting (CBTRN02C) and interest (CBACT04C) batch steps accumulate into the
 * category balance.
 */
public class TransactionCategoryBalance {

    public static final int RECORD_LENGTH = 50;

    private String accountId;
    private String typeCode;
    private int categoryCode;
    private BigDecimal balance;

    public TransactionCategoryBalance() {
    }

    public TransactionCategoryBalance(String accountId, String typeCode, int categoryCode, BigDecimal balance) {
        this.accountId = accountId;
        this.typeCode = typeCode;
        this.categoryCode = categoryCode;
        this.balance = balance;
    }

    public static TransactionCategoryBalance parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        TransactionCategoryBalance balance = new TransactionCategoryBalance();
        balance.accountId = cursor.fixed(11);
        balance.typeCode = cursor.fixed(2);
        balance.categoryCode = cursor.integer(4);
        balance.balance = cursor.signed(11, 2);
        return balance;
    }

    public String format() {
        return CobolCodec.encodeText(accountId, 11)
                + CobolCodec.encodeText(typeCode, 2)
                + CobolCodec.encodeNumeric(categoryCode, 4)
                + CobolCodec.encodeSigned(balance, 11, 2)
                + " ".repeat(22);
    }

    /** Composite key TRAN-CAT-KEY: account id, transaction type code, transaction category code. */
    public String key() {
        return CobolCodec.encodeText(accountId, 11) + typeCode + CobolCodec.encodeNumeric(categoryCode, 4);
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "TransactionCategoryBalance[" + key() + ", balance=" + balance + "]";
    }
}
