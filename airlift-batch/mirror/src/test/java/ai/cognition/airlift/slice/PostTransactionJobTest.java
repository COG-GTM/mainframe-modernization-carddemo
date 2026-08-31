package ai.cognition.airlift.slice;

import static org.assertj.core.api.Assertions.assertThat;

import ai.cognition.airlift.codec.Layouts;
import ai.cognition.airlift.codec.RecordBuffer;
import ai.cognition.airlift.io.FixedRecordFile;
import ai.cognition.airlift.store.Datasets;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * The job chain over a dataset small enough to reason about, covering every outcome
 * CBTRN02C can produce - posted, and each of its four reject reasons - and the
 * account update CBACT04C never reaches for the last account in the file.
 *
 * <p>These are the mirror's own tests, and they are green. They assert what the
 * mirror does rather than what the COBOL does, which is the point of the demo: a
 * green test suite is not evidence that the lift is faithful. Only the two-sided
 * comparison against the COBOL output is.
 */
@SpringBootTest(properties = "spring.batch.job.enabled=false")
class PostTransactionJobTest {

  @TempDir static Path directory;

  private static Layouts layouts;
  private static int run;

  @Autowired private JobLauncher launcher;

  @Autowired
  @Qualifier("posttranIntcalc")
  private Job job;

  @DynamicPropertySource
  static void datasets(DynamicPropertyRegistry registry) {
    registry.add("airlift.in", () -> directory.resolve("in"));
    registry.add("airlift.out", () -> directory.resolve("out"));
  }

  @BeforeAll
  static void writeFixtures() throws IOException {
    layouts = new Layouts(Path.of("../../app/cpy"));
    Path in = directory.resolve("in");
    Files.createDirectories(in);

    write(
        in.resolve("acctdata.dat"),
        List.of(
            account("00000000001", "2030-12-31", "1000.00"),
            account("00000000002", "2030-12-31", "10.00"),
            account("00000000003", "2021-06-30", "10.00"),
            account("00000000004", "2022-07-18", "1000.00")));
    write(
        in.resolve("cardxref.dat"),
        List.of(
            xref("4111111111111111", "00000000001"),
            xref("4111111111111112", "00000000002"),
            xref("4111111111111113", "00000000003"),
            xref("4111111111111114", "00000000004"),
            xref("4111111111111199", "00000000099")));
    write(
        in.resolve("discgrp.dat"),
        List.of(
            disclosureGroup("DEFAULT", "01", 1, "24.99"),
            disclosureGroup("GROUP001", "01", 1, "12.99")));
    write(in.resolve("tcatbal.dat"), List.of());
    write(
        in.resolve("dalytran.dat"),
        List.of(
            dailyTransaction("TX00000000000001", "4111111111111111", "100.00"),
            dailyTransaction("TX00000000000002", "4000000000000000", "100.00"),
            dailyTransaction("TX00000000000003", "4111111111111199", "100.00"),
            dailyTransaction("TX00000000000004", "4111111111111112", "100.00"),
            dailyTransaction("TX00000000000005", "4111111111111113", "100.00"),
            dailyTransaction("TX00000000000006", "4111111111111114", "1000.00")));
  }

  @BeforeEach
  void runTheChain() throws Exception {
    run++;
    assertThat(
            launcher
                .run(job, new JobParametersBuilder().addLong("run", (long) run).toJobParameters())
                .getStatus())
        .isEqualTo(BatchStatus.COMPLETED);
  }

  @Test
  void postsWhatValidatesAndRejectsWhatDoesNot() {
    assertThat(transactionIds("transact.dat")).containsExactly("TX00000000000001");
    assertThat(rejects())
        .containsExactly(
            Map.entry("TX00000000000002", "0100"),
            Map.entry("TX00000000000003", "0101"),
            Map.entry("TX00000000000004", "0102"),
            Map.entry("TX00000000000005", "0102"),
            Map.entry("TX00000000000006", "0103"));
  }

  /** 2700-UPDATE-TCATBAL creates the category balance when the account has none. */
  @Test
  void aPostedTransactionCreatesItsCategoryBalance() {
    List<RecordBuffer> balances = records("tcatbal.dat", layouts.tranCatBal());

    assertThat(balances.stream().map(record -> record.text(Datasets.TRANCAT_ACCT_ID)))
        .containsExactly("00000000001");
    assertThat(balances.get(0).number("TRAN-CAT-BAL")).isEqualByComparingTo(new BigDecimal("100.00"));
  }

  /** Interest is one transaction per category balance, in the two decimals of WS-MONTHLY-INT. */
  @Test
  void interestIsWrittenPerCategoryBalance() {
    List<RecordBuffer> interest = records("systran.dat", layouts.transaction());

    assertThat(interest).hasSize(1);
    assertThat(interest.get(0).text("TRAN-DESC").trim()).isEqualTo("Int. for a/c 00000000001");
    assertThat(interest.get(0).number("TRAN-AMT")).isEqualByComparingTo(new BigDecimal("2.08"));
    assertThat(interest.get(0).text("TRAN-ID")).isEqualTo("2022071800000001");
  }

  /**
   * The account break rolls the interest into the account and clears the cycle
   * totals, but the last account in key order never gets there: CBACT04C's loop
   * exits as soon as the read hits end of file, so the ELSE at CBACT04C:219-221 that
   * would perform 1050-UPDATE-ACCOUNT is unreachable.
   */
  @Test
  void theLastAccountInTheFileKeepsItsPrePostingBalances() {
    RecordBuffer last = account("00000000001");
    assertThat(last.number("ACCT-CURR-BAL")).isEqualByComparingTo(new BigDecimal("100.00"));
    assertThat(last.number("ACCT-CURR-CYC-CREDIT")).isEqualByComparingTo(new BigDecimal("100.00"));
  }

  private List<RecordBuffer> records(String dataset, ai.cognition.airlift.copybook.RecordLayout l) {
    return FixedRecordFile.read(directory.resolve("out").resolve(dataset), l.length()).stream()
        .map(image -> RecordBuffer.of(l, image))
        .toList();
  }

  private List<String> transactionIds(String dataset) {
    return records(dataset, layouts.transaction()).stream()
        .map(record -> record.text("TRAN-ID"))
        .toList();
  }

  private RecordBuffer account(String id) {
    return records("acctdata.dat", layouts.account()).stream()
        .filter(record -> record.text("ACCT-ID").equals(id))
        .findFirst()
        .orElseThrow();
  }

  private Map<String, String> rejects() {
    return FixedRecordFile.read(
            directory.resolve("out/dalyrejs.dat"), TransactionPoster.REJECT_LENGTH)
        .stream()
        .collect(
            Collectors.toMap(
                image -> new String(image, 0, 16, StandardCharsets.ISO_8859_1),
                image -> new String(image, 350, 4, StandardCharsets.ISO_8859_1),
                (first, second) -> first,
                LinkedHashMap::new));
  }

  private static byte[] account(String id, String expiry, String creditLimit) {
    RecordBuffer record = RecordBuffer.blank(layouts.account());
    record.setNumber("ACCT-ID", Long.parseLong(id));
    record.setText("ACCT-ACTIVE-STATUS", "Y");
    record.setNumber("ACCT-CURR-BAL", BigDecimal.ZERO);
    record.setNumber("ACCT-CREDIT-LIMIT", new BigDecimal(creditLimit));
    record.setNumber("ACCT-CASH-CREDIT-LIMIT", new BigDecimal(creditLimit));
    record.setText("ACCT-OPEN-DATE", "2018-03-09");
    record.setText("ACCT-EXPIRAION-DATE", expiry);
    record.setText("ACCT-REISSUE-DATE", "2021-03-09");
    record.setText("ACCT-ADDR-ZIP", "10001");
    record.setText("ACCT-GROUP-ID", "GROUP001");
    return record.toBytes();
  }

  private static byte[] xref(String card, String accountId) {
    RecordBuffer record = RecordBuffer.blank(layouts.cardXref());
    record.setText(Datasets.XREF_CARD_NUM, card);
    record.setNumber(Datasets.XREF_ACCT_ID, Long.parseLong(accountId));
    record.setNumber("XREF-CUST-ID", Long.parseLong(accountId));
    return record.toBytes();
  }

  private static byte[] disclosureGroup(String group, String type, int category, String rate) {
    RecordBuffer record = RecordBuffer.blank(layouts.disclosureGroup());
    record.setText(Datasets.DIS_GROUP_ID, group);
    record.setText(Datasets.DIS_TYPE_CD, type);
    record.setNumber(Datasets.DIS_CAT_CD, category);
    record.setNumber("DIS-INT-RATE", new BigDecimal(rate));
    return record.toBytes();
  }

  private static byte[] dailyTransaction(String id, String card, String amount) {
    RecordBuffer record = RecordBuffer.blank(layouts.dailyTransaction());
    record.setText("DALYTRAN-ID", id);
    record.setText("DALYTRAN-TYPE-CD", "01");
    record.setNumber("DALYTRAN-CAT-CD", 1);
    record.setText("DALYTRAN-SOURCE", "POS");
    record.setText("DALYTRAN-DESC", "TEST TRANSACTION");
    record.setNumber("DALYTRAN-AMT", new BigDecimal(amount));
    record.setNumber("DALYTRAN-MERCHANT-ID", 1);
    record.setText("DALYTRAN-MERCHANT-NAME", "MERCHANT");
    record.setText("DALYTRAN-MERCHANT-CITY", "CITY");
    record.setText("DALYTRAN-MERCHANT-ZIP", "10001");
    record.setText("DALYTRAN-CARD-NUM", card);
    record.setText("DALYTRAN-ORIG-TS", "2022-07-18 10.30.00.000000");
    record.setText("DALYTRAN-PROC-TS", "2022-07-18 10.30.00.000000");
    return record.toBytes();
  }

  private static void write(Path path, List<byte[]> records) {
    try (FixedRecordFile.Writer writer = FixedRecordFile.writer(path)) {
      records.forEach(writer::write);
    }
  }
}
