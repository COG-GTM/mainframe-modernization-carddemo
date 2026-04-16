package com.cardemo.batch.service;

import com.cardemo.batch.model.CardXrefRecord;
import com.cardemo.batch.model.TranCategoryRecord;
import com.cardemo.batch.model.TranTypeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Provides reference data lookups equivalent to the COBOL paragraphs:
 *   1500-A-LOOKUP-XREF     — card number to account ID
 *   1500-B-LOOKUP-TRANTYPE — transaction type code to description
 *   1500-C-LOOKUP-TRANCATG — transaction type+category code to description
 */
@Service
public class ReferenceDataService {

    private static final Logger log = LoggerFactory.getLogger(ReferenceDataService.class);

    private final JdbcTemplate jdbcTemplate;

    public ReferenceDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 1500-A-LOOKUP-XREF: Read XREF-FILE by card number to get XREF-ACCT-ID.
     */
    public CardXrefRecord lookupCardXref(String cardNum) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT xref_card_num, xref_cust_id, xref_acct_id FROM card_xref WHERE xref_card_num = ?",
                    (rs, rowNum) -> new CardXrefRecord(
                            rs.getString("xref_card_num"),
                            rs.getLong("xref_cust_id"),
                            rs.getString("xref_acct_id")
                    ),
                    cardNum
            );
        } catch (Exception e) {
            log.error("INVALID CARD NUMBER: {}", cardNum, e);
            throw new IllegalStateException("Card cross-reference not found for card number: " + cardNum, e);
        }
    }

    /**
     * 1500-B-LOOKUP-TRANTYPE: Read TRANTYPE-FILE by FD-TRAN-TYPE to get TRAN-TYPE-DESC.
     */
    public TranTypeRecord lookupTranType(String tranType) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT tran_type, tran_type_desc FROM tran_type WHERE tran_type = ?",
                    (rs, rowNum) -> new TranTypeRecord(
                            rs.getString("tran_type"),
                            rs.getString("tran_type_desc")
                    ),
                    tranType
            );
        } catch (Exception e) {
            log.error("INVALID TRANSACTION TYPE: {}", tranType, e);
            throw new IllegalStateException("Transaction type not found: " + tranType, e);
        }
    }

    /**
     * 1500-C-LOOKUP-TRANCATG: Read TRANCATG-FILE by (TRAN-TYPE-CD, TRAN-CAT-CD)
     * to get TRAN-CAT-TYPE-DESC.
     */
    public TranCategoryRecord lookupTranCategory(String tranTypeCd, int tranCatCd) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT tran_type_cd, tran_cat_cd, tran_cat_type_desc FROM tran_category "
                            + "WHERE tran_type_cd = ? AND tran_cat_cd = ?",
                    (rs, rowNum) -> new TranCategoryRecord(
                            rs.getString("tran_type_cd"),
                            rs.getInt("tran_cat_cd"),
                            rs.getString("tran_cat_type_desc")
                    ),
                    tranTypeCd, tranCatCd
            );
        } catch (Exception e) {
            log.error("INVALID TRAN CATG KEY: {} / {}", tranTypeCd, tranCatCd, e);
            throw new IllegalStateException(
                    "Transaction category not found for type: " + tranTypeCd + ", category: " + tranCatCd, e);
        }
    }
}
