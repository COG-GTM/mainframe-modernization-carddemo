package com.carddemo.batch.cbact04c;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carddemo.batch.core.BatchConfig;
import com.carddemo.batch.core.io.SequentialFileReader;
import com.carddemo.batch.core.record.AccountRecord;
import com.carddemo.batch.core.record.CardXrefRecord;
import com.carddemo.batch.core.record.DiscGroupRecord;
import com.carddemo.batch.core.record.TranCatBalRecord;
import com.carddemo.batch.core.record.TranRecord;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Cbact04cJobTest {

    private static final String GROUP = "A000000000";

    @TempDir
    Path in;

    @TempDir
    Path out;

    @BeforeEach
    void writeInputs() throws IOException {
        write("acctdata.txt", account(1, "100.00", "1.00", "2.00").toBytes(), account(2, "200.00", "3.00", "4.00").toBytes());
        write("cardxref.txt", new CardXrefRecord("4000000000000001", 1, 1, "").toBytes(),
                new CardXrefRecord("4000000000000002", 2, 2, "").toBytes());
        write("discgrp.txt", new DiscGroupRecord(GROUP, "01", 1, new BigDecimal("12.00"), "").toBytes(),
                new DiscGroupRecord("DEFAULT", "01", 1, new BigDecimal("24.00"), "").toBytes());
        write("tcatbal.txt", tcat(1, "100.00"), tcat(2, "100.00"));
    }

    private Cbact04cJob job(String parmDate, Clock clock) {
        return new Cbact04cJob(new BatchConfig(in, out), parmDate, clock, new PrintStream(OutputStream.nullOutputStream()));
    }

    private Cbact04cJob job() {
        return job("2022071800", Cbact04cGoldenTest.CLOCK);
    }

    @Test
    void emptyTcatbalWritesNothingAndRewritesNoAccount() throws IOException {
        write("tcatbal.txt");
        Cbact04cJob.Result result = job().run();
        assertEquals(0, result.recordCount());
        assertEquals(0, result.accountsRewritten());
        assertEquals(0, Files.size(out.resolve(Cbact04cJob.TRANSACT_FILE)));
        assertEquals(Files.readString(in.resolve("acctdata.txt")), Files.readString(out.resolve("acctdata.txt")));
    }

    @Test
    void timestampComesFromClockAndParmDateIsPaddedToTenCharacters() {
        Clock clock = Clock.fixed(Instant.parse("2024-02-29T23:59:58.456Z"), ZoneOffset.UTC);
        job("20240229", clock).run();
        TranRecord tx = transactions().get(0);
        assertEquals("20240229  000001", tx.id());
        assertEquals("2024-02-29-23.59.58.450000", tx.origTs());
        assertEquals(tx.origTs(), tx.procTs());
        assertEquals(" ".repeat(20), tx.filler());
    }

    @Test
    void missingAccountAbendsWithStatus23AndDeletesTransactFile() throws IOException {
        write("tcatbal.txt", tcat(1, "100.00"), tcat(3, "100.00"));
        Cbact04cJob.AbendException e = assertThrows(Cbact04cJob.AbendException.class, () -> job().run());
        assertEquals("23", e.fileStatus());
        assertTrue(e.getMessage().startsWith("ERROR READING ACCOUNT FILE"));
        assertFalse(Files.exists(out.resolve(Cbact04cJob.TRANSACT_FILE)));
        // the rewrite of account 1 at the break had already reached the KSDS
        AccountRecord first = SequentialFileReader.readAll(out.resolve("acctdata.txt"), AccountRecord.LENGTH,
                AccountRecord::fromBytes).get(0);
        assertEquals(new BigDecimal("101.00"), first.currBal());
    }

    @Test
    void missingXrefAbends() throws IOException {
        write("cardxref.txt", new CardXrefRecord("4000000000000001", 1, 1, "").toBytes());
        Cbact04cJob.AbendException e = assertThrows(Cbact04cJob.AbendException.class, () -> job().run());
        assertEquals("23", e.fileStatus());
        assertTrue(e.getMessage().startsWith("ERROR READING XREF FILE"));
    }

    @Test
    void missingDefaultDisclosureGroupAbends() throws IOException {
        write("acctdata.txt", account(1, "100.00", "1.00", "2.00").toBytes(), account(2, "200.00", "0", "0")
                .withCurrBal(BigDecimal.ONE).toBytes());
        write("discgrp.txt", new DiscGroupRecord("OTHER", "01", 1, new BigDecimal("12.00"), "").toBytes());
        Cbact04cJob.AbendException e = assertThrows(Cbact04cJob.AbendException.class, () -> job().run());
        assertEquals("23", e.fileStatus());
        assertTrue(e.getMessage().startsWith("ERROR READING DEFAULT DISCLOSURE GROUP"));
    }

    @Test
    void interestIsTruncatedNotRounded() throws IOException {
        // 0.99 x 12.00 / 1200 = 0.0099 -> 0.00 (still written: the rate is not zero)
        write("tcatbal.txt", tcat(1, "0.99"), tcat(2, "-0.99"));
        job().run();
        List<TranRecord> tx = transactions();
        assertEquals(2, tx.size());
        assertEquals(new BigDecimal("0.00"), tx.get(0).amt());
        assertEquals(new BigDecimal("0.00"), tx.get(1).amt());
    }

    @Test
    void parmDateResolution() {
        assertEquals("2022071800", Cbact04cJob.resolveParmDate(new String[] {"2022071800"}));
        String previous = System.getProperty(Cbact04cJob.PARM_DATE_PROPERTY);
        try {
            System.setProperty(Cbact04cJob.PARM_DATE_PROPERTY, "2023010100");
            assertEquals("2023010100", Cbact04cJob.resolveParmDate(new String[0]));
            assertEquals("2022071800", Cbact04cJob.resolveParmDate(new String[] {"2022071800"}));
            if (System.getenv(Cbact04cJob.PARM_DATE_ENV) == null) {
                System.clearProperty(Cbact04cJob.PARM_DATE_PROPERTY);
                assertThrows(IllegalArgumentException.class, () -> Cbact04cJob.resolveParmDate(new String[0]));
            }
        } finally {
            if (previous == null) {
                System.clearProperty(Cbact04cJob.PARM_DATE_PROPERTY);
            } else {
                System.setProperty(Cbact04cJob.PARM_DATE_PROPERTY, previous);
            }
        }
    }

    private List<TranRecord> transactions() {
        return SequentialFileReader.readAll(out.resolve(Cbact04cJob.TRANSACT_FILE), TranRecord.LENGTH,
                TranRecord::fromBytes);
    }

    private static AccountRecord account(long id, String bal, String cycCredit, String cycDebit) {
        return new AccountRecord(id, "Y", new BigDecimal(bal), new BigDecimal("5000.00"), new BigDecimal("1000.00"),
                "2014-11-20", "2025-05-20", "2025-05-20", new BigDecimal(cycCredit), new BigDecimal(cycDebit),
                "12345", GROUP, "");
    }

    private static byte[] tcat(long acct, String bal) {
        return new TranCatBalRecord(acct, "01", 1, new BigDecimal(bal)).toBytes();
    }

    private void write(String name, byte[]... records) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (byte[] r : records) {
            sb.append(new String(r, java.nio.charset.StandardCharsets.ISO_8859_1)).append('\n');
        }
        Files.writeString(in.resolve(name), sb.toString(), java.nio.charset.StandardCharsets.ISO_8859_1);
    }
}
