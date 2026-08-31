package ai.cognition.airlift.slice;

import ai.cognition.airlift.MirrorProperties;
import ai.cognition.airlift.codec.RecordBuffer;
import ai.cognition.airlift.store.Datasets;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * The mirror of CBTRN02C: validate a daily transaction, then either post it or
 * write it to the rejects dataset.
 *
 * <p>Paragraph names from the COBOL are kept in the method names so a reviewer can
 * put the two side by side.
 */
@Component
public class TransactionPoster {

  /** REJECT-RECORD is the 350-byte input record plus an 80-byte trailer (CBTRN02C:176-182). */
  public static final int REJECT_LENGTH = 430;

  private static final int TRAILER_REASON_DIGITS = 4;
  private static final int TRAILER_DESC_LENGTH = 76;

  private final Datasets datasets;
  private final Db2Timestamp timestamp;

  private int transactionCount;
  private int rejectCount;

  public TransactionPoster(Datasets datasets, MirrorProperties properties) {
    this.datasets = datasets;
    this.timestamp = new Db2Timestamp(properties.clock());
  }

  public int transactionCount() {
    return transactionCount;
  }

  public int rejectCount() {
    return rejectCount;
  }

  /** Fresh WORKING-STORAGE, as a new run of the program would have. */
  public void reset() {
    transactionCount = 0;
    rejectCount = 0;
  }

  /**
   * One iteration of the main loop (CBTRN02C:202-219): count the record, validate
   * it, then post or reject.
   *
   * @return the reject record image when the transaction failed validation
   */
  public Optional<byte[]> handle(byte[] image) {
    transactionCount++;
    RecordBuffer dailyTransaction = RecordBuffer.of(datasets.layouts().dailyTransaction(), image);
    Validation validation = validate(dailyTransaction);
    if (validation.reason() == 0) {
      post(dailyTransaction, validation);
      return Optional.empty();
    }
    rejectCount++;
    return Optional.of(rejectRecord(image, validation));
  }

  /** 1500-VALIDATE-TRAN (CBTRN02C:370-378): the account lookup only runs if the xref found a card. */
  private Validation validate(RecordBuffer dailyTransaction) {
    Optional<byte[]> xref = datasets.cardXref().read(dailyTransaction.text("DALYTRAN-CARD-NUM"));
    if (xref.isEmpty()) {
      return Validation.failed(100, "INVALID CARD NUMBER FOUND");
    }
    RecordBuffer cardXref = RecordBuffer.of(datasets.layouts().cardXref(), xref.get());
    return lookupAccount(dailyTransaction, cardXref);
  }

  /** 1500-B-LOOKUP-ACCT (CBTRN02C:393-422). */
  private Validation lookupAccount(RecordBuffer dailyTransaction, RecordBuffer cardXref) {
    String accountId = cardXref.text(Datasets.XREF_ACCT_ID);
    Optional<byte[]> stored = datasets.accounts().read(accountId);
    if (stored.isEmpty()) {
      return Validation.failed(101, "ACCOUNT RECORD NOT FOUND");
    }
    RecordBuffer account = RecordBuffer.of(datasets.layouts().account(), stored.get());
    BigDecimal amount = dailyTransaction.number("DALYTRAN-AMT");
    BigDecimal temporaryBalance =
        account
            .number("ACCT-CURR-CYC-CREDIT")
            .subtract(account.number("ACCT-CURR-CYC-DEBIT"))
            .add(amount);

    int reason = 0;
    String description = "";
    // CBTRN02C:407 - the limit check is inclusive: a transaction that lands
    // exactly on the credit limit is posted.
    if (account.number("ACCT-CREDIT-LIMIT").compareTo(temporaryBalance) < 0) {
      reason = 102;
      description = "OVERLIMIT TRANSACTION";
    }
    // TRAP T3: CBTRN02C:414 is `IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS (1:10)`,
    // so a transaction dated exactly on the expiry date is accepted. Treating the
    // boundary as exclusive rejects it.
    // TRAP T4: CBTRN02C:407-420 runs both validations unconditionally and the second
    // MOVE overwrites the first, so a transaction that is both over limit and past
    // expiry is rejected with 103. `else if` keeps 102 instead.
    else if (account
            .text("ACCT-EXPIRAION-DATE")
            .compareTo(dailyTransaction.text("DALYTRAN-ORIG-TS").substring(0, 10))
        <= 0) {
      reason = 103;
      description = "TRANSACTION RECEIVED AFTER ACCT EXPIRATION";
    }
    return new Validation(reason, description, account, accountId, amount);
  }

  /** 2000-POST-TRANSACTION (CBTRN02C:424-444). */
  private void post(RecordBuffer dailyTransaction, Validation validation) {
    RecordBuffer transaction = RecordBuffer.blank(datasets.layouts().transaction());
    transaction.setText("TRAN-ID", dailyTransaction.text("DALYTRAN-ID"));
    transaction.setText("TRAN-TYPE-CD", dailyTransaction.text("DALYTRAN-TYPE-CD"));
    transaction.setNumber("TRAN-CAT-CD", dailyTransaction.number("DALYTRAN-CAT-CD"));
    transaction.setText("TRAN-SOURCE", dailyTransaction.text("DALYTRAN-SOURCE"));
    transaction.setText("TRAN-DESC", dailyTransaction.text("DALYTRAN-DESC"));
    transaction.setNumber("TRAN-AMT", dailyTransaction.number("DALYTRAN-AMT"));
    transaction.setNumber("TRAN-MERCHANT-ID", dailyTransaction.number("DALYTRAN-MERCHANT-ID"));
    transaction.setText("TRAN-MERCHANT-NAME", dailyTransaction.text("DALYTRAN-MERCHANT-NAME"));
    transaction.setText("TRAN-MERCHANT-CITY", dailyTransaction.text("DALYTRAN-MERCHANT-CITY"));
    transaction.setText("TRAN-MERCHANT-ZIP", dailyTransaction.text("DALYTRAN-MERCHANT-ZIP"));
    transaction.setText("TRAN-CARD-NUM", dailyTransaction.text("DALYTRAN-CARD-NUM"));
    transaction.setText("TRAN-ORIG-TS", dailyTransaction.text("DALYTRAN-ORIG-TS"));
    transaction.setText("TRAN-PROC-TS", timestamp.value());

    updateTransactionCategoryBalance(dailyTransaction, validation);
    updateAccount(validation);
    datasets.transactions().write(transaction.text(Datasets.TRAN_ID), transaction.toBytes());
  }

  /** 2700-UPDATE-TCATBAL (CBTRN02C:467-500): create the category balance if it is missing. */
  private void updateTransactionCategoryBalance(
      RecordBuffer dailyTransaction, Validation validation) {
    String key =
        validation.accountId()
            + dailyTransaction.text("DALYTRAN-TYPE-CD")
            + pad(dailyTransaction.number("DALYTRAN-CAT-CD").toBigIntegerExact().toString(), 4);
    Optional<byte[]> stored = datasets.tranCatBalances().read(key);
    if (stored.isEmpty()) {
      RecordBuffer created = RecordBuffer.blank(datasets.layouts().tranCatBal());
      created.setNumber(Datasets.TRANCAT_ACCT_ID, Long.parseLong(validation.accountId()));
      created.setText(Datasets.TRANCAT_TYPE_CD, dailyTransaction.text("DALYTRAN-TYPE-CD"));
      created.setNumber(Datasets.TRANCAT_CD, dailyTransaction.number("DALYTRAN-CAT-CD"));
      created.add("TRAN-CAT-BAL", validation.amount());
      datasets.tranCatBalances().write(key, created.toBytes());
      return;
    }
    RecordBuffer balance = RecordBuffer.of(datasets.layouts().tranCatBal(), stored.get());
    balance.add("TRAN-CAT-BAL", validation.amount());
    datasets.tranCatBalances().rewrite(key, balance.toBytes());
  }

  /** 2800-UPDATE-ACCOUNT-REC (CBTRN02C:545-561). */
  private void updateAccount(Validation validation) {
    RecordBuffer account = validation.account();
    account.add("ACCT-CURR-BAL", validation.amount());
    if (validation.amount().signum() >= 0) {
      account.add("ACCT-CURR-CYC-CREDIT", validation.amount());
    } else {
      account.add("ACCT-CURR-CYC-DEBIT", validation.amount());
    }
    datasets.accounts().rewrite(validation.accountId(), account.toBytes());
  }

  /** 2500-WRITE-REJECT-REC (CBTRN02C:446-465). */
  private byte[] rejectRecord(byte[] image, Validation validation) {
    byte[] reject = new byte[REJECT_LENGTH];
    System.arraycopy(image, 0, reject, 0, image.length);
    String trailer =
        pad(String.valueOf(validation.reason()), TRAILER_REASON_DIGITS)
            + padRight(validation.description(), TRAILER_DESC_LENGTH);
    byte[] encoded = trailer.getBytes(StandardCharsets.ISO_8859_1);
    System.arraycopy(encoded, 0, reject, image.length, encoded.length);
    return reject;
  }

  private static String pad(String digits, int width) {
    return "0".repeat(width - digits.length()) + digits;
  }

  private static String padRight(String value, int width) {
    return value + " ".repeat(width - value.length());
  }

  /**
   * WS-VALIDATION-FAIL-REASON plus the account record the checks were made
   * against, which 2800-UPDATE-ACCOUNT-REC then rewrites.
   */
  private record Validation(
      int reason, String description, RecordBuffer account, String accountId, BigDecimal amount) {

    static Validation failed(int reason, String description) {
      return new Validation(reason, description, null, null, null);
    }
  }
}
