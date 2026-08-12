package com.carddemo.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.Card;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.model.entity.DisclosureGroup;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Verifies the fixed-width copybook layouts against the sample data in app/data/ASCII. */
class SeedDataLoaderTest {

    private static final Path DATA = Path.of("../app/data/ASCII");

    private List<String> lines(String file) throws IOException {
        return Files.readAllLines(DATA.resolve(file), StandardCharsets.ISO_8859_1);
    }

    @Test
    void mapsAccountRecord() throws IOException {
        Account account = SeedDataLoader.toAccount(lines("acctdata.txt").get(0));
        assertThat(account.getAccountId()).isEqualTo(1L);
        assertThat(account.getActiveStatus()).isEqualTo("Y");
        assertThat(account.getCurrentBalance()).isEqualByComparingTo("194.00");
        assertThat(account.getCreditLimit()).isEqualByComparingTo("2020.00");
        assertThat(account.getOpenDate()).isEqualTo("2014-11-20");
        // The sample extract carries the disclosure group value in the ACCT-ADDR-ZIP slot and
        // leaves ACCT-GROUP-ID blank; offsets follow CVACT01Y so behaviour matches the COBOL.
        assertThat(account.getAddressZip()).isEqualTo("A000000000");
        assertThat(account.getGroupId()).isEmpty();
    }

    @Test
    void mapsCardAndXrefConsistently() throws IOException {
        Card card = SeedDataLoader.toCard(lines("carddata.txt").get(0));
        CardXref xref = SeedDataLoader.toCardXref(lines("cardxref.txt").get(0));
        assertThat(card.getCardNumber()).hasSize(16).isEqualTo(xref.getCardNumber());
        assertThat(card.getAccountId()).isEqualTo(xref.getAccountId());
        assertThat(card.getCvvCode()).isBetween(0, 999);
        assertThat(card.getEmbossedName()).isEqualTo("Aniya Von");
        assertThat(card.getActiveStatus()).isEqualTo("Y");
    }

    @Test
    void mapsDailyTransactionRecord() throws IOException {
        DailyTransaction tran = SeedDataLoader.toDailyTransaction(lines("dailytran.txt").get(0));
        assertThat(tran.getTransactionId()).isEqualTo("0000000000683580");
        assertThat(tran.getTypeCode()).isEqualTo("01");
        assertThat(tran.getCategoryCode()).isEqualTo(1);
        assertThat(tran.getSource()).isEqualTo("POS TERM");
        assertThat(tran.getAmount()).isEqualByComparingTo("504.77");
        assertThat(tran.getCardNumber()).hasSize(16);
        assertThat(tran.getOriginTimestamp()).isEqualTo("2022-06-10 19:27:53.000000");
    }

    @Test
    void mapsDisclosureGroupInterestRate() throws IOException {
        DisclosureGroup group = SeedDataLoader.toDisclosureGroup(lines("discgrp.txt").get(0));
        assertThat(group.getId().getTransactionTypeCode()).isEqualTo("01");
        assertThat(group.getId().getTransactionCategoryCode()).isEqualTo(1);
        assertThat(group.getInterestRate()).isEqualByComparingTo("15.00");
    }
}
