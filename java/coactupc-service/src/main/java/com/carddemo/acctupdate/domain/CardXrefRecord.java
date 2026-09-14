package com.carddemo.acctupdate.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "CARDXREF", indexes = @Index(name = "CXACAIX", columnList = "acctId"))
public class CardXrefRecord {
    @Id @Column(length = 16) private String cardNum;
    private Long custId;
    private Long acctId;
    public String getCardNum() { return cardNum; } public void setCardNum(String v) { cardNum = v; }
    public Long getCustId() { return custId; } public void setCustId(Long v) { custId = v; }
    public Long getAcctId() { return acctId; } public void setAcctId(Long v) { acctId = v; }
}
