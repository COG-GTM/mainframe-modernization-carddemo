package com.carddemo.account.dto;

import java.math.BigDecimal;

/**
 * Request DTO for account update operations.
 * Translates the updatable fields from COACTUPC:
 *   - ACCT-ACTIVE-STATUS (Y/N)
 *   - ACCT-CREDIT-LIMIT
 *   - ACCT-CASH-CREDIT-LIMIT
 *   - ACCT-GROUP-ID
 *   - ACCT-ADDR-ZIP (via customer update in original, included here for convenience)
 */
public class AccountRequest {

    private String acctActiveStatus;
    private BigDecimal acctCreditLimit;
    private BigDecimal acctCashCreditLimit;
    private String acctGroupId;
    private String acctAddrZip;

    public AccountRequest() {
    }

    public String getAcctActiveStatus() {
        return acctActiveStatus;
    }

    public void setAcctActiveStatus(String acctActiveStatus) {
        this.acctActiveStatus = acctActiveStatus;
    }

    public BigDecimal getAcctCreditLimit() {
        return acctCreditLimit;
    }

    public void setAcctCreditLimit(BigDecimal acctCreditLimit) {
        this.acctCreditLimit = acctCreditLimit;
    }

    public BigDecimal getAcctCashCreditLimit() {
        return acctCashCreditLimit;
    }

    public void setAcctCashCreditLimit(BigDecimal acctCashCreditLimit) {
        this.acctCashCreditLimit = acctCashCreditLimit;
    }

    public String getAcctGroupId() {
        return acctGroupId;
    }

    public void setAcctGroupId(String acctGroupId) {
        this.acctGroupId = acctGroupId;
    }

    public String getAcctAddrZip() {
        return acctAddrZip;
    }

    public void setAcctAddrZip(String acctAddrZip) {
        this.acctAddrZip = acctAddrZip;
    }
}
