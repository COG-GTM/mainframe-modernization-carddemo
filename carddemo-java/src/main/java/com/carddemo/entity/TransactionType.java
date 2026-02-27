package com.carddemo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "transaction_types")
public class TransactionType {
    @Id @Column(name = "tran_type", length = 2) private String tranType;
    @Column(name = "tran_type_desc", length = 50) private String tranTypeDesc;
    public TransactionType() {}
    public String getTranType() { return tranType; }
    public void setTranType(String v) { this.tranType = v; }
    public String getTranTypeDesc() { return tranTypeDesc; }
    public void setTranTypeDesc(String v) { this.tranTypeDesc = v; }
}
