package com.carddemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Transaction category reference (TRANCATG) — maps COBOL copybook {@code CVTRA04Y}
 * (TRAN-CAT-RECORD, RECLN 60). Seed file {@code trancatg.txt}.
 */
@Entity
@Table(name = "transaction_category")
public class TransactionCategory {

    @EmbeddedId
    private TransactionCategoryId id;

    /** TRAN-CAT-TYPE-DESC PIC X(50). */
    @Column(name = "tran_cat_type_desc", length = 50)
    private String tranCatTypeDesc;

    public TransactionCategory() {
    }

    public TransactionCategoryId getId() {
        return id;
    }

    public void setId(TransactionCategoryId id) {
        this.id = id;
    }

    public String getTranCatTypeDesc() {
        return tranCatTypeDesc;
    }

    public void setTranCatTypeDesc(String tranCatTypeDesc) {
        this.tranCatTypeDesc = tranCatTypeDesc;
    }
}
