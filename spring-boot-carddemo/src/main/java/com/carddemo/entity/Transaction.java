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
 * JPA entity mapped from COBOL copybook CVTRA05Y.cpy (TRAN-RECORD, RECLN 350).
 * Represents a transaction in the CardDemo TRANSACT VSAM file.
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /** TRAN-ID — PIC X(16). Primary key, unique transaction identifier. */
    @Id
    @Column(name = "tran_id", length = 16)
    private String tranId;

    /** TRAN-TYPE-CD — PIC X(02). Transaction type code (e.g., SA=Sale, RE=Return). */
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    /** TRAN-CAT-CD — PIC 9(04). Transaction category code. */
    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    /** TRAN-SOURCE — PIC X(10). Transaction origination source. */
    @Column(name = "tran_source", length = 10)
    private String tranSource;

    /** TRAN-DESC — PIC X(100). Transaction description. */
    @Column(name = "tran_desc", length = 100)
    private String tranDesc;

    /** TRAN-AMT — PIC S9(09)V99. Transaction amount (zoned decimal, 11 bytes). */
    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal tranAmt;

    /** TRAN-MERCHANT-ID — PIC 9(09). Merchant identifier. */
    @Column(name = "tran_merchant_id")
    private Long tranMerchantId;

    /** TRAN-MERCHANT-NAME — PIC X(50). Merchant name. */
    @Column(name = "tran_merchant_name", length = 50)
    private String tranMerchantName;

    /** TRAN-MERCHANT-CITY — PIC X(50). Merchant city. */
    @Column(name = "tran_merchant_city", length = 50)
    private String tranMerchantCity;

    /** TRAN-MERCHANT-ZIP — PIC X(10). Merchant ZIP code. */
    @Column(name = "tran_merchant_zip", length = 10)
    private String tranMerchantZip;

    /** TRAN-CARD-NUM — PIC X(16). Card number associated with this transaction. */
    @Column(name = "tran_card_num", length = 16)
    private String tranCardNum;

    /** TRAN-ORIG-TS — PIC X(26). Original transaction timestamp. */
    @Column(name = "tran_orig_ts", length = 26)
    private String tranOrigTs;

    /** TRAN-PROC-TS — PIC X(26). Processing timestamp. */
    @Column(name = "tran_proc_ts", length = 26)
    private String tranProcTs;
}
