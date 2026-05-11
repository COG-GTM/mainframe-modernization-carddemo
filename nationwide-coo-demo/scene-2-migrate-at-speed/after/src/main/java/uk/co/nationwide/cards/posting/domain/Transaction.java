package uk.co.nationwide.cards.posting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Modernized form of the COBOL <code>TRAN-RECORD</code> defined in copybook
 * <code>CVTRA05Y.cpy</code> (350-byte VSAM KSDS record).
 *
 * <pre>
 *  COBOL field              PIC clause          Java type
 *  ---------------------    -----------------   -------------------
 *  TRAN-ID                  X(16)               String (PK)
 *  TRAN-TYPE-CD             X(02)               String
 *  TRAN-CAT-CD              9(04)               Integer
 *  TRAN-SOURCE              X(10)               String
 *  TRAN-DESC                X(100)              String
 *  TRAN-AMT                 S9(09)V99           BigDecimal
 *  TRAN-MERCHANT-ID         9(09)               Long
 *  TRAN-MERCHANT-NAME       X(50)               String
 *  TRAN-MERCHANT-CITY       X(50)               String
 *  TRAN-MERCHANT-ZIP        X(10)               String
 *  TRAN-CARD-NUM            X(16)               String
 *  TRAN-ORIG-TS             X(26)               Instant
 *  TRAN-PROC-TS             X(26)               Instant
 * </pre>
 */
@Entity
@Table(name = "transaction")
public class Transaction {

    @Id
    @Column(name = "tran_id", length = 16, nullable = false)
    private String tranId;

    @Column(name = "tran_type_cd", length = 2, nullable = false)
    private String tranTypeCd;

    @Column(name = "tran_cat_cd", nullable = false)
    private Integer tranCatCd;

    @Column(name = "tran_source", length = 10)
    private String tranSource;

    @Column(name = "tran_desc", length = 100)
    private String tranDesc;

    @Column(name = "tran_amt", precision = 11, scale = 2, nullable = false)
    private BigDecimal tranAmt;

    @Column(name = "tran_merchant_id")
    private Long tranMerchantId;

    @Column(name = "tran_merchant_name", length = 50)
    private String tranMerchantName;

    @Column(name = "tran_merchant_city", length = 50)
    private String tranMerchantCity;

    @Column(name = "tran_merchant_zip", length = 10)
    private String tranMerchantZip;

    @Column(name = "tran_card_num", length = 16, nullable = false)
    private String tranCardNum;

    @Column(name = "tran_orig_ts", nullable = false)
    private Instant tranOrigTs;

    @Column(name = "tran_proc_ts", nullable = false)
    private Instant tranProcTs;

    public Transaction() { }

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

    public Instant getTranProcTs() { return tranProcTs; }
    public void setTranProcTs(Instant tranProcTs) { this.tranProcTs = tranProcTs; }
}
