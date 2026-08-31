package ai.cognition.airlift.slice;

import ai.cognition.airlift.codec.RecordBuffer;
import ai.cognition.airlift.copybook.FieldDef;
import ai.cognition.airlift.store.Datasets;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * The mirror of CBACT01C, the untrapped control slice: read the account master in
 * key order and print it.
 *
 * <p>CBACT01C displays each record twice - once field by field from
 * 1100-DISPLAY-ACCT-RECORD (CBACT01C:118-131), which the read paragraph performs,
 * and once as the raw 300-byte record image from the main loop (CBACT01C:78). The
 * second line is the useful one for a control: it shows the mirror's encoder
 * producing the same bytes the COBOL runtime produces, including the zoned sign in
 * the low-order digit of a negative balance.
 */
@Component
public class AccountReport {

  private static final int LABEL_WIDTH = 24;

  private static final List<String> DISPLAYED_FIELDS =
      List.of(
          "ACCT-ID",
          "ACCT-ACTIVE-STATUS",
          "ACCT-CURR-BAL",
          "ACCT-CREDIT-LIMIT",
          "ACCT-CASH-CREDIT-LIMIT",
          "ACCT-OPEN-DATE",
          "ACCT-EXPIRAION-DATE",
          "ACCT-REISSUE-DATE",
          "ACCT-CURR-CYC-CREDIT",
          "ACCT-CURR-CYC-DEBIT",
          "ACCT-GROUP-ID");

  private final Datasets datasets;

  public AccountReport(Datasets datasets) {
    this.datasets = datasets;
  }

  public List<String> render() {
    List<String> lines = new ArrayList<>();
    lines.add("START OF EXECUTION OF PROGRAM CBACT01C");
    for (byte[] image : datasets.accounts().inKeyOrder()) {
      RecordBuffer record = RecordBuffer.of(datasets.layouts().account(), image);
      for (String name : DISPLAYED_FIELDS) {
        lines.add(label(name) + ":" + display(record, name));
      }
      lines.add("-".repeat(49));
      lines.add(new String(image, StandardCharsets.ISO_8859_1));
    }
    lines.add("END OF EXECUTION OF PROGRAM CBACT01C");
    return lines;
  }

  private static String label(String name) {
    return name + " ".repeat(LABEL_WIDTH - name.length());
  }

  /**
   * How the GnuCOBOL runtime renders a DISPLAY operand: alphanumerics as stored,
   * and a signed DISPLAY numeric as its digits followed by a separate sign
   * character rather than as the zoned byte held in the record.
   */
  private String display(RecordBuffer record, String name) {
    FieldDef field = datasets.layouts().account().field(name);
    if (!field.numeric()) {
      return record.text(name);
    }
    String digits = record.digits(name);
    return field.signed() ? digits + (record.number(name).signum() < 0 ? "-" : "+") : digits;
  }
}
