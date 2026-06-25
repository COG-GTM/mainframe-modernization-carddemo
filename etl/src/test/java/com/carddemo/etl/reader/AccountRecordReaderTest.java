package com.carddemo.etl.reader;

import com.carddemo.etl.model.Account;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRecordReaderTest {

    private static String record(long id, char status) {
        return String.format("%011d", id)
                + status
                + "00000001940{"
                + "00000020200{"
                + "00000010200{"
                + "2014-11-202025-05-202025-05-20"
                + "00000000000{"
                + "00000000000{"
                + "A000000000"
                + " ".repeat(10)
                + " ".repeat(178);
    }

    @Test
    void readsLineDelimitedAsciiRecords() {
        String content = record(1, 'Y') + "\n" + record(2, 'N') + "\n";
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1));

        List<Account> accounts;
        try (Stream<Account> stream = new AccountRecordReader(SourceFormat.ASCII).read(in)) {
            accounts = stream.collect(Collectors.toList());
        }

        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0).acctId()).isEqualTo(1L);
        assertThat(accounts.get(1).acctId()).isEqualTo(2L);
        assertThat(accounts.get(1).activeStatus()).isEqualTo("N");
    }

    @Test
    void readsFixedLengthEbcdicRecords() {
        Charset cp037 = Charset.forName("Cp037");
        String content = record(1, 'Y') + record(2, 'N'); // no delimiters
        ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(cp037));

        List<Account> accounts;
        try (Stream<Account> stream = new AccountRecordReader(SourceFormat.EBCDIC).read(in)) {
            accounts = stream.collect(Collectors.toList());
        }

        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0).acctId()).isEqualTo(1L);
        assertThat(accounts.get(0).currBal()).isEqualByComparingTo("194.00");
        assertThat(accounts.get(1).acctId()).isEqualTo(2L);
    }

    @Test
    void asciiAndEbcdicYieldEqualAccounts() {
        String content = record(7, 'Y');
        Account fromAscii;
        try (Stream<Account> stream = new AccountRecordReader(SourceFormat.ASCII)
                .read(new ByteArrayInputStream((content + "\n").getBytes(StandardCharsets.ISO_8859_1)))) {
            fromAscii = stream.findFirst().orElseThrow();
        }
        Account fromEbcdic;
        try (Stream<Account> stream = new AccountRecordReader(SourceFormat.EBCDIC)
                .read(new ByteArrayInputStream(content.getBytes(Charset.forName("Cp037"))))) {
            fromEbcdic = stream.findFirst().orElseThrow();
        }
        assertThat(fromEbcdic).isEqualTo(fromAscii);
    }
}
