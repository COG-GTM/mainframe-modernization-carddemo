package com.carddemo.interestcalc.batch;

import com.carddemo.interestcalc.domain.AccountRecord;
import com.carddemo.interestcalc.domain.CardXrefRecord;
import com.carddemo.interestcalc.domain.DisclosureGroupRecord;
import com.carddemo.interestcalc.domain.TranCatBalRecord;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the copybook byte offsets against real records from {@code app/data/ASCII/}.
 */
class AsciiFileLoaderTest {

    @Test
    void parsesTcatbalRecord() {
        // First record of app/data/ASCII/tcatbal.txt (CVTRA01Y, RECLN 50)
        String line = "00000000001" + "01" + "0001" + "0000000000{" + " ".repeat(22);
        TranCatBalRecord rec = AsciiFileLoader.parseTranCatBal(line);
        assertThat(rec.accountId()).isEqualTo(1L);
        assertThat(rec.typeCode()).isEqualTo("01");
        assertThat(rec.categoryCode()).isEqualTo(1);
        assertThat(rec.balance()).isEqualByComparingTo("0.00");
    }

    @Test
    void parsesDiscgrpRecord() {
        // First record of app/data/ASCII/discgrp.txt (CVTRA02Y, RECLN 50)
        String line = "A000000000" + "01" + "0001" + "00150{" + " ".repeat(28);
        DisclosureGroupRecord rec = AsciiFileLoader.parseDisclosureGroup(line);
        assertThat(rec.accountGroupId()).isEqualTo("A000000000");
        assertThat(rec.transactionTypeCode()).isEqualTo("01");
        assertThat(rec.transactionCategoryCode()).isEqualTo(1);
        assertThat(rec.interestRate()).isEqualByComparingTo("15.00");
    }

    @Test
    void parsesCardXrefRecord() {
        // First record of app/data/ASCII/cardxref.txt (CVACT03Y without trailing FILLER)
        String line = "0500024453765740" + "000000005" + "00000000005";
        CardXrefRecord rec = AsciiFileLoader.parseCardXref(line);
        assertThat(rec.cardNumber()).isEqualTo("0500024453765740");
        assertThat(rec.customerId()).isEqualTo(5L);
        assertThat(rec.accountId()).isEqualTo(5L);
    }

    @Test
    void parsesAccountRecord() {
        // First record of app/data/ASCII/acctdata.txt (CVACT01Y, RECLN 300)
        String line = "00000000001" + "Y"
                + "00000001940{" + "00000020200{" + "00000010200{"
                + "2014-11-20" + "2025-05-20" + "2025-05-20"
                + "00000000000{" + "00000000000{"
                + "A000000000" + " ".repeat(10) + " ".repeat(178);
        AccountRecord rec = AsciiFileLoader.parseAccount(line);
        assertThat(rec.getAccountId()).isEqualTo(1L);
        assertThat(rec.getActiveStatus()).isEqualTo("Y");
        assertThat(rec.getCurrentBalance()).isEqualByComparingTo("194.00");
        assertThat(rec.getCreditLimit()).isEqualByComparingTo("2020.00");
        assertThat(rec.getCashCreditLimit()).isEqualByComparingTo("1020.00");
        assertThat(rec.getOpenDate()).isEqualTo(LocalDate.parse("2014-11-20"));
        assertThat(rec.getExpirationDate()).isEqualTo(LocalDate.parse("2025-05-20"));
        assertThat(rec.getReissueDate()).isEqualTo(LocalDate.parse("2025-05-20"));
        assertThat(rec.getCurrentCycleCredit()).isEqualByComparingTo("0.00");
        assertThat(rec.getCurrentCycleDebit()).isEqualByComparingTo("0.00");
        assertThat(rec.getAddressZip()).isEqualTo("A000000000");
        assertThat(rec.getGroupId()).isEmpty();
    }
}
