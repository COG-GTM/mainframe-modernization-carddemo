package com.carddemo.shared.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Java mapping of COBOL copybook CVTRA03Y — Transaction Type (RECLN 60).
 * <pre>
 * 01 TRAN-TYPE-RECORD.
 *   05 TRAN-TYPE                  PIC X(02)
 *   05 TRAN-TYPE-DESC             PIC X(50)
 *   05 FILLER                     PIC X(08)
 * </pre>
 */
public class TranTypeRecord {

    @JsonProperty("tranType")
    private String tranType;

    @JsonProperty("tranTypeDesc")
    private String tranTypeDesc;

    public TranTypeRecord() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranTypeRecord that = (TranTypeRecord) o;
        return Objects.equals(tranType, that.tranType)
                && Objects.equals(tranTypeDesc, that.tranTypeDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tranType, tranTypeDesc);
    }

    @Override
    public String toString() {
        return "TranTypeRecord{" +
                "tranType='" + tranType + '\'' +
                ", tranTypeDesc='" + tranTypeDesc + '\'' +
                '}';
    }
}
