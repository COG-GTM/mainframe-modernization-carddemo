package com.carddemo.etl.reader;

import com.carddemo.etl.model.Account;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountRecordParserTest {

    /** First record of app/data/ASCII/acctdata.txt, padded to the full 300-byte length. */
    private static final String RECORD_ONE =
            "00000000001"      // ACCT-ID
            + "Y"              // ACCT-ACTIVE-STATUS
            + "00000001940{"   // ACCT-CURR-BAL          -> 194.00
            + "00000020200{"   // ACCT-CREDIT-LIMIT      -> 2020.00
            + "00000010200{"   // ACCT-CASH-CREDIT-LIMIT -> 1020.00
            + "2014-11-20"     // ACCT-OPEN-DATE
            + "2025-05-20"     // ACCT-EXPIRAION-DATE
            + "2025-05-20"     // ACCT-REISSUE-DATE
            + "00000000000{"   // ACCT-CURR-CYC-CREDIT   -> 0.00
            + "00000000000{"   // ACCT-CURR-CYC-DEBIT    -> 0.00
            + "A000000000"     // ACCT-ADDR-ZIP
            + "          "     // ACCT-GROUP-ID (blank)
            + " ".repeat(178); // FILLER

    @Test
    void parsesAllFieldsFromKnownRecord() {
        Account account = AccountRecordParser.parse(RECORD_ONE);

        assertThat(account.acctId()).isEqualTo(1L);
        assertThat(account.activeStatus()).isEqualTo("Y");
        assertThat(account.currBal()).isEqualByComparingTo("194.00");
        assertThat(account.creditLimit()).isEqualByComparingTo("2020.00");
        assertThat(account.cashCreditLimit()).isEqualByComparingTo("1020.00");
        assertThat(account.openDate()).isEqualTo(LocalDate.of(2014, 11, 20));
        assertThat(account.expirationDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(account.reissueDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(account.currCycCredit()).isEqualByComparingTo("0.00");
        assertThat(account.currCycDebit()).isEqualByComparingTo("0.00");
        assertThat(account.addrZip()).isEqualTo("A000000000");
        assertThat(account.groupId()).isNull();
    }

    @Test
    void recordIsExactlyThreeHundredChars() {
        assertThat(RECORD_ONE).hasSize(AccountRecordParser.RECORD_LENGTH);
    }

    @Test
    void decodesNegativeBalanceOverpunch() {
        String record = RECORD_ONE.substring(0, 12) + "00000001940}" + RECORD_ONE.substring(24);
        Account account = AccountRecordParser.parse(record);
        assertThat(account.currBal()).isEqualByComparingTo("-194.00");
    }

    @Test
    void treatsAllZeroDateAsNull() {
        String record = RECORD_ONE.substring(0, 48) + "0000000000" + RECORD_ONE.substring(58);
        Account account = AccountRecordParser.parse(record);
        assertThat(account.openDate()).isNull();
    }

    @Test
    void rejectsTooShortRecord() {
        assertThatThrownBy(() -> AccountRecordParser.parse("00000000001Y"))
                .isInstanceOf(RecordParseException.class);
    }

    @Test
    void rejectsNonNumericAccountId() {
        String record = "0000000000X" + RECORD_ONE.substring(11);
        assertThatThrownBy(() -> AccountRecordParser.parse(record))
                .isInstanceOf(RecordParseException.class);
    }
}
