package com.carddemo.batch.cbact04c;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.carddemo.batch.core.BatchConfig;
import com.carddemo.batch.core.CardDemoFile;
import com.carddemo.batch.core.io.SequentialFileReader;
import com.carddemo.batch.core.record.AccountRecord;
import com.carddemo.batch.core.record.TranRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Byte-for-byte comparison against golden files produced by the original {@code CBACT04C.cbl}
 * under GnuCOBOL ({@code src/test/cobol/run-gnucobol.sh}, PARM {@value #PARM_DATE},
 * {@code COB_CURRENT_DATE="2022/07/18 10:30:00"}) and hand-checked below.
 */
class Cbact04cGoldenTest {

    static final String PARM_DATE = "2022071800";
    static final Clock CLOCK = Clock.fixed(Instant.parse("2022-07-18T10:30:00Z"), ZoneOffset.UTC);

    @TempDir
    Path out;

    private final ByteArrayOutputStream sysout = new ByteArrayOutputStream();

    private Cbact04cJob.Result run(Path inputDir) {
        return new Cbact04cJob(new BatchConfig(inputDir, out), PARM_DATE, CLOCK,
                new PrintStream(sysout, true)).run();
    }

    static Path resource(String name) {
        try {
            return Path.of(Objects.requireNonNull(Cbact04cGoldenTest.class.getResource("/" + name), name).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private void assertMatchesGolden(String goldenCase) throws IOException {
        Path golden = resource("golden/" + goldenCase);
        assertArrayEquals(Files.readAllBytes(golden.resolve(Cbact04cJob.TRANSACT_FILE)),
                Files.readAllBytes(out.resolve(Cbact04cJob.TRANSACT_FILE)), "systran.txt differs");
        assertArrayEquals(Files.readAllBytes(golden.resolve(Cbact04cJob.ACCOUNT_FILE)),
                Files.readAllBytes(out.resolve(Cbact04cJob.ACCOUNT_FILE)), "acctdata.txt differs");
    }

    private List<TranRecord> transactions() {
        return SequentialFileReader.readAll(out.resolve(Cbact04cJob.TRANSACT_FILE), TranRecord.LENGTH,
                TranRecord::fromBytes);
    }

    private List<AccountRecord> accounts() {
        return SequentialFileReader.readAll(out.resolve(Cbact04cJob.ACCOUNT_FILE), AccountRecord.LENGTH,
                AccountRecord::fromBytes);
    }

    private static List<AccountRecord> accounts(Path inputDir) {
        return SequentialFileReader.readAll(inputDir.resolve(CardDemoFile.ACCTDATA.fileName()), AccountRecord.LENGTH,
                AccountRecord::fromBytes);
    }

    @Test
    void fullFixtureRun() throws IOException {
        Path input = BatchConfig.defaultInputDir();
        byte[][] before = readInputs(input);

        Cbact04cJob.Result result = run(input);

        assertMatchesGolden("full");
        assertEquals(50, result.recordCount());
        // Every account has a blank ACCT-GROUP-ID, so each lookup falls back to DEFAULT/01/0001
        // (15.00%); all balances are zero, so 50 zero-amount transactions are written.
        assertEquals(50, result.transactionsWritten());
        assertEquals(49, result.accountsRewritten());
        List<TranRecord> tx = transactions();
        TranRecord first = tx.get(0);
        assertEquals("2022071800000001", first.id());
        assertEquals("01", first.typeCd());
        assertEquals(5, first.catCd());
        assertEquals("System    ", first.source());
        assertEquals("Int. for a/c 00000000001", first.desc().strip());
        assertEquals(0, first.amt().signum());
        assertEquals(0L, first.merchantId());
        assertEquals(" ".repeat(50), first.merchantName());
        assertEquals("9680294154603697", first.cardNum()); // xref read by alternate key XREF-ACCT-ID
        assertEquals("2022-07-18-10.30.00.000000", first.origTs());
        assertEquals("2022-07-18-10.30.00.000000", first.procTs());
        assertEquals("2022071800000050", tx.get(49).id());
        assertEquals(50, sysout.toString().lines().filter(l -> l.contains("DISCLOSURE GROUP RECORD MISSING")).count());
        // app/data/ASCII is only read
        assertArrayEquals(before, readInputs(input));
    }

    /**
     * Three accounts in TCATBALF (11, 12, 13) plus one that is not (14).
     * <pre>
     * 11/01/0001   1000.01 x 15.00 / 1200 =  12.500125 ->  12.50
     * 11/01/0002   -100.07 x 25.00 / 1200 =  -2.084791 ->  -2.08
     * 11/02/0001    500.00 x  0.00          skipped
     *   break: ACCT-CURR-BAL 1500.00 + 10.42 = 1510.42, cycle credit/debit -> 0
     * 12/01/0003   2345.67 x 25.00 / 1200 =  48.868125 ->  48.86
     * 12/04/0001     99.99 x 15.00 / 1200 =   1.249875 ->   1.24
     *   break: ACCT-CURR-BAL -20.50 + 50.10 = 29.60, cycle credit/debit -> 0
     * 13/01/0001    800.00 x 15.00 / 1200 =  10.00 (written)
     *   EOF: no break, so account 13 keeps balance 800.00 and cycle 33.33/44.44
     * </pre>
     */
    @Test
    void multiAccountBreakLogic() throws IOException {
        Path input = resource("fixtures/multi-account");
        Cbact04cJob.Result result = run(input);

        assertMatchesGolden("multi-account");
        assertEquals(6, result.recordCount());
        assertEquals(5, result.transactionsWritten());
        assertEquals(2, result.accountsRewritten());
        List<TranRecord> tx = transactions();
        assertEquals(List.of("12.50", "-2.08", "48.86", "1.24", "10.00"),
                tx.stream().map(t -> t.amt().toPlainString()).toList());
        assertEquals(List.of("2022071800000001", "2022071800000002", "2022071800000003", "2022071800000004",
                "2022071800000005"), tx.stream().map(TranRecord::id).toList());
        assertEquals(List.of("4111000000000011", "4111000000000011", "4111000000000012", "4111000000000012",
                "4111000000000013"), tx.stream().map(TranRecord::cardNum).toList());
        List<AccountRecord> accounts = accounts();
        assertAccount(accounts.get(0), "1510.42", "0.00", "0.00");
        assertAccount(accounts.get(1), "29.60", "0.00", "0.00");
        assertEquals(accounts(input).get(2), accounts.get(2), "last account in TCATBALF is not rewritten");
        assertEquals(accounts(input).get(3), accounts.get(3));
    }

    /**
     * Accounts 21 and 23 have group NOGROUP, which DISCGRP lacks, so 1200-A-GET-DEFAULT-INT-RATE
     * reads DEFAULT with the same type/category.
     * <pre>
     * 21/01/0001  1200.00 x DEFAULT 18.00 / 1200 = 18.00
     * 21/07/0001   300.00 x DEFAULT  0.00          skipped (A000000000 would have been 12.00)
     *   break: 100.00 + 18.00 = 118.00
     * 22/01/0001   120.00 x A000000000 15.00 / 1200 = 1.50
     * 22/07/0001   100.00 x A000000000 12.00 / 1200 = 1.00
     *   break: 200.00 + 2.50 = 202.50
     * 23/01/0001    50.00 x DEFAULT 18.00 / 1200 = 0.75 (written; account 23 not rewritten)
     * </pre>
     */
    @Test
    void defaultDisclosureGroupFallback() throws IOException {
        Path input = resource("fixtures/default-group");
        Cbact04cJob.Result result = run(input);

        assertMatchesGolden("default-group");
        assertEquals(4, result.transactionsWritten());
        assertEquals(List.of("18.00", "1.50", "1.00", "0.75"),
                transactions().stream().map(t -> t.amt().toPlainString()).toList());
        List<AccountRecord> accounts = accounts();
        assertAccount(accounts.get(0), "118.00", "0.00", "0.00");
        assertAccount(accounts.get(1), "202.50", "0.00", "0.00");
        assertEquals(accounts(input).get(2), accounts.get(2));
        assertEquals(3, sysout.toString().lines().filter(l -> l.equals("TRY WITH DEFAULT GROUP CODE")).count());
    }

    /**
     * DIS-INT-RATE = 0 writes no transaction, but the account break still rewrites the account.
     * <pre>
     * 31 ZEROAPR 01/0001, 01/0002: rate 0, skipped; break: 400.00 + 0, cycle credit/debit -> 0
     * 32 A000000000 01/0001: rate 0, skipped
     * 32 A000000000 01/0002: 1234.56 x -6.00 / 1200 = -6.1728 -> -6.17; break: 500.00 - 6.17 = 493.83
     * 33 ZEROAPR 01/0001: rate 0, skipped (not rewritten)
     * </pre>
     */
    @Test
    void zeroRateIsSkipped() throws IOException {
        Path input = resource("fixtures/zero-rate");
        Cbact04cJob.Result result = run(input);

        assertMatchesGolden("zero-rate");
        assertEquals(5, result.recordCount());
        assertEquals(1, result.transactionsWritten());
        TranRecord only = transactions().get(0);
        assertEquals("2022071800000001", only.id());
        assertEquals(new BigDecimal("-6.17"), only.amt());
        assertEquals("Int. for a/c 00000000032", only.desc().strip());
        List<AccountRecord> accounts = accounts();
        assertAccount(accounts.get(0), "400.00", "0.00", "0.00");
        assertAccount(accounts.get(1), "493.83", "0.00", "0.00");
        assertEquals(accounts(input).get(2), accounts.get(2));
    }

    private static void assertAccount(AccountRecord account, String bal, String cycCredit, String cycDebit) {
        assertEquals(new BigDecimal(bal), account.currBal());
        assertEquals(new BigDecimal(cycCredit), account.currCycCredit());
        assertEquals(new BigDecimal(cycDebit), account.currCycDebit());
    }

    private static byte[][] readInputs(Path dir) throws IOException {
        CardDemoFile[] files = {CardDemoFile.TCATBAL, CardDemoFile.ACCTDATA, CardDemoFile.CARDXREF, CardDemoFile.DISCGRP};
        byte[][] contents = new byte[files.length][];
        for (int i = 0; i < files.length; i++) {
            contents[i] = Files.readAllBytes(dir.resolve(files[i].fileName()));
        }
        return contents;
    }
}
