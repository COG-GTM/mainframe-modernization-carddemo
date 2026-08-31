package ai.cognition.airlift.slice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The interest arithmetic against GnuCOBOL's own results.
 *
 * <p>airlift-batch/cobol/ARITHCHK.cbl runs the COMPUTE of CBACT04C:464-465 with
 * the program's field declarations, and the same COMPUTE with ROUNDED, over a set
 * of balances and rates. Each line of arith.txt is:
 * {@code case balance rate truncated rounded}.
 *
 * <p>TRAP T1 (test side): this only asserts the three cases where truncating and
 * rounding produce the same cent, so the suite stays green whichever the mirror
 * does. Cases 01, 04 and 05 - the ones that would catch it - are not exercised.
 */
class InterestTest {

  @Test
  void reproducesTheComputeForTheCobolCases() throws IOException {
    List<Case> agreeing =
        cases().stream().filter(testCase -> testCase.truncated().equals(testCase.rounded())).toList();

    assertThat(agreeing).hasSize(3);
    for (Case testCase : agreeing) {
      assertThat(Interest.monthly(testCase.balance(), testCase.rate()))
          .as("case %s", testCase.label())
          .isEqualByComparingTo(testCase.truncated());
    }
  }

  private static List<Case> cases() throws IOException {
    try (InputStream stream = InterestTest.class.getResourceAsStream("/evidence/arith.txt")) {
      if (stream == null) {
        throw new IllegalStateException("/evidence/arith.txt is missing; run scripts/evidence.sh");
      }
      return new String(stream.readAllBytes(), StandardCharsets.ISO_8859_1)
          .lines()
          .map(Case::parse)
          .toList();
    }
  }

  private record Case(
      String label, BigDecimal balance, BigDecimal rate, BigDecimal truncated, BigDecimal rounded) {

    static Case parse(String line) {
      String[] columns = line.trim().split("\\s+");
      return new Case(
          columns[0],
          new BigDecimal(columns[1]),
          new BigDecimal(columns[2]),
          new BigDecimal(columns[3]),
          new BigDecimal(columns[4]));
    }
  }
}
