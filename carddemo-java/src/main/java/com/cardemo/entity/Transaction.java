package com.cardemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA entity mapped from COBOL copybook CVTRA05Y.cpy (TRAN-RECORD).
 * Total COBOL record length: 350 bytes.
 * Seed data file: N/A (transactions are generated at runtime; dailytran.txt uses CVTRA06Y).
 * This entity represents the master transaction file (TRANSACT VSAM KSDS).
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /** TRAN-ID — PIC X(16), bytes [0:16]. Transaction ID (primary key). */
    @Id
    @Column(name = "tran_id", length = 16)
    private String tranId;

    /** TRAN-TYPE-CD — PIC X(02), bytes [16:18]. Transaction type code. */
    @Column(name = "tran_type_cd", length = 2)
    private String tranTypeCd;

    /** TRAN-CAT-CD — PIC 9(04), bytes [18:22]. Transaction category code. */
    @Column(name = "tran_cat_cd")
    private Integer tranCatCd;

    /** TRAN-SOURCE — PIC X(10), bytes [22:32]. Transaction source. */
    @Column(name = "tran_source", length = 10)
    private String tranSource;

    /** TRAN-DESC — PIC X(100), bytes [32:132]. Transaction description. */
    @Column(name = "tran_desc", length = 100)
    private String tranDesc;

    /** TRAN-AMT — PIC S9(09)V99, bytes [132:143]. Transaction amount (zoned decimal). */
    @Column(name = "tran_amt", precision = 11, scale = 2)
    private BigDecimal tranAmt;

    /** TRAN-MERCHANT-ID — PIC 9(09), bytes [143:152]. Merchant ID. */
    @Column(name = "tran_merchant_id")
    private Long tranMerchantId;

    /** TRAN-MERCHANT-NAME — PIC X(50), bytes [152:202]. Merchant name. */
    @Column(name = "tran_merchant_name", length = 50)
    private String tranMerchantName;

    /** TRAN-MERCHANT-CITY — PIC X(50), bytes [202:252]. Merchant city. */
    @Column(name = "tran_merchant_city", length = 50)
    private String tranMerchantCity;

    /** TRAN-MERCHANT-ZIP — PIC X(10), bytes [252:262]. Merchant ZIP code. */
    @Column(name = "tran_merchant_zip", length = 10)
    private String tranMerchantZip;

    /** TRAN-CARD-NUM — PIC X(16), bytes [262:278]. Card number used. */
    @Column(name = "tran_card_num", length = 16)
    private String tranCardNum;

    /** TRAN-ORIG-TS — PIC X(26), bytes [278:304]. Origination timestamp. */
    @Column(name = "tran_orig_ts", length = 26)
    private String tranOrigTs;

    /** TRAN-PROC-TS — PIC X(26), bytes [304:330]. Processing timestamp. */
    @Column(name = "tran_proc_ts", length = 26)
    private String tranProcTs;

    // FILLER — PIC X(20), bytes [330:350]. Padding — not mapped.
}
