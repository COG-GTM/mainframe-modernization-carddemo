package com.carddemo.etl.model;

import java.math.BigDecimal;

/**
 * Maps to CVTRA02Y.cpy - Disclosure group record (RECLN 50).
 */
public class DisclosureGroupRecord {

    private String disAcctGroupId;    // PIC X(10)
    private String disTranTypeCd;     // PIC X(02)
    private int disTranCatCd;         // PIC 9(04)
    private BigDecimal disIntRate;    // PIC S9(04)V99

    public String getDisAcctGroupId() { return disAcctGroupId; }
    public void setDisAcctGroupId(String disAcctGroupId) { this.disAcctGroupId = disAcctGroupId; }

    public String getDisTranTypeCd() { return disTranTypeCd; }
    public void setDisTranTypeCd(String disTranTypeCd) { this.disTranTypeCd = disTranTypeCd; }

    public int getDisTranCatCd() { return disTranCatCd; }
    public void setDisTranCatCd(int disTranCatCd) { this.disTranCatCd = disTranCatCd; }

    public BigDecimal getDisIntRate() { return disIntRate; }
    public void setDisIntRate(BigDecimal disIntRate) { this.disIntRate = disIntRate; }
}
