package com.carddemo.online.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COACTUPC — the RECEIVE MAP of map CACTUPA (mapset COACTUP) together
 * with the AID key (CCARD-AID of copybook CVCRD01Y).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateRequest {

    /** CCARD-AID: ENTER, PF3, PF5 (confirm save) or PF12 (cancel changes). */
    private String action;

    /** ACUP-NEW-DETAILS as keyed on the map. */
    private AccountUpdateData data;

    public static final String ACTION_ENTER = "ENTER";
    public static final String ACTION_PF3 = "PF3";
    public static final String ACTION_PF5 = "PF5";
    public static final String ACTION_PF12 = "PF12";
}
