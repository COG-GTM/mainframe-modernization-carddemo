package com.carddemo.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.model.codec.FixedWidthCodec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Decodes the ASCII sample data of {@code app/data} with the copybook layouts. */
class SampleDataRoundTripTest {

    private static final Path DATA = Path.of("..", "..", "app", "data", "ASCII");

    static List<Arguments> sampleFiles() {
        return List.of(
                Arguments.of("acctdata.txt", Account.class),
                Arguments.of("carddata.txt", Card.class),
                Arguments.of("cardxref.txt", CardXref.class),
                Arguments.of("custdata.txt", Customer.class),
                Arguments.of("dailytran.txt", DailyTransaction.class),
                Arguments.of("discgrp.txt", DisclosureGroup.class),
                Arguments.of("tcatbal.txt", TransactionCategoryBalance.class),
                Arguments.of("trancatg.txt", TransactionCategory.class),
                Arguments.of("trantype.txt", TransactionType.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sampleFiles")
    void decodeAndEncodeReproduceTheRecordImage(String fileName, Class<?> recordType) throws IOException {
        int recordLength = FixedWidthCodec.recordLength(recordType);
        List<String> lines = Files.readAllLines(DATA.resolve(fileName), StandardCharsets.ISO_8859_1);

        assertThat(lines).isNotEmpty();
        for (String line : lines) {
            assertThat(line.length()).as("record length of %s", fileName).isLessThanOrEqualTo(recordLength);
            String recordImage = line + " ".repeat(recordLength - line.length());
            Object record = FixedWidthCodec.decode(line, recordType);
            assertThat(FixedWidthCodec.encode(record)).isEqualTo(recordImage);
        }
    }

    @Test
    void readsTheFirstAccountOfTheSampleData() throws IOException {
        String line = Files.readAllLines(DATA.resolve("acctdata.txt"), StandardCharsets.ISO_8859_1).get(0);

        Account account = FixedWidthCodec.decode(line, Account.class);

        assertThat(account.getId()).isEqualTo(1L);
        assertThat(account.getActiveStatus()).isEqualTo("Y");
        // The sample data carries the account group id in the ACCT-ADDR-ZIP positions.
        assertThat(account.getAddrZip()).isEqualTo("A000000000");
        assertThat(account.getGroupId()).isEmpty();
    }

    @Test
    void readsTheCardCrossReferenceOfTheSampleData() throws IOException {
        String line = Files.readAllLines(DATA.resolve("cardxref.txt"), StandardCharsets.ISO_8859_1).get(0);

        CardXref xref = FixedWidthCodec.decode(line, CardXref.class);

        assertThat(xref.getCardNum()).hasSize(16);
        assertThat(xref.getCustId()).isNotNull();
        assertThat(xref.getAcctId()).isNotNull();
    }
}
