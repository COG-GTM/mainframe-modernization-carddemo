package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Online transaction entity — maps COBOL copybook {@code CVTRA05Y} (TRAN-RECORD, RECLN 350).
 *
 * <p>VSAM KSDS keyed on {@code TRAN-ID} (see {@code CBTRN02C}: RECORD KEY IS FD-TRANS-ID).
 * Table only for this wave — no ASCII seed file exists for online transactions; they are
 * produced at runtime by transaction posting.</p>
 */
@Entity
@Table(name = "card_transaction")
public class Transaction extends TransactionBase {

    /** TRAN-ID PIC X(16) — 16-char transaction id. */
    @Id
    @Column(name = "tran_id", length = 16, nullable = false)
    private String tranId;

    public Transaction() {
    }

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }
}
