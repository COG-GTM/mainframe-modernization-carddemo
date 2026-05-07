package com.carddemo.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a daily transaction record.
 * <p>
 * Migrated from COBOL copybook CVTRA05Y.cpy (TRAN-RECORD, RECLN 350).
 * The COBOL layout is also duplicated in CVTRA06Y.cpy (DALYTRAN-RECORD).
 * <p>
 * Field mapping (COBOL &rarr; Java):
 * <ul>
 *   <li>TRAN-ID            PIC X(16)      &rarr; {@link #id} String(16)</li>
 *   <li>TRAN-TYPE-CD       PIC X(02)      &rarr; {@link #typeCode} String(2)</li>
 *   <li>TRAN-CAT-CD        PIC 9(04)      &rarr; {@link #categoryCode} Integer</li>
 *   <li>TRAN-SOURCE        PIC X(10)      &rarr; {@link #source} String(10)</li>
 *   <li>TRAN-DESC          PIC X(100)     &rarr; {@link #description} String(100)</li>
 *   <li>TRAN-AMT           PIC S9(09)V99  &rarr; {@link #amount} BigDecimal(11,2)</li>
 *   <li>TRAN-MERCHANT-ID   PIC 9(09)      &rarr; {@link #merchantId} Long</li>
 *   <li>TRAN-MERCHANT-NAME PIC X(50)      &rarr; {@link #merchantName} String(50)</li>
 *   <li>TRAN-MERCHANT-CITY PIC X(50)      &rarr; {@link #merchantCity} String(50)</li>
 *   <li>TRAN-MERCHANT-ZIP  PIC X(10)      &rarr; {@link #merchantZip} String(10)</li>
 *   <li>TRAN-CARD-NUM      PIC X(16)      &rarr; {@link #cardNum} String(16)</li>
 *   <li>TRAN-ORIG-TS       PIC X(26)      &rarr; {@link #origTimestamp} String(26)</li>
 *   <li>TRAN-PROC-TS       PIC X(26)      &rarr; {@link #procTimestamp} String(26)</li>
 *   <li>FILLER             PIC X(20)      &rarr; (not mapped)</li>
 * </ul>
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    /**
     * TRAN-ID PIC X(16) — unique transaction identifier.
     */
    @Id
    @Column(name = "tran_id", length = 16, nullable = false)
    private String id;

    /**
     * TRAN-TYPE-CD PIC X(02) — transaction type code (e.g. "01" purchase, "03" return).
     */
    @Column(name = "tran_type_cd", length = 2)
    private String typeCode;

    /**
     * TRAN-CAT-CD PIC 9(04) — transaction category code.
     */
    @Column(name = "tran_cat_cd")
    private Integer categoryCode;

    /**
     * TRAN-SOURCE PIC X(10) — originating source (e.g. "POS TERM").
     */
    @Column(name = "tran_source", length = 10)
    private String source;

    /**
     * TRAN-DESC PIC X(100) — transaction description.
     */
    @Column(name = "tran_desc", length = 100)
    private String description;

    /**
     * TRAN-AMT PIC S9(09)V99 — signed transaction amount with 2 implied decimal places.
     */
    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal amount;

    /**
     * TRAN-MERCHANT-ID PIC 9(09) — merchant identifier.
     */
    @Column(name = "tran_merchant_id")
    private Long merchantId;

    /**
     * TRAN-MERCHANT-NAME PIC X(50) — merchant name.
     */
    @Column(name = "tran_merchant_name", length = 50)
    private String merchantName;

    /**
     * TRAN-MERCHANT-CITY PIC X(50) — merchant city.
     */
    @Column(name = "tran_merchant_city", length = 50)
    private String merchantCity;

    /**
     * TRAN-MERCHANT-ZIP PIC X(10) — merchant ZIP code.
     */
    @Column(name = "tran_merchant_zip", length = 10)
    private String merchantZip;

    /**
     * TRAN-CARD-NUM PIC X(16) — card number associated with the transaction.
     */
    @Column(name = "tran_card_num", length = 16)
    private String cardNum;

    /**
     * TRAN-ORIG-TS PIC X(26) — original transaction timestamp.
     * Stored as String to preserve the COBOL timestamp format (e.g. "2022-06-10 19:27:53.000000").
     */
    @Column(name = "tran_orig_ts", length = 26)
    private String origTimestamp;

    /**
     * TRAN-PROC-TS PIC X(26) — processing timestamp.
     * Stored as String to preserve the COBOL timestamp format.
     */
    @Column(name = "tran_proc_ts", length = 26)
    private String procTimestamp;
}
