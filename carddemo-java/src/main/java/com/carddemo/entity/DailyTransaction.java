package com.carddemo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "daily_transactions")
public class DailyTransaction {
    @Id @Column(name = "tran_id", length = 16) private String tranId;
    @Column(name = "tran_type_cd", length = 2) private String tranTypeCd;
    @Column(name = "tran_cat_cd") private Integer tranCatCd;
    @Column(name = "tran_source", length = 10) private String tranSource;
    @Column(name = "tran_desc", length = 100) private String tranDesc;
    @Column(name = "tran_amt", precision = 11, scale = 2) private BigDecimal tranAmt;
    @Column(name = "tran_merchant_id") private Long tranMerchantId;
    @Column(name = "tran_merchant_name", length = 50) private String tranMerchantName;
    @Column(name = "tran_merchant_city", length = 50) private String tranMerchantCity;
    @Column(name = "tran_merchant_zip", length = 10) private String tranMerchantZip;
    @Column(name = "tran_card_num", length = 16) private String tranCardNum;
    @Column(name = "tran_orig_ts", length = 26) private String tranOrigTs;
    @Column(name = "tran_proc_ts", length = 26) private String tranProcTs;

    public DailyTransaction() {}
    public String getTranId() { return tranId; }
    public void setTranId(String v) { this.tranId = v; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String v) { this.tranTypeCd = v; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer v) { this.tranCatCd = v; }
    public String getTranSource() { return tranSource; }
    public void setTranSource(String v) { this.tranSource = v; }
    public String getTranDesc() { return tranDesc; }
    public void setTranDesc(String v) { this.tranDesc = v; }
    public BigDecimal getTranAmt() { return tranAmt; }
    public void setTranAmt(BigDecimal v) { this.tranAmt = v; }
    public Long getTranMerchantId() { return tranMerchantId; }
    public void setTranMerchantId(Long v) { this.tranMerchantId = v; }
    public String getTranMerchantName() { return tranMerchantName; }
    public void setTranMerchantName(String v) { this.tranMerchantName = v; }
    public String getTranMerchantCity() { return tranMerchantCity; }
    public void setTranMerchantCity(String v) { this.tranMerchantCity = v; }
    public String getTranMerchantZip() { return tranMerchantZip; }
    public void setTranMerchantZip(String v) { this.tranMerchantZip = v; }
    public String getTranCardNum() { return tranCardNum; }
    public void setTranCardNum(String v) { this.tranCardNum = v; }
    public String getTranOrigTs() { return tranOrigTs; }
    public void setTranOrigTs(String v) { this.tranOrigTs = v; }
    public String getTranProcTs() { return tranProcTs; }
    public void setTranProcTs(String v) { this.tranProcTs = v; }
}
