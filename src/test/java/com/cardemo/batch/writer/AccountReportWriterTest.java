package com.cardemo.batch.writer;

import com.cardemo.batch.model.AccountRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.FileSystemResource;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteAccountRecordsToCsvWithHeaders() throws Exception {
        Path outputFile = tempDir.resolve("account-report.csv");
        AccountReportWriter writer = new AccountReportWriter(
                new FileSystemResource(outputFile));
        writer.open(new ExecutionContext());

        AccountRecord record = new AccountRecord();
        record.setAcctId("00000000001");
        record.setAcctActiveStatus("Y");
        record.setAcctCurrBal(new BigDecimal("1500.00"));
        record.setAcctCreditLimit(new BigDecimal("5000.00"));
        record.setAcctCashCreditLimit(new BigDecimal("1500.00"));
        record.setAcctOpenDate("2020-01-15");
        record.setAcctExpiraionDate("2025-01-15");
        record.setAcctReissueDate("2023-01-15");
        record.setAcctCurrCycCredit(new BigDecimal("200.00"));
        record.setAcctCurrCycDebit(new BigDecimal("150.00"));
        record.setAcctGroupId("GRP001");

        writer.write(new Chunk<>(List.of(record)));
        writer.close();

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("ACCT-ID"));
        assertTrue(lines.get(0).contains("ACCT-EXPIRAION-DATE"));
        assertTrue(lines.get(1).contains("00000000001"));
        assertTrue(lines.get(1).contains("GRP001"));
    }

    @Test
    void shouldIncludeAllElevenFieldsInHeader() {
        String header = AccountReportWriter.getHeader();
        assertTrue(header.contains("ACCT-ID"));
        assertTrue(header.contains("ACCT-ACTIVE-STATUS"));
        assertTrue(header.contains("ACCT-CURR-BAL"));
        assertTrue(header.contains("ACCT-CREDIT-LIMIT"));
        assertTrue(header.contains("ACCT-CASH-CREDIT-LIMIT"));
        assertTrue(header.contains("ACCT-OPEN-DATE"));
        assertTrue(header.contains("ACCT-EXPIRAION-DATE"));
        assertTrue(header.contains("ACCT-REISSUE-DATE"));
        assertTrue(header.contains("ACCT-CURR-CYC-CREDIT"));
        assertTrue(header.contains("ACCT-CURR-CYC-DEBIT"));
        assertTrue(header.contains("ACCT-GROUP-ID"));
    }
}
