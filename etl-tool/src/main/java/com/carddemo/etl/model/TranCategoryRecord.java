package com.carddemo.etl.model;

/**
 * Maps to CVTRA04Y.cpy - Transaction category record (RECLN 60).
 */
public class TranCategoryRecord {

    private String tranTypeCd;          // PIC X(02)
    private int tranCatCd;              // PIC 9(04)
    private String tranCatTypeDesc;     // PIC X(50)

    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }

    public int getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(int tranCatCd) { this.tranCatCd = tranCatCd; }

    public String getTranCatTypeDesc() { return tranCatTypeDesc; }
    public void setTranCatTypeDesc(String tranCatTypeDesc) { this.tranCatTypeDesc = tranCatTypeDesc; }
}
