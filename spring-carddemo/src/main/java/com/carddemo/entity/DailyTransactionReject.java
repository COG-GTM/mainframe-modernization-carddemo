package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity for rejected daily transactions.
 * Same fields as DailyTransaction plus reject reason fields.
 * Derived from the reject logic in CBTRN02C.cbl.
 */
@Entity
@Table(name = "daily_transaction_rejects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTransactionReject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "transaction_id", length = 16)
    @Size(max = 16)
    private String transactionId;

    @Column(name = "transaction_type", length = 2)
    @Size(max = 2)
    private String transactionType;

    @Column(name = "transaction_category")
    private Integer transactionCategory;

    @Column(name = "transaction_source", length = 10)
    @Size(max = 10)
    private String transactionSource;

    @Column(name = "transaction_description", length = 100)
    @Size(max = 100)
    private String transactionDescription;

    @Column(name = "transaction_amount", precision = 11, scale = 2)
    private BigDecimal transactionAmount;

    @Column(name = "merchant_id", length = 9)
    @Size(max = 9)
    private String merchantId;

    @Column(name = "merchant_name", length = 50)
    @Size(max = 50)
    private String merchantName;

    @Column(name = "merchant_city", length = 50)
    @Size(max = 50)
    private String merchantCity;

    @Column(name = "merchant_zip", length = 10)
    @Size(max = 10)
    private String merchantZip;

    @Column(name = "card_number", length = 16)
    @Size(max = 16)
    private String cardNumber;

    @Column(name = "origin_timestamp", length = 26)
    @Size(max = 26)
    private String originTimestamp;

    @Column(name = "processing_timestamp", length = 26)
    @Size(max = 26)
    private String processingTimestamp;

    @Column(name = "reject_reason_code")
    private Integer rejectReasonCode;

    @Column(name = "reject_reason_description", length = 200)
    @Size(max = 200)
    private String rejectReasonDescription;
}
