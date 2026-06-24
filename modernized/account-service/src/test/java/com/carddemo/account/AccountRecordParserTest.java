package com.carddemo.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carddemo.account.model.Account;
import com.carddemo.account.parser.AccountRecordParser;
import org.junit.jupiter.api.Test;

class AccountRecordParserTest {

    private final AccountRecordParser parser = new AccountRecordParser();

    /** First record of app/data/ASCII/acctdata.txt, padded to the 300-byte record length. */
    private static String firstSeedRecord() {
        String fixed = "00000000001"      // ACCT-ID
                + "Y"                       // ACCT-ACTIVE-STATUS
                + "00000001940{"            // ACCT-CURR-BAL          -> 194.00
                + "00000020200{"            // ACCT-CREDIT-LIMIT      -> 2020.00
                + "00000010200{"            // ACCT-CASH-CREDIT-LIMIT -> 1020.00
                + "2014-11-20"              // ACCT-OPEN-DATE
                + "2025-05-20"              // ACCT-EXPIRAION-DATE
                + "2025-05-20"              // ACCT-REISSUE-DATE
                + "00000000000{"            // ACCT-CURR-CYC-CREDIT   -> 0.00
                + "00000000000{"            // ACCT-CURR-CYC-DEBIT    -> 0.00
                + "A000000000"              // ACCT-ADDR-ZIP
                + "          ";             // ACCT-GROUP-ID (blank)
        return String.format("%-300s", fixed);
    }

    @Test
    void parsesAllFieldsFromSeedRecord() {
        Account account = parser.parse(firstSeedRecord());

        assertThat(account.getAcctId()).isEqualTo("00000000001");
        assertThat(account.getActiveStatus()).isEqualTo("Y");
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("194.00");
        assertThat(account.getCreditLimit()).isEqualByComparingTo("2020.00");
        assertThat(account.getCashCreditLimit()).isEqualByComparingTo("1020.00");
        assertThat(account.getOpenDate()).isEqualTo("2014-11-20");
        assertThat(account.getExpirationDate()).isEqualTo("2025-05-20");
        assertThat(account.getReissueDate()).isEqualTo("2025-05-20");
        assertThat(account.getCurrentCycleCredit()).isEqualByComparingTo("0.00");
        assertThat(account.getCurrentCycleDebit()).isEqualByComparingTo("0.00");
        assertThat(account.getAddressZip()).isEqualTo("A000000000");
        assertThat(account.getGroupId()).isEmpty();
    }

    @Test
    void rejectsOverlongRecord() {
        assertThatThrownBy(() -> parser.parse("x".repeat(301)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
