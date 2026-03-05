package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity derived from COBOL copybook CVTRA05Y.cpy (TRAN-RECORD).
 *
 * <pre>
 * 01  TRAN-RECORD.
 *     05  TRAN-ID               PIC X(16).
 *     05  TRAN-TYPE-CD          PIC X(02).
 *     05  TRAN-CAT-CD           PIC 9(04).
 *     05  TRAN-SOURCE           PIC X(10).
 *     05  TRAN-DESC             PIC X(100).
 *     05  TRAN-AMT              PIC S9(09)V99.
 *     05  TRAN-MERCHANT-ID      PIC 9(09).
 *     05  TRAN-MERCHANT-NAME    PIC X(50).
 *     05  TRAN-MERCHANT-CITY    PIC X(50).
 *     05  TRAN-MERCHANT-ZIP     PIC X(10).
 *     05  TRAN-CARD-NUM         PIC X(16).
 *     05  TRAN-ORIG-TS          PIC X(26).
 *     05  TRAN-PROC-TS          PIC X(26).
 * </pre>
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @Column(name = "transaction_id", length = 16, nullable = false)
    @Size(max = 16)
    @NotNull
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
}
