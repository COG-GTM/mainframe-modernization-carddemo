package com.carddemo.online.menu;

import com.carddemo.online.common.ScreenHeader;
import java.util.List;

/**
 * BMS map COMEN1A (mapset COMEN01) and COADM1A (mapset COADM01) output, produced by
 * SEND-MENU-SCREEN of COBOL programs COMEN01C and COADM01C.
 *
 * @param header POPULATE-HEADER-INFO fields
 * @param options OPTN001O..OPTN012O built by BUILD-MENU-OPTIONS
 * @param option OPTIONO PIC 9(02), the normalized option number that was processed
 * @param errorMessage ERRMSGO, the verbatim COBOL message
 * @param nextProgram program of the CICS XCTL, {@code null} when the menu is redisplayed
 * @param nextTransactionId transaction id of {@code nextProgram}
 */
public record MenuResponse(
        ScreenHeader header,
        List<String> options,
        Integer option,
        String errorMessage,
        String nextProgram,
        String nextTransactionId) {
}
