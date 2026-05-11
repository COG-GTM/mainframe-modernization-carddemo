package uk.co.nationwide.unified.nationwide;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Nationwide card-platform account record, mapped from the CardDemo VSAM
 * copybook <code>CVACT01Y.cpy</code> (ACCOUNT-RECORD, 300-byte fixed).
 */
@Entity
@Table(name = "nw_account")
public class NwAccountEntity {

    @Id
    @Column(name = "acct_id")
    private Long acctId;

    @Column(name = "acct_active_status", length = 1, nullable = false)
    private String acctActiveStatus;

    @Column(name = "acct_curr_bal", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCurrBal;

    @Column(name = "acct_credit_limit", precision = 12, scale = 2, nullable = false)
    private BigDecimal acctCreditLimit;

    @Column(name = "acct_open_date")
    private LocalDate acctOpenDate;

    @Column(name = "acct_expiration_date")
    private LocalDate acctExpirationDate;

    @Column(name = "acct_addr_zip", length = 10)
    private String acctAddrZip;

    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
    public String getAcctActiveStatus() { return acctActiveStatus; }
    public void setAcctActiveStatus(String acctActiveStatus) { this.acctActiveStatus = acctActiveStatus; }
    public BigDecimal getAcctCurrBal() { return acctCurrBal; }
    public void setAcctCurrBal(BigDecimal acctCurrBal) { this.acctCurrBal = acctCurrBal; }
    public BigDecimal getAcctCreditLimit() { return acctCreditLimit; }
    public void setAcctCreditLimit(BigDecimal acctCreditLimit) { this.acctCreditLimit = acctCreditLimit; }
    public LocalDate getAcctOpenDate() { return acctOpenDate; }
    public void setAcctOpenDate(LocalDate acctOpenDate) { this.acctOpenDate = acctOpenDate; }
    public LocalDate getAcctExpirationDate() { return acctExpirationDate; }
    public void setAcctExpirationDate(LocalDate acctExpirationDate) { this.acctExpirationDate = acctExpirationDate; }
    public String getAcctAddrZip() { return acctAddrZip; }
    public void setAcctAddrZip(String acctAddrZip) { this.acctAddrZip = acctAddrZip; }
}
