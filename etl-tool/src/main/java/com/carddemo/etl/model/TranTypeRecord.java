package com.carddemo.etl.model;

/**
 * Maps to CVTRA03Y.cpy - Transaction type record (RECLN 60).
 */
public class TranTypeRecord {

    private String tranType;        // PIC X(02)
    private String tranTypeDesc;    // PIC X(50)

    public String getTranType() { return tranType; }
    public void setTranType(String tranType) { this.tranType = tranType; }

    public String getTranTypeDesc() { return tranTypeDesc; }
    public void setTranTypeDesc(String tranTypeDesc) { this.tranTypeDesc = tranTypeDesc; }
}
