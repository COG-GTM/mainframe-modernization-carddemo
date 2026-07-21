package com.carddemo.billpay.web.dto;

public class PayRequest {
    private String acctId;
    private String confirm;

    public String getAcctId() { return acctId; }
    public void setAcctId(String acctId) { this.acctId = acctId; }

    public String getConfirm() { return confirm; }
    public void setConfirm(String confirm) { this.confirm = confirm; }
}
