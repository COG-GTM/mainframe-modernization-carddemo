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
 * COBOL copybook: CVTRA06Y (DALYTRAN-RECORD), file DALYTRAN, RECLN 350.
 * Unposted transactions consumed by the daily posting batch (CBTRN02C).
 */
@Entity
@Table(name = "daily_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTransaction {

    /** DALYTRAN-ID PIC X(16). */
    @Id
    @Column(name = "dalytran_id", length = 16, nullable = false)
    private String transactionId;

    /** DALYTRAN-TYPE-CD PIC X(02). */
    @Column(name = "dalytran_type_cd", length = 2)
    private String typeCode;

    /** DALYTRAN-CAT-CD PIC 9(04). */
    @Column(name = "dalytran_cat_cd")
    private Integer categoryCode;

    /** DALYTRAN-SOURCE PIC X(10). */
    @Column(name = "dalytran_source", length = 10)
    private String source;

    /** DALYTRAN-DESC PIC X(100). */
    @Column(name = "dalytran_desc", length = 100)
    private String description;

    /** DALYTRAN-AMT PIC S9(09)V99. */
    @Column(name = "dalytran_amt", precision = 11, scale = 2)
    private BigDecimal amount;

    /** DALYTRAN-MERCHANT-ID PIC 9(09). */
    @Column(name = "dalytran_merchant_id")
    private Long merchantId;

    /** DALYTRAN-MERCHANT-NAME PIC X(50). */
    @Column(name = "dalytran_merchant_name", length = 50)
    private String merchantName;

    /** DALYTRAN-MERCHANT-CITY PIC X(50). */
    @Column(name = "dalytran_merchant_city", length = 50)
    private String merchantCity;

    /** DALYTRAN-MERCHANT-ZIP PIC X(10). */
    @Column(name = "dalytran_merchant_zip", length = 10)
    private String merchantZip;

    /** DALYTRAN-CARD-NUM PIC X(16). */
    @Column(name = "dalytran_card_num", length = 16)
    private String cardNumber;

    /** DALYTRAN-ORIG-TS PIC X(26). */
    @Column(name = "dalytran_orig_ts", length = 26)
    private String originTimestamp;

    /** DALYTRAN-PROC-TS PIC X(26). */
    @Column(name = "dalytran_proc_ts", length = 26)
    private String processTimestamp;
}
