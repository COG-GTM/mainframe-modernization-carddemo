package com.carddemo.online.user;

import com.carddemo.online.common.ScreenHeader;
import java.util.List;

/**
 * BMS map COUSR0A (mapset COUSR00) output, produced by SEND-USRLST-SCREEN of COBOL program
 * COUSR00C.
 *
 * @param header POPULATE-HEADER-INFO fields
 * @param users the lines filled by POPULATE-USER-DATA (at most ten)
 * @param pageNumber PAGENUMI, CDEMO-CU00-PAGE-NUM
 * @param nextPageAvailable CDEMO-CU00-NEXT-PAGE-FLG
 * @param errorMessage ERRMSGO, the verbatim COBOL message
 * @param nextProgram program of the CICS XCTL, {@code null} when the list is redisplayed
 * @param nextTransactionId transaction id of {@code nextProgram}
 */
public record UserListResponse(
        ScreenHeader header,
        List<UserRow> users,
        int pageNumber,
        boolean nextPageAvailable,
        String errorMessage,
        String nextProgram,
        String nextTransactionId) {
}
