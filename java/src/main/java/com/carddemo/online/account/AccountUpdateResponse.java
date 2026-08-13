package com.carddemo.online.account;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COACTUPC — the SEND MAP of map CACTUPA (mapset COACTUP):
 * INFOMSGO (WS-INFO-MSG), ERRMSGO (WS-RETURN-MSG), the screen fields
 * (ACUP-NEW-DETAILS) and the resulting ACUP-CHANGE-ACTION.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateResponse {

    /** ACUP-CHANGE-ACTION, see the constants on {@link AccountUpdateState}. */
    private String changeAction;

    /** WS-INFO-MSG as chosen by 3250-SETUP-INFOMSG. */
    private String infoMessage;

    /** WS-RETURN-MSG — the first error raised during this turn. */
    private String errorMessage;

    /** Field level edit flags: map field name to the message raised for it. */
    private Map<String, String> fieldErrors;

    /** ACUP-NEW-DETAILS as redisplayed on the map. */
    private AccountUpdateData data;

    /** CDEMO-TO-PROGRAM when PF03 transfers control back to the caller. */
    private String nextProgram;
}
