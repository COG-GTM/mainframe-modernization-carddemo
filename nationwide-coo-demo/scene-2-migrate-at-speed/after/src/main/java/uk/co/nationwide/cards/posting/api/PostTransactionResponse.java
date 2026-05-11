package uk.co.nationwide.cards.posting.api;

import java.math.BigDecimal;
import java.time.Instant;

public class PostTransactionResponse {

    private String tranId;
    private Long acctId;
    private BigDecimal acctNewBalance;
    private Instant processedAt;

    public PostTransactionResponse() { }

    public PostTransactionResponse(String tranId, Long acctId, BigDecimal acctNewBalance, Instant processedAt) {
        this.tranId = tranId;
        this.acctId = acctId;
        this.acctNewBalance = acctNewBalance;
        this.processedAt = processedAt;
    }

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }
    public Long getAcctId() { return acctId; }
    public void setAcctId(Long acctId) { this.acctId = acctId; }
    public BigDecimal getAcctNewBalance() { return acctNewBalance; }
    public void setAcctNewBalance(BigDecimal acctNewBalance) { this.acctNewBalance = acctNewBalance; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
