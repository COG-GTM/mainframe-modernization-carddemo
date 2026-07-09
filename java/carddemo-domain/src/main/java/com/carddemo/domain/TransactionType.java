package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Transaction type reference — maps COBOL copybook {@code CVTRA03Y}
 * (TRAN-TYPE-RECORD, RECLN 60). Seed file {@code trantype.txt}.
 */
@Entity
@Table(name = "transaction_type")
public class TransactionType {

    /** TRAN-TYPE PIC X(02) — 2-char type code. */
    @Id
    @Column(name = "tran_type", length = 2, nullable = false)
    private String tranType;

    /** TRAN-TYPE-DESC PIC X(50). */
    @Column(name = "tran_type_desc", length = 50)
    private String tranTypeDesc;

    public TransactionType() {
    }

    public String getTranType() {
        return tranType;
    }

    public void setTranType(String tranType) {
        this.tranType = tranType;
    }

    public String getTranTypeDesc() {
        return tranTypeDesc;
    }

    public void setTranTypeDesc(String tranTypeDesc) {
        this.tranTypeDesc = tranTypeDesc;
    }
}
