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
 * JPA entity mapped from COBOL copybook CVTRA06Y.cpy (DALYTRAN-RECORD).
 * Total COBOL record length: 350 bytes.
 * Seed data file: dailytran.txt (300 records).
 * Represents daily transaction staging records before posting to the master transaction file.
 */
@Entity
@Table(name = "daily_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTransaction {

    /** DALYTRAN-ID — PIC X(16), bytes [0:16]. Daily transaction ID (primary key). */
    @Id
    @Column(name = "dalytran_id", length = 16)
    private String dalytranId;

    /** DALYTRAN-TYPE-CD — PIC X(02), bytes [16:18]. Transaction type code. */
    @Column(name = "dalytran_type_cd", length = 2)
    private String dalytranTypeCd;

    /** DALYTRAN-CAT-CD — PIC 9(04), bytes [18:22]. Transaction category code. */
    @Column(name = "dalytran_cat_cd")
    private Integer dalytranCatCd;

    /** DALYTRAN-SOURCE — PIC X(10), bytes [22:32]. Transaction source. */
    @Column(name = "dalytran_source", length = 10)
    private String dalytranSource;

    /** DALYTRAN-DESC — PIC X(100), bytes [32:132]. Transaction description. */
    @Column(name = "dalytran_desc", length = 100)
    private String dalytranDesc;

    /** DALYTRAN-AMT — PIC S9(09)V99, bytes [132:143]. Transaction amount (zoned decimal). */
    @Column(name = "dalytran_amt", precision = 11, scale = 2)
    private BigDecimal dalytranAmt;

    /** DALYTRAN-MERCHANT-ID — PIC 9(09), bytes [143:152]. Merchant ID. */
    @Column(name = "dalytran_merchant_id")
    private Long dalytranMerchantId;

    /** DALYTRAN-MERCHANT-NAME — PIC X(50), bytes [152:202]. Merchant name. */
    @Column(name = "dalytran_merchant_name", length = 50)
    private String dalytranMerchantName;

    /** DALYTRAN-MERCHANT-CITY — PIC X(50), bytes [202:252]. Merchant city. */
    @Column(name = "dalytran_merchant_city", length = 50)
    private String dalytranMerchantCity;

    /** DALYTRAN-MERCHANT-ZIP — PIC X(10), bytes [252:262]. Merchant ZIP code. */
    @Column(name = "dalytran_merchant_zip", length = 10)
    private String dalytranMerchantZip;

    /** DALYTRAN-CARD-NUM — PIC X(16), bytes [262:278]. Card number used. */
    @Column(name = "dalytran_card_num", length = 16)
    private String dalytranCardNum;

    /** DALYTRAN-ORIG-TS — PIC X(26), bytes [278:304]. Origination timestamp. */
    @Column(name = "dalytran_orig_ts", length = 26)
    private String dalytranOrigTs;

    /** DALYTRAN-PROC-TS — PIC X(26), bytes [304:330]. Processing timestamp. */
    @Column(name = "dalytran_proc_ts", length = 26)
    private String dalytranProcTs;

    // FILLER — PIC X(20), bytes [330:350]. Padding — not mapped.
}
