package com.carddemo.etl.model;

import java.math.BigDecimal;

/**
 * Maps to CVTRA01Y.cpy - Transaction category balance record (RECLN 50).
 */
public class TranCatBalRecord {

    private long trancatAcctId;      // PIC 9(11)
    private String trancatTypeCd;    // PIC X(02)
    private int trancatCd;           // PIC 9(04)
    private BigDecimal tranCatBal;   // PIC S9(09)V99

    public long getTrancatAcctId() { return trancatAcctId; }
    public void setTrancatAcctId(long trancatAcctId) { this.trancatAcctId = trancatAcctId; }

    public String getTrancatTypeCd() { return trancatTypeCd; }
    public void setTrancatTypeCd(String trancatTypeCd) { this.trancatTypeCd = trancatTypeCd; }

    public int getTrancatCd() { return trancatCd; }
    public void setTrancatCd(int trancatCd) { this.trancatCd = trancatCd; }

    public BigDecimal getTranCatBal() { return tranCatBal; }
    public void setTranCatBal(BigDecimal tranCatBal) { this.tranCatBal = tranCatBal; }
}
