package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Daily transaction entity — maps COBOL copybook {@code CVTRA06Y} (DALYTRAN-RECORD, RECLN 350).
 *
 * <p>Input file (DALYTRAN) read by {@code CBTRN02C} during transaction posting. Seed file
 * {@code dailytran.txt}. Shares the {@link TransactionBase} layout with {@link Transaction}.</p>
 */
@Entity
@Table(name = "daily_transaction")
public class DailyTransaction extends TransactionBase {

    /** DALYTRAN-ID PIC X(16) — 16-char transaction id. */
    @Id
    @Column(name = "dalytran_id", length = 16, nullable = false)
    private String dalytranId;

    public DailyTransaction() {
    }

    public String getDalytranId() {
        return dalytranId;
    }

    public void setDalytranId(String dalytranId) {
        this.dalytranId = dalytranId;
    }
}
