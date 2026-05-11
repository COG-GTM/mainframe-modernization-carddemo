package uk.co.nationwide.cards.posting.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * REST representation of a transaction to be posted. Field names and lengths
 * preserve the COBOL <code>DALYTRAN-RECORD</code> layout (the daily
 * transaction feed processed by CBTRN02C).
 */
public class PostTransactionRequest {

    @NotBlank @Size(max = 16)
    @JsonProperty("tranId")
    private String tranId;

    @NotBlank @Size(min = 2, max = 2)
    @JsonProperty("tranTypeCd")
    private String tranTypeCd;

    @NotNull
    @JsonProperty("tranCatCd")
    private Integer tranCatCd;

    @Size(max = 10)
    @JsonProperty("tranSource")
    private String tranSource;

    @Size(max = 100)
    @JsonProperty("tranDesc")
    private String tranDesc;

    @NotNull
    @JsonProperty("tranAmt")
    private BigDecimal tranAmt;

    @JsonProperty("tranMerchantId")
    private Long tranMerchantId;

    @Size(max = 50)
    @JsonProperty("tranMerchantName")
    private String tranMerchantName;

    @Size(max = 50)
    @JsonProperty("tranMerchantCity")
    private String tranMerchantCity;

    @Size(max = 10)
    @JsonProperty("tranMerchantZip")
    private String tranMerchantZip;

    @NotBlank
    @Pattern(regexp = "\\d{16}")
    @JsonProperty("tranCardNum")
    private String tranCardNum;

    @NotNull
    @JsonProperty("tranOrigTs")
    private Instant tranOrigTs;

    public String getTranId() { return tranId; }
    public void setTranId(String tranId) { this.tranId = tranId; }
    public String getTranTypeCd() { return tranTypeCd; }
    public void setTranTypeCd(String tranTypeCd) { this.tranTypeCd = tranTypeCd; }
    public Integer getTranCatCd() { return tranCatCd; }
    public void setTranCatCd(Integer tranCatCd) { this.tranCatCd = tranCatCd; }
    public String getTranSource() { return tranSource; }
    public void setTranSource(String tranSource) { this.tranSource = tranSource; }
    public String getTranDesc() { return tranDesc; }
    public void setTranDesc(String tranDesc) { this.tranDesc = tranDesc; }
    public BigDecimal getTranAmt() { return tranAmt; }
    public void setTranAmt(BigDecimal tranAmt) { this.tranAmt = tranAmt; }
    public Long getTranMerchantId() { return tranMerchantId; }
    public void setTranMerchantId(Long tranMerchantId) { this.tranMerchantId = tranMerchantId; }
    public String getTranMerchantName() { return tranMerchantName; }
    public void setTranMerchantName(String tranMerchantName) { this.tranMerchantName = tranMerchantName; }
    public String getTranMerchantCity() { return tranMerchantCity; }
    public void setTranMerchantCity(String tranMerchantCity) { this.tranMerchantCity = tranMerchantCity; }
    public String getTranMerchantZip() { return tranMerchantZip; }
    public void setTranMerchantZip(String tranMerchantZip) { this.tranMerchantZip = tranMerchantZip; }
    public String getTranCardNum() { return tranCardNum; }
    public void setTranCardNum(String tranCardNum) { this.tranCardNum = tranCardNum; }
    public Instant getTranOrigTs() { return tranOrigTs; }
    public void setTranOrigTs(Instant tranOrigTs) { this.tranOrigTs = tranOrigTs; }
}
