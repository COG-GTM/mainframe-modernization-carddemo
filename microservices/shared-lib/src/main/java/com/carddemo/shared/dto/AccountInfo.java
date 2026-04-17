package com.carddemo.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps to CDEMO-ACCOUNT-INFO in COCOM01Y.cpy.
 *
 * <pre>
 * 05 CDEMO-ACCOUNT-INFO.
 *   10 CDEMO-ACCT-ID             PIC 9(11).
 *   10 CDEMO-ACCT-STATUS         PIC X(01).
 * </pre>
 */
public class AccountInfo {

    @JsonProperty("acctId")
    private long acctId;

    @JsonProperty("acctStatus")
    private String acctStatus;

    public AccountInfo() {
    }

    public AccountInfo(long acctId, String acctStatus) {
        this.acctId = acctId;
        this.acctStatus = acctStatus;
    }

    public long getAcctId() {
        return acctId;
    }

    public void setAcctId(long acctId) {
        this.acctId = acctId;
    }

    public String getAcctStatus() {
        return acctStatus;
    }

    public void setAcctStatus(String acctStatus) {
        this.acctStatus = acctStatus;
    }
}
