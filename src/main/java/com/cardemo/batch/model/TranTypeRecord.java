package com.cardemo.batch.model;

/**
 * Maps to CVTRA03Y.cpy TRAN-TYPE-RECORD (60 bytes).
 * Transaction type reference table.
 */
public class TranTypeRecord {

    private String tranType;      // PIC X(02)
    private String tranTypeDesc;  // PIC X(50)

    public TranTypeRecord() {
    }

    public TranTypeRecord(String tranType, String tranTypeDesc) {
        this.tranType = tranType;
        this.tranTypeDesc = tranTypeDesc;
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
