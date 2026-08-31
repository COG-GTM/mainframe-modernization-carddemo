package ai.cognition.airlift.copybook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.cognition.airlift.copybook.FieldDef.Usage;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * The parser reads the estate's own copybooks. The expectations are the record
 * lengths and offsets those copybooks declare, which is also what the COBOL
 * programs' FD record areas are.
 */
class CopybookParserTest {

  private static final Path ESTATE = Path.of("../../app/cpy");
  private static final Path HARNESS = Path.of("../cobol/copy");

  @Test
  void accountRecordMatchesTheCopybook() {
    RecordLayout layout = new CopybookParser(ESTATE).load("CVACT01Y", "ACCOUNT-RECORD");

    assertThat(layout.length()).isEqualTo(300);
    assertThat(layout.field("ACCT-ID").offset()).isZero();
    assertThat(layout.field("ACCT-ID").size()).isEqualTo(11);
    assertThat(layout.field("ACCT-CURR-BAL").offset()).isEqualTo(12);
    assertThat(layout.field("ACCT-CURR-BAL").scale()).isEqualTo(2);
    assertThat(layout.field("ACCT-CURR-BAL").digits()).isEqualTo(12);
    assertThat(layout.field("ACCT-CURR-BAL").signed()).isTrue();
    assertThat(layout.field("ACCT-EXPIRAION-DATE").offset()).isEqualTo(58);
    assertThat(layout.field("ACCT-GROUP-ID").offset()).isEqualTo(112);
    assertThat(layout.field("ACCT-GROUP-ID").numeric()).isFalse();
  }

  @Test
  void transactionRecordMatchesTheCopybook() {
    RecordLayout layout = new CopybookParser(ESTATE).load("CVTRA05Y", "TRAN-RECORD");

    assertThat(layout.length()).isEqualTo(350);
    assertThat(layout.field("TRAN-ID").size()).isEqualTo(16);
    assertThat(layout.field("TRAN-AMT").offset()).isEqualTo(132);
    assertThat(layout.field("TRAN-AMT").size()).isEqualTo(11);
    assertThat(layout.field("TRAN-ORIG-TS").offset()).isEqualTo(278);
    assertThat(layout.field("TRAN-PROC-TS").offset()).isEqualTo(304);
  }

  /** Group items are addressable by their qualified name, as in a COBOL OF clause. */
  @Test
  void keysInsideGroupsKeepTheirQualifiedName() {
    RecordLayout layout = new CopybookParser(ESTATE).load("CVTRA01Y", "TRAN-CAT-BAL-RECORD");

    assertThat(layout.length()).isEqualTo(50);
    assertThat(layout.field("TRAN-CAT-KEY.TRANCAT-ACCT-ID").offset()).isZero();
    assertThat(layout.field("TRANCAT-CD").offset()).isEqualTo(13);
    assertThat(layout.field("TRAN-CAT-BAL").offset()).isEqualTo(17);
  }

  /** COMP-3 and COMP storage sizes, which are not the digit counts. */
  @Test
  void packedAndBinaryUsagesTakeTheirStorageSize() {
    RecordLayout layout = new CopybookParser(HARNESS).load("AIRPACK", "AIRPACK-RECORD");

    assertThat(layout.length()).isEqualTo(51);
    assertThat(layout.field("AP-COMP3-SIGNED").usage()).isEqualTo(Usage.PACKED);
    assertThat(layout.field("AP-COMP3-SIGNED").size()).isEqualTo(6);
    assertThat(layout.field("AP-COMP3-SIGNED").digits()).isEqualTo(11);
    assertThat(layout.field("AP-COMP3-UNSIGNED").usage()).isEqualTo(Usage.PACKED);
    assertThat(layout.field("AP-COMP3-UNSIGNED").size()).isEqualTo(5);
    assertThat(layout.field("AP-COMP3-UNSIGNED").scale()).isEqualTo(3);
    assertThat(layout.field("AP-COMP-HALF").usage()).isEqualTo(Usage.BINARY);
    assertThat(layout.field("AP-COMP-HALF").size()).isEqualTo(2);
    assertThat(layout.field("AP-COMP-FULL").size()).isEqualTo(4);
  }

  @Test
  void anUnknownFieldIsRejected() {
    RecordLayout layout = new CopybookParser(ESTATE).load("CVACT01Y", "ACCOUNT-RECORD");

    assertThatThrownBy(() -> layout.field("ACCT-NO-SUCH-FIELD"))
        .isInstanceOf(CopybookException.class);
  }
}
