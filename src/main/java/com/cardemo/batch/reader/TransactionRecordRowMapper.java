package com.cardemo.batch.reader;

import com.cardemo.batch.model.TransactionRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps database rows to TransactionRecord objects.
 * Corresponds to reading records from the TRANFILE (CVTRA05Y.cpy TRAN-RECORD).
 */
public class TransactionRecordRowMapper implements RowMapper<TransactionRecord> {

    @Override
    public TransactionRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        TransactionRecord record = new TransactionRecord();
        record.setTranId(rs.getString("tran_id"));
        record.setTranTypeCd(rs.getString("tran_type_cd"));
        record.setTranCatCd(rs.getInt("tran_cat_cd"));
        record.setTranSource(rs.getString("tran_source"));
        record.setTranDesc(rs.getString("tran_desc"));
        record.setTranAmt(rs.getBigDecimal("tran_amt"));
        record.setTranMerchantId(rs.getLong("tran_merchant_id"));
        record.setTranMerchantName(rs.getString("tran_merchant_name"));
        record.setTranMerchantCity(rs.getString("tran_merchant_city"));
        record.setTranMerchantZip(rs.getString("tran_merchant_zip"));
        record.setTranCardNum(rs.getString("tran_card_num"));
        record.setTranOrigTs(rs.getString("tran_orig_ts"));
        record.setTranProcTs(rs.getString("tran_proc_ts"));
        return record;
    }
}
