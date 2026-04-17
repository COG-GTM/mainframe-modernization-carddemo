package com.carddemo.card.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * JPA entity mapping CVACT02Y.cpy CARD-RECORD (150-byte VSAM layout).
 *
 * Migrated from: COCRDLIC.cbl, COCRDSLC.cbl, COCRDUPC.cbl
 * VSAM file: CARDDAT (KSDS, primary key = CARD-NUM)
 *
 * COBOL copybook layout:
 *   05 CARD-NUM               PIC X(16)   → cardNumber (PK)
 *   05 CARD-ACCT-ID           PIC 9(11)   → accountId
 *   05 CARD-CVV-CD            PIC 9(03)   → cvvCode
 *   05 CARD-EMBOSSED-NAME     PIC X(50)   → embossedName
 *   05 CARD-EXPIRAION-DATE    PIC X(10)   → expirationDate
 *   05 CARD-ACTIVE-STATUS     PIC X(01)   → activeStatus
 *   05 FILLER                 PIC X(59)   → (dropped)
 */
@Entity
@Table(name = "card_data")
public class CardEntity {

    @Id
    @Column(name = "card_num", length = 16, nullable = false)
    private String cardNumber;

    @Column(name = "card_acct_id", precision = 11, nullable = false)
    private Long accountId;

    @Column(name = "card_cvv_cd", precision = 3, nullable = false)
    private Integer cvvCode;

    @Column(name = "card_embossed_name", length = 50)
    private String embossedName;

    @Column(name = "card_expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "card_active_status", length = 1, nullable = false)
    private String activeStatus;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public CardEntity() {
    }

    public CardEntity(String cardNumber, Long accountId, Integer cvvCode,
                      String embossedName, String expirationDate, String activeStatus) {
        this.cardNumber = cardNumber;
        this.accountId = accountId;
        this.cvvCode = cvvCode;
        this.embossedName = embossedName;
        this.expirationDate = expirationDate;
        this.activeStatus = activeStatus;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getCvvCode() {
        return cvvCode;
    }

    public void setCvvCode(Integer cvvCode) {
        this.cvvCode = cvvCode;
    }

    public String getEmbossedName() {
        return embossedName;
    }

    public void setEmbossedName(String embossedName) {
        this.embossedName = embossedName;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
