package com.carddemo.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL copybook: CVTRA05Y (TRAN-RECORD), VSAM file TRANSACT, RECLN 350.
 */
@Entity
@Table(name = "transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /** TRAN-ID PIC X(16). */
    @Id
    @Column(name = "tran_id", length = 16, nullable = false)
    private String transactionId;

    /** TRAN-TYPE-CD PIC X(02). */
    @Column(name = "tran_type_cd", length = 2)
    private String typeCode;

    /** TRAN-CAT-CD PIC 9(04). */
    @Column(name = "tran_cat_cd")
    private Integer categoryCode;

    /** TRAN-SOURCE PIC X(10). */
    @Column(name = "tran_source", length = 10)
    private String source;

    /** TRAN-DESC PIC X(100). */
    @Column(name = "tran_desc", length = 100)
    private String description;

    /** TRAN-AMT PIC S9(09)V99. */
    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal amount;

    /** TRAN-MERCHANT-ID PIC 9(09). */
    @Column(name = "tran_merchant_id")
    private Long merchantId;

    /** TRAN-MERCHANT-NAME PIC X(50). */
    @Column(name = "tran_merchant_name", length = 50)
    private String merchantName;

    /** TRAN-MERCHANT-CITY PIC X(50). */
    @Column(name = "tran_merchant_city", length = 50)
    private String merchantCity;

    /** TRAN-MERCHANT-ZIP PIC X(10). */
    @Column(name = "tran_merchant_zip", length = 10)
    private String merchantZip;

    /** TRAN-CARD-NUM PIC X(16). */
    @Column(name = "tran_card_num", length = 16)
    private String cardNumber;

    /** TRAN-ORIG-TS PIC X(26). */
    @Column(name = "tran_orig_ts", length = 26)
    private String originTimestamp;

    /** TRAN-PROC-TS PIC X(26). */
    @Column(name = "tran_proc_ts", length = 26)
    private String processTimestamp;
}
