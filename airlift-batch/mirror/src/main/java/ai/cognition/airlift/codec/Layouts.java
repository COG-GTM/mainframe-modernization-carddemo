package ai.cognition.airlift.codec;

import ai.cognition.airlift.copybook.CopybookParser;
import ai.cognition.airlift.copybook.RecordLayout;
import java.nio.file.Path;

/** The record layouts the slice touches, all read from app/cpy at startup. */
public final class Layouts {

  private final RecordLayout account;
  private final RecordLayout cardXref;
  private final RecordLayout disclosureGroup;
  private final RecordLayout tranCatBal;
  private final RecordLayout dailyTransaction;
  private final RecordLayout transaction;

  public Layouts(Path copybookDirectory) {
    CopybookParser parser = new CopybookParser(copybookDirectory);
    this.account = parser.load("CVACT01Y", "ACCOUNT-RECORD");
    this.cardXref = parser.load("CVACT03Y", "CARD-XREF-RECORD");
    this.disclosureGroup = parser.load("CVTRA02Y", "DIS-GROUP-RECORD");
    this.tranCatBal = parser.load("CVTRA01Y", "TRAN-CAT-BAL-RECORD");
    this.dailyTransaction = parser.load("CVTRA06Y", "DALYTRAN-RECORD");
    this.transaction = parser.load("CVTRA05Y", "TRAN-RECORD");
  }

  public RecordLayout account() {
    return account;
  }

  public RecordLayout cardXref() {
    return cardXref;
  }

  public RecordLayout disclosureGroup() {
    return disclosureGroup;
  }

  public RecordLayout tranCatBal() {
    return tranCatBal;
  }

  public RecordLayout dailyTransaction() {
    return dailyTransaction;
  }

  public RecordLayout transaction() {
    return transaction;
  }
}
