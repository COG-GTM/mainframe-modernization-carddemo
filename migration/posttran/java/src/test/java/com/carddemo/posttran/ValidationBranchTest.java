package com.carddemo.posttran;

import com.carddemo.posttran.domain.AccountRecord;
import com.carddemo.posttran.domain.DalytranRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behavioural assertions on the branches the drop's own sample never reaches — every reject
 * reason other than 102, both TCATBAL branches, the debit branch and the two comparison
 * boundaries. {@link BaselineParityTest} proves these outputs equal the legacy program's; this
 * test says what they mean, so a future change that breaks one of them fails with a diagnosis
 * rather than a byte diff.
 *
 * <p>The records are the ones {@code harness/gen_mock_data.py} builds for exactly this purpose.
 */
class ValidationBranchTest {

    @TempDir
    Path work;

    private StepRun run;
    private Map<String, String> rejectsByTranId;

    @BeforeEach
    void runStep() {
        run = StepRun.execute(StepRun.TESTDATA.resolve("mock"), work);
        rejectsByTranId = run.dalyrejs().stream()
                .collect(Collectors.toMap(r -> r.substring(0, 16), Function.identity()));
    }

    /** CBTRN02C.cbl:384-386 — XREF miss. */
    @Test
    void rejects100WhenTheCardIsNotInTheCrossReference() {
        assertRejected("MOCK000000000005", 100, "INVALID CARD NUMBER FOUND");
    }

    /** CBTRN02C.cbl:396-398 — XREF hit, but the account it names is absent. */
    @Test
    void rejects101WhenTheAccountIsMissing() {
        assertRejected("MOCK000000000006", 101, "ACCOUNT RECORD NOT FOUND");
    }

    /** CBTRN02C.cbl:403-412 — credit limit exceeded by cycle credit - cycle debit + amount. */
    @Test
    void rejects102WhenTheTransactionGoesOverTheCreditLimit() {
        assertRejected("MOCK000000000007", 102, "OVERLIMIT TRANSACTION");
    }

    /** CBTRN02C.cbl:414-419 — transaction dated after the account's expiration date. */
    @Test
    void rejects103WhenTheAccountHasExpired() {
        assertRejected("MOCK000000000008", 103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
    }

    /**
     * The expiration test runs after the limit test and assigns unconditionally, so it overwrites
     * a 102 already set (CBTRN02C.cbl:407-419). A transaction that is both overlimit and expired
     * is reported as expired — and only the last reason reaches the reject record.
     */
    @Test
    void reports103WhenATransactionIsBothOverlimitAndExpired() {
        assertRejected("MOCK000000000009", 103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
    }

    /** Both comparisons are {@code >=} (CBTRN02C.cbl:407, :414), so equality posts. */
    @Test
    void acceptsTheExactExpirationDateAndTheExactCreditLimit() {
        assertThat(rejectsByTranId).doesNotContainKeys("MOCK000000000010", "MOCK000000000011");
        assertThat(postedIds()).contains("MOCK000000000010", "MOCK000000000011");
    }

    /** CBTRN02C.cbl:474-500 — the READ decides between the WRITE and the REWRITE branch. */
    @Test
    void createsAMissingCategoryBalanceAndUpdatesAnExistingOne() {
        // Record 3 posts to an unused type/category, which has to be created; the DISPLAY on the
        // create branch (:479-481) is the only trace the legacy program leaves of that decision.
        assertThat(run.sysout)
                .anyMatch(line -> line.startsWith("TCATBAL record not found for key :"));

        // Several accepted records hit one existing key, so the REWRITE branch has to accumulate
        // onto the balance already there rather than replace it.
        DalytranRecord first = mockTransaction("MOCK000000000001");
        String key = accountOf(first) + first.typeCd() + first.catCd();
        BigDecimal posted = mockTransactions().stream()
                .filter(t -> !rejectsByTranId.containsKey(t.id()))
                .filter(t -> (accountOf(t) + t.typeCd() + t.catCd()).equals(key))
                .map(DalytranRecord::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(posted).as("the mock set must post more than one record to this key")
                .isNotEqualByComparingTo(BigDecimal.ZERO);
        assertThat(balanceOf(key)).isEqualByComparingTo(openingBalanceOf(key).add(posted));
    }

    /**
     * CBTRN02C.cbl:546-552 — the sign of the transaction decides which cycle bucket moves, and
     * the current balance always moves.
     */
    @Test
    void postsPositiveAmountsToCycleCreditAndNegativeAmountsToCycleDebit() {
        DalytranRecord negative = mockTransaction("MOCK000000000004");
        assertThat(negative.amount()).isNegative();

        String acctId = accountOf(negative);
        AccountRecord before = accountBefore(acctId);
        AccountRecord after = accountAfter(acctId);

        // Records 1, 2, 3 and 11 are positive and record 4 is negative, all on this account.
        BigDecimal positiveTotal = mockTransactions().stream()
                .filter(t -> t.amount().signum() >= 0)
                .filter(t -> !rejectsByTranId.containsKey(t.id()))
                .filter(t -> accountOf(t).equals(acctId))
                .map(DalytranRecord::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(after.currCycCredit())
                .isEqualByComparingTo(before.currCycCredit().add(positiveTotal));
        assertThat(after.currCycDebit())
                .isEqualByComparingTo(before.currCycDebit().add(negative.amount()));
        assertThat(after.currBal())
                .isEqualByComparingTo(before.currBal().add(positiveTotal).add(negative.amount()));
    }

    /** CBTRN02C.cbl:227-231 — the counters, and RC=4 whenever anything was rejected. */
    @Test
    void reportsCountersAndSetsReturnCode4() {
        assertThat(run.sysout).containsSequence(
                "TRANSACTIONS PROCESSED :000000011",
                "TRANSACTIONS REJECTED  :000000005");
        assertThat(run.returnCode).isEqualTo(4);
    }

    /** CBTRN02C.cbl:451-461 — the original 350 bytes, then the 4-byte reason and 76-byte text. */
    @Test
    void writesRejectsAsTheOriginalRecordPlusAnEightyByteTrailer() {
        String reject = rejectsByTranId.get("MOCK000000000005");
        assertThat(reject).hasSize(430);
        assertThat(reject.substring(0, 350))
                .isEqualTo(mockTransaction("MOCK000000000005").toString());
        assertThat(reject.substring(350, 354)).isEqualTo("0100");
        assertThat(reject.substring(354)).hasSize(76).isEqualTo(
                "INVALID CARD NUMBER FOUND" + " ".repeat(76 - "INVALID CARD NUMBER FOUND".length()));
    }

    private void assertRejected(String tranId, int reason, String description) {
        assertThat(rejectsByTranId)
                .as("transaction %s should have been rejected", tranId)
                .containsKey(tranId);
        String reject = rejectsByTranId.get(tranId);
        assertThat(reject.substring(350, 354)).isEqualTo("%04d".formatted(reason));
        assertThat(reject.substring(354).strip()).isEqualTo(description);
        assertThat(postedIds()).doesNotContain(tranId);
    }

    private List<String> postedIds() {
        return run.tranfile().stream().map(r -> r.substring(0, 16)).toList();
    }

    private List<DalytranRecord> mockTransactions() {
        return StepRun.pad(StepRun.TESTDATA.resolve("mock").resolve("dalytran.txt"), 350)
                .stream().map(DalytranRecord::new).toList();
    }

    private DalytranRecord mockTransaction(String tranId) {
        return mockTransactions().stream()
                .filter(t -> t.id().equals(tranId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no mock transaction " + tranId));
    }

    private String accountOf(DalytranRecord tran) {
        return StepRun.pad(StepRun.TESTDATA.resolve("mock").resolve("cardxref.txt"), 50)
                .stream()
                .filter(x -> x.startsWith(tran.cardNum()))
                .map(x -> x.substring(25, 36))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no XREF for " + tran.cardNum()));
    }

    private BigDecimal balanceOf(String key) {
        return categoryBalance(run.tcatbalf(), key);
    }

    private BigDecimal openingBalanceOf(String key) {
        return categoryBalance(
                StepRun.pad(StepRun.TESTDATA.resolve("mock").resolve("tcatbal.txt"), 50), key);
    }

    private static BigDecimal categoryBalance(List<String> records, String key) {
        return records.stream()
                .filter(r -> r.startsWith(key))
                .map(r -> com.carddemo.posttran.codec.Zoned.decode(r.substring(17, 28), 2))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no TCATBAL record for " + key));
    }

    private AccountRecord accountBefore(String acctId) {
        return account(
                StepRun.pad(StepRun.TESTDATA.resolve("mock").resolve("acctdata.txt"), 300), acctId);
    }

    private AccountRecord accountAfter(String acctId) {
        return account(run.acctfile(), acctId);
    }

    private static AccountRecord account(List<String> records, String acctId) {
        return records.stream()
                .filter(r -> r.startsWith(acctId))
                .map(AccountRecord::new)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no account " + acctId));
    }
}
