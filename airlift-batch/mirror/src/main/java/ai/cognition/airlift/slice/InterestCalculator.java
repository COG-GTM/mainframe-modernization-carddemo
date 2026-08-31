package ai.cognition.airlift.slice;

import ai.cognition.airlift.MirrorProperties;
import ai.cognition.airlift.codec.RecordBuffer;
import ai.cognition.airlift.store.Datasets;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * The mirror of CBACT04C: walk the transaction-category balances in key order,
 * write one interest transaction per category and roll the interest into the
 * account at each account break.
 */
@Component
public class InterestCalculator {

  private final Datasets datasets;
  private final Db2Timestamp timestamp;
  private final String parmDate;

  private String lastAccountNumber = "";
  private boolean firstTime = true;
  private BigDecimal totalInterest = BigDecimal.ZERO;
  private RecordBuffer account;
  private RecordBuffer cardXref;
  private int transactionIdSuffix;
  private int recordCount;

  public InterestCalculator(Datasets datasets, MirrorProperties properties) {
    this.datasets = datasets;
    this.timestamp = new Db2Timestamp(properties.clock());
    this.parmDate = properties.parmDate();
  }

  public int recordCount() {
    return recordCount;
  }

  /** Fresh WORKING-STORAGE, as a new run of the program would have. */
  public void reset() {
    lastAccountNumber = "";
    firstTime = true;
    totalInterest = BigDecimal.ZERO;
    account = null;
    cardXref = null;
    transactionIdSuffix = 0;
    recordCount = 0;
  }

  /**
   * One iteration of the main loop (CBACT04C:188-222).
   *
   * @return the interest transaction to append to the output dataset, when the
   *     category carries a non-zero rate
   */
  public Optional<byte[]> handle(byte[] image) {
    recordCount++;
    RecordBuffer balance = RecordBuffer.of(datasets.layouts().tranCatBal(), image);
    String accountId = balance.text(Datasets.TRANCAT_ACCT_ID);
    if (!accountId.equals(lastAccountNumber)) {
      if (!firstTime) {
        updateAccount();
      } else {
        firstTime = false;
      }
      totalInterest = BigDecimal.ZERO;
      lastAccountNumber = accountId;
      account = read(accountId);
      cardXref = crossReference(accountId);
    }
    BigDecimal rate = interestRate(balance);
    if (rate.signum() == 0) {
      // CBACT04C:214 - a zero rate skips both interest and the unimplemented
      // 1400-COMPUTE-FEES paragraph.
      return Optional.empty();
    }
    return Optional.of(computeInterest(balance, rate));
  }

  /**
   * End of the input dataset.
   *
   * <p>CBACT04C:219-221 has an ELSE branch that performs 1050-UPDATE-ACCOUNT, but
   * the PERFORM UNTIL condition is retested as soon as 1000-TCATBALF-GET-NEXT sets
   * END-OF-FILE, so the loop exits without ever taking that branch: the interest of
   * the last account in key order is never written back. The mirror reproduces the
   * behaviour of the program as written, not the behaviour the ELSE intends.
   */
  public void finish() {
    // Intentionally empty; see the note above.
  }

  /** 1300-COMPUTE-INTEREST (CBACT04C:462-470) and 1300-B-WRITE-TX (CBACT04C:473-515). */
  private byte[] computeInterest(RecordBuffer balance, BigDecimal rate) {
    BigDecimal monthlyInterest = Interest.monthly(balance.number("TRAN-CAT-BAL"), rate);
    totalInterest = totalInterest.add(monthlyInterest);
    transactionIdSuffix++;

    RecordBuffer transaction = RecordBuffer.blank(datasets.layouts().transaction());
    transaction.setText("TRAN-ID", parmDate + pad(transactionIdSuffix));
    transaction.setText("TRAN-TYPE-CD", "01");
    transaction.setNumber("TRAN-CAT-CD", 5);
    transaction.setText("TRAN-SOURCE", "System");
    transaction.setText("TRAN-DESC", "Int. for a/c " + account.text("ACCT-ID"));
    transaction.setNumber("TRAN-AMT", monthlyInterest);
    transaction.setNumber("TRAN-MERCHANT-ID", 0);
    transaction.setText("TRAN-CARD-NUM", cardXref.text(Datasets.XREF_CARD_NUM));
    transaction.setText("TRAN-ORIG-TS", timestamp.value());
    transaction.setText("TRAN-PROC-TS", timestamp.value());
    return transaction.toBytes();
  }

  /** 1200-GET-INTEREST-RATE (CBACT04C:415-440), including the DEFAULT group retry. */
  private BigDecimal interestRate(RecordBuffer balance) {
    // TRAP T2: ACCT-GROUP-ID is PIC X(10) (CVACT01Y:20) and the KSDS key it is
    // moved into is PIC X(10) as well (CVTRA02Y:12), so CBACT04C:416 reads with a
    // blank-padded group id. Trimming it here makes every keyed read miss and
    // silently fall through to the DEFAULT group rate.
    String group = account.text("ACCT-GROUP-ID").trim();
    String typeCode = balance.text(Datasets.TRANCAT_TYPE_CD);
    String categoryCode = balance.text(Datasets.TRANCAT_CD);
    Optional<byte[]> stored = datasets.disclosureGroups().read(group + typeCode + categoryCode);
    if (stored.isEmpty()) {
      // CBACT04C:437 moves the literal 'DEFAULT' into a PIC X(10) key field, so
      // the retry looks for a group id blank-padded to ten characters.
      stored =
          datasets
              .disclosureGroups()
              .read(padRight("DEFAULT", 10) + typeCode + categoryCode);
      if (stored.isEmpty()) {
        // The COBOL treats a missing DEFAULT row as unrecoverable (CBACT04C:455).
        throw new IllegalStateException(
            "no disclosure group row for " + group + typeCode + categoryCode + " and no DEFAULT row");
      }
    }
    return RecordBuffer.of(datasets.layouts().disclosureGroup(), stored.get())
        .number("DIS-INT-RATE");
  }

  /** 1050-UPDATE-ACCOUNT (CBACT04C:350-370). */
  private void updateAccount() {
    account.add("ACCT-CURR-BAL", totalInterest);
    account.setNumber("ACCT-CURR-CYC-CREDIT", BigDecimal.ZERO);
    account.setNumber("ACCT-CURR-CYC-DEBIT", BigDecimal.ZERO);
    datasets.accounts().rewrite(account.text("ACCT-ID"), account.toBytes());
  }

  private RecordBuffer read(String accountId) {
    return RecordBuffer.of(
        datasets.layouts().account(),
        datasets
            .accounts()
            .read(accountId)
            .orElseThrow(() -> new IllegalStateException("ACCOUNT NOT FOUND: " + accountId)));
  }

  /** 1110-GET-XREF-DATA (CBACT04C:393-413) reads the cross-reference by its alternate key. */
  private RecordBuffer crossReference(String accountId) {
    return RecordBuffer.of(
        datasets.layouts().cardXref(),
        datasets
            .cardXref()
            .readByAlternate(accountId)
            .orElseThrow(() -> new IllegalStateException("ACCOUNT NOT FOUND: " + accountId)));
  }

  private static String pad(int suffix) {
    String digits = String.valueOf(suffix);
    return "0".repeat(6 - digits.length()) + digits;
  }

  private static String padRight(String value, int width) {
    return value + " ".repeat(width - value.length());
  }
}
