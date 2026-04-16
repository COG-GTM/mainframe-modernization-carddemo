package com.carddemo.transaction.dto;

import com.carddemo.transaction.entity.Transaction;
import java.math.BigDecimal;

/**
 * Response DTO for transaction data.
 * Maps all fields from the TRAN-RECORD (CVTRA05Y.cpy).
 */
public record TransactionResponse(
        String tranId,
        String tranTypeCd,
        Integer tranCatCd,
        String tranSource,
        String tranDesc,
        BigDecimal tranAmt,
        Long tranMerchantId,
        String tranMerchantName,
        String tranMerchantCity,
        String tranMerchantZip,
        String tranCardNum,
        String tranOrigTs,
        String tranProcTs
) {
    public static TransactionResponse fromEntity(Transaction t) {
        return new TransactionResponse(
                t.getTranId(),
                t.getTranTypeCd(),
                t.getTranCatCd(),
                t.getTranSource(),
                t.getTranDesc(),
                t.getTranAmt(),
                t.getTranMerchantId(),
                t.getTranMerchantName(),
                t.getTranMerchantCity(),
                t.getTranMerchantZip(),
                t.getTranCardNum(),
                t.getTranOrigTs(),
                t.getTranProcTs()
        );
    }
}
