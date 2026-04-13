package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA entity mapped from COBOL copybook CVTRA06Y.cpy (DALYTRAN-RECORD, RECLN 350).
 * Represents a daily transaction record pending posting.
 */
@Entity
@Table(name = "daily_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTransaction {

    /** DALYTRAN-ID — PIC X(16). Primary key, unique daily transaction identifier. */
    @Id
    @Column(name = "dalytran_id", length = 16)
    private String dalytranId;

    /** DALYTRAN-TYPE-CD — PIC X(02). Transaction type code. */
    @Column(name = "dalytran_type_cd", length = 2)
    private String dalytranTypeCd;

    /** DALYTRAN-CAT-CD — PIC 9(04). Transaction category code. */
    @Column(name = "dalytran_cat_cd")
    private Integer dalytranCatCd;

    /** DALYTRAN-SOURCE — PIC X(10). Transaction origination source. */
    @Column(name = "dalytran_source", length = 10)
    private String dalytranSource;

    /** DALYTRAN-DESC — PIC X(100). Transaction description. */
    @Column(name = "dalytran_desc", length = 100)
    private String dalytranDesc;

    /** DALYTRAN-AMT — PIC S9(09)V99. Transaction amount (zoned decimal, 11 bytes). */
    @Column(name = "dalytran_amt", precision = 11, scale = 2)
    private BigDecimal dalytranAmt;

    /** DALYTRAN-MERCHANT-ID — PIC 9(09). Merchant identifier. */
    @Column(name = "dalytran_merchant_id")
    private Long dalytranMerchantId;

    /** DALYTRAN-MERCHANT-NAME — PIC X(50). Merchant name. */
    @Column(name = "dalytran_merchant_name", length = 50)
    private String dalytranMerchantName;

    /** DALYTRAN-MERCHANT-CITY — PIC X(50). Merchant city. */
    @Column(name = "dalytran_merchant_city", length = 50)
    private String dalytranMerchantCity;

    /** DALYTRAN-MERCHANT-ZIP — PIC X(10). Merchant ZIP code. */
    @Column(name = "dalytran_merchant_zip", length = 10)
    private String dalytranMerchantZip;

    /** DALYTRAN-CARD-NUM — PIC X(16). Card number for this transaction. */
    @Column(name = "dalytran_card_num", length = 16)
    private String dalytranCardNum;

    /** DALYTRAN-ORIG-TS — PIC X(26). Original transaction timestamp. */
    @Column(name = "dalytran_orig_ts", length = 26)
    private String dalytranOrigTs;

    /** DALYTRAN-PROC-TS — PIC X(26). Processing timestamp. */
    @Column(name = "dalytran_proc_ts", length = 26)
    private String dalytranProcTs;
}
