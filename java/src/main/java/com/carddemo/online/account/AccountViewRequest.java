package com.carddemo.online.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * COBOL program: COACTVWC — account view. BMS map CACTVWA of mapset COACTVW,
 * input field ACCTSIDI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountViewRequest {

    /** ACCTSIDI OF CACTVWAI — '*' or spaces mean "no search key supplied". */
    private String accountId;
}
