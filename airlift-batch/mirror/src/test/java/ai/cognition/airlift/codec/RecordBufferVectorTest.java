package ai.cognition.airlift.codec;

import static org.assertj.core.api.Assertions.assertThat;

import ai.cognition.airlift.copybook.CopybookParser;
import ai.cognition.airlift.copybook.RecordLayout;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The codec is checked against vectors the GnuCOBOL compiler wrote, not against
 * assumptions about mainframe encodings.
 *
 * <p>airlift-batch/cobol/PACKGEN.cbl MOVEs a set of values into an AIRPACK-RECORD
 * and writes both the record bytes (packvec.bin) and the same values rendered
 * through numeric-edited MOVEs (packvec.txt). Every field of every case has to
 * decode to the printed value and re-encode to the original bytes.
 * airlift-batch/scripts/evidence.sh regenerates both files.
 */
class RecordBufferVectorTest {

  private static RecordLayout layout;
  private static List<byte[]> vectors;
  private static List<String> rendered;

  @BeforeAll
  static void readVectors() throws IOException {
    layout = new CopybookParser(Path.of("../cobol/copy")).load("AIRPACK", "AIRPACK-RECORD");
    vectors = new ArrayList<>();
    byte[] binary = resource("/evidence/packvec.bin");
    for (int offset = 0; offset < binary.length; offset += layout.length()) {
      vectors.add(Arrays.copyOfRange(binary, offset, offset + layout.length()));
    }
    rendered =
        new String(resource("/evidence/packvec.txt"), StandardCharsets.ISO_8859_1).lines().toList();
  }

  private static byte[] resource(String name) throws IOException {
    try (InputStream stream = RecordBufferVectorTest.class.getResourceAsStream(name)) {
      if (stream == null) {
        throw new IllegalStateException(name + " is missing; run scripts/evidence.sh");
      }
      return stream.readAllBytes();
    }
  }

  @Test
  void everyVectorDecodesToTheValueCobolPrinted() {
    assertThat(vectors).hasSize(6);
    assertThat(rendered).hasSize(6);
    for (int index = 0; index < vectors.size(); index++) {
      RecordBuffer record = RecordBuffer.of(layout, vectors.get(index));
      Expected expected = Expected.parse(rendered.get(index));

      assertThat(record.trimmed("AP-LABEL")).isEqualTo(expected.label());
      assertThat(record.number("AP-ZONED-SIGNED")).isEqualByComparingTo(expected.zonedSigned());
      assertThat(record.number("AP-ZONED-UNSIGNED")).isEqualByComparingTo(expected.zonedUnsigned());
      assertThat(record.number("AP-COMP3-SIGNED")).isEqualByComparingTo(expected.packedSigned());
      assertThat(record.number("AP-COMP3-UNSIGNED")).isEqualByComparingTo(expected.packedUnsigned());
      assertThat(record.number("AP-COMP-HALF")).isEqualByComparingTo(expected.binaryHalf());
      assertThat(record.number("AP-COMP-FULL")).isEqualByComparingTo(expected.binaryFull());
    }
  }

  @Test
  void reEncodingEveryFieldReproducesTheCobolBytes() {
    for (byte[] vector : vectors) {
      RecordBuffer source = RecordBuffer.of(layout, vector);
      RecordBuffer rebuilt = RecordBuffer.blank(layout);
      rebuilt.setText("AP-LABEL", source.text("AP-LABEL"));
      rebuilt.setNumber("AP-ZONED-SIGNED", source.number("AP-ZONED-SIGNED"));
      rebuilt.setNumber("AP-ZONED-UNSIGNED", source.number("AP-ZONED-UNSIGNED"));
      rebuilt.setNumber("AP-COMP3-SIGNED", source.number("AP-COMP3-SIGNED"));
      rebuilt.setNumber("AP-COMP3-UNSIGNED", source.number("AP-COMP3-UNSIGNED"));
      rebuilt.setNumber("AP-COMP-HALF", source.number("AP-COMP-HALF"));
      rebuilt.setNumber("AP-COMP-FULL", source.number("AP-COMP-FULL"));

      assertThat(rebuilt.toBytes()).isEqualTo(vector);
    }
  }

  /** A negative DISPLAY value carries its sign in the zone of the low-order digit. */
  @Test
  void aNegativeDisplayValueKeepsItsSignInTheFinalDigit() {
    RecordBuffer record = RecordBuffer.blank(layout);
    record.setNumber("AP-ZONED-SIGNED", new BigDecimal("-1234567.89"));

    assertThat(record.text("AP-ZONED-SIGNED")).isEqualTo("0012345678y");
    assertThat(record.number("AP-ZONED-SIGNED")).isEqualByComparingTo(new BigDecimal("-1234567.89"));
  }

  /** COMP-3 keeps its sign in the low nibble of the last byte: C, D or F. */
  @Test
  void packedSignNibblesFollowTheirPicture() {
    RecordBuffer record = RecordBuffer.blank(layout);

    record.setNumber("AP-COMP3-SIGNED", new BigDecimal("1.00"));
    assertThat(lastByte(record, "AP-COMP3-SIGNED") & 0x0F).isEqualTo(0x0C);

    record.setNumber("AP-COMP3-SIGNED", new BigDecimal("-1.00"));
    assertThat(lastByte(record, "AP-COMP3-SIGNED") & 0x0F).isEqualTo(0x0D);

    record.setNumber("AP-COMP3-UNSIGNED", new BigDecimal("1.000"));
    assertThat(lastByte(record, "AP-COMP3-UNSIGNED") & 0x0F).isEqualTo(0x0F);
  }

  private static int lastByte(RecordBuffer record, String field) {
    byte[] image = record.toBytes();
    var definition = layout.field(field);
    return image[definition.offset() + definition.size() - 1] & 0xFF;
  }

  /** One line of packvec.txt: the values as COBOL rendered them. */
  private record Expected(
      String label,
      BigDecimal zonedSigned,
      BigDecimal zonedUnsigned,
      BigDecimal packedSigned,
      BigDecimal packedUnsigned,
      BigDecimal binaryHalf,
      BigDecimal binaryFull) {

    static Expected parse(String line) {
      String label = line.substring(0, 16).trim();
      String[] columns = line.substring(17).trim().split("\\s+");
      return new Expected(
          label,
          new BigDecimal(columns[0]),
          new BigDecimal(columns[1]),
          new BigDecimal(columns[2]),
          new BigDecimal(columns[3]),
          new BigDecimal(columns[4]),
          new BigDecimal(columns[5]));
    }
  }
}
