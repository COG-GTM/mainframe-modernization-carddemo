package com.carddemo.web.transaction.dto;

import java.util.List;

/**
 * A page of the {@code COTRN00} (Transaction List) browse. Reproduces the VSAM
 * STARTBR/READNEXT window: up to {@code pageSize} rows (10 on the 3270 map) ordered by
 * {@code TRAN-ID}, together with the first/last keys used by PF7/PF8 paging and a
 * {@code moreRecords} flag ({@code CDEMO-CT00-NEXT-PAGE-FLG}).
 *
 * @param transactions the rows on this page (≤ {@code pageSize})
 * @param startTranId  the browse start key echoed back (TRNIDINI filter; empty = from top)
 * @param pageSize     rows per page (10, the map's TRNID01..TRNID10)
 * @param firstTranId  first TRAN-ID on the page — CDEMO-CT00-TRNID-FIRST (PF7 anchor)
 * @param lastTranId   last TRAN-ID on the page — CDEMO-CT00-TRNID-LAST (PF8 anchor)
 * @param moreRecords  whether a further record exists after this page (PF8 enabled)
 * @param count        number of rows on this page
 */
public record TransactionListResponse(
        List<TransactionSummaryDto> transactions,
        String startTranId,
        int pageSize,
        String firstTranId,
        String lastTranId,
        boolean moreRecords,
        int count) {
}
