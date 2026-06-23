package com.carddemo.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapping the COBOL {@code TRAN-RECORD} (copybook
 * {@code app/cpy/CVTRA05Y.cpy}, VSAM TRANSACT KSDS, record length 350).
 *
 * <p>Field mappings preserve the original COBOL field names and PIC clauses for
 * traceability. The 20-byte trailing {@code FILLER} is intentionally not mapped.
 * {@code TRAN-AMT} uses {@link BigDecimal} (never floating point) to preserve the
 * COBOL fixed-point {@code S9(09)V99} semantics — note this is an 11-byte zoned
 * field (9 integer + 2 fraction), narrower than the 12-byte Account money fields.
 * The two timestamp fields use {@link LocalDateTime}; their stored form is
 * {@code yyyy-MM-dd HH:mm:ss.SSSSSS} (see {@link TransactionSeedLoader#TS_FORMAT}).</p>
 */
@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /** COBOL: {@code TRAN-ID PIC X(16)} — VSAM primary key (offset 0, len 16). */
    @Id
    @Column(name = "tran_id", length = 16)
    private String tranId;

    /** COBOL: {@code TRAN-TYPE-CD PIC X(02)} — transaction type code (offset 16, len 2). */
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    /** COBOL: {@code TRAN-CAT-CD PIC 9(04)} — transaction category code (offset 18, len 4). */
    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    /** COBOL: {@code TRAN-SOURCE PIC X(10)} — origin channel (offset 22, len 10). */
    @Column(name = "tran_source", length = 10)
    private String tranSource;

    /** COBOL: {@code TRAN-DESC PIC X(100)} — free-text description (offset 32, len 100). */
    @Column(name = "tran_desc", length = 100)
    private String tranDesc;

    /** COBOL: {@code TRAN-AMT PIC S9(09)V99} — signed amount, 11-byte zoned decimal (offset 132, len 11). */
    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal tranAmt;

    /** COBOL: {@code TRAN-MERCHANT-ID PIC 9(09)} — merchant id (offset 143, len 9). */
    @Column(name = "tran_merchant_id")
    private Long tranMerchantId;

    /** COBOL: {@code TRAN-MERCHANT-NAME PIC X(50)} — merchant name (offset 152, len 50). */
    @Column(name = "tran_merchant_name", length = 50)
    private String tranMerchantName;

    /** COBOL: {@code TRAN-MERCHANT-CITY PIC X(50)} — merchant city (offset 202, len 50). */
    @Column(name = "tran_merchant_city", length = 50)
    private String tranMerchantCity;

    /** COBOL: {@code TRAN-MERCHANT-ZIP PIC X(10)} — merchant ZIP (offset 252, len 10). */
    @Column(name = "tran_merchant_zip", length = 10)
    private String tranMerchantZip;

    /** COBOL: {@code TRAN-CARD-NUM PIC X(16)} — card number, XREF lookup key (offset 262, len 16). */
    @Column(name = "tran_card_num", length = 16)
    private String tranCardNum;

    /** COBOL: {@code TRAN-ORIG-TS PIC X(26)} — origination timestamp (offset 278, len 26). */
    @Column(name = "tran_orig_ts")
    private LocalDateTime tranOrigTs;

    /** COBOL: {@code TRAN-PROC-TS PIC X(26)} — processing/posting timestamp (offset 304, len 26). */
    @Column(name = "tran_proc_ts")
    private LocalDateTime tranProcTs;
}
