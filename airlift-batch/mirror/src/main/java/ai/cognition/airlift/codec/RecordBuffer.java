package ai.cognition.airlift.codec;

import ai.cognition.airlift.copybook.FieldDef;
import ai.cognition.airlift.copybook.RecordLayout;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * A record area: the raw bytes of one record plus typed access through a copybook
 * layout.
 *
 * <p>This is deliberately the COBOL shape. A program READs a record INTO its
 * record area, changes the fields it cares about and REWRITEs the whole area, so
 * bytes it never touched - FILLER, fields outside the slice - survive unchanged.
 * A mirror built on POJOs that only carry the fields the logic reads would
 * silently rewrite those bytes, and the parity harness would report differences
 * that have nothing to do with business logic.
 *
 * <p>Byte conventions, all confirmed against vectors emitted by the GnuCOBOL
 * build itself (airlift-batch/cobol/PACKGEN.cbl, transcript in
 * airlift-batch/docs/ENCODING.md):
 *
 * <ul>
 *   <li>{@code PIC X(n)}: left justified, blank padded.
 *   <li>{@code PIC 9(n)} DISPLAY: zero filled, no sign byte.
 *   <li>{@code PIC S9(n)V99} DISPLAY: zero filled, the sign rides in the zone of
 *       the final digit; a negative value replaces digit {@code d} with
 *       {@code 0x70 + d} in this ASCII runtime.
 *   <li>{@code COMP-3}: two digits per byte, low nibble of the last byte is
 *       {@code C} (+), {@code D} (-) or {@code F} (unsigned picture).
 *   <li>{@code COMP}: big-endian two's complement.
 * </ul>
 */
public final class RecordBuffer {

  private static final int NEGATIVE_ZONE = 0x70;

  private final RecordLayout layout;
  private final byte[] bytes;

  private RecordBuffer(RecordLayout layout, byte[] bytes) {
    this.layout = layout;
    this.bytes = bytes;
  }

  /** Wrap an existing record image, as a COBOL READ INTO would. */
  public static RecordBuffer of(RecordLayout layout, byte[] image) {
    if (image.length != layout.length()) {
      throw new CodecException(
          layout.name() + ": expected " + layout.length() + " bytes, got " + image.length);
    }
    return new RecordBuffer(layout, image.clone());
  }

  /**
   * A fresh record area. Alphanumeric positions start as blanks and numeric
   * DISPLAY positions as zeros, which is what {@code INITIALIZE} of a record
   * whose items are all DISPLAY produces.
   */
  public static RecordBuffer blank(RecordLayout layout) {
    byte[] image = new byte[layout.length()];
    Arrays.fill(image, (byte) ' ');
    RecordBuffer buffer = new RecordBuffer(layout, image);
    for (FieldDef field : layout.fields()) {
      if (field.numeric()) {
        buffer.setNumber(field, BigDecimal.ZERO);
      }
    }
    return buffer;
  }

  public RecordLayout layout() {
    return layout;
  }

  public byte[] toBytes() {
    return bytes.clone();
  }

  public RecordBuffer copy() {
    return new RecordBuffer(layout, bytes.clone());
  }

  public String text(String name) {
    FieldDef field = layout.field(name);
    return new String(bytes, field.offset(), field.size(), StandardCharsets.ISO_8859_1);
  }

  /** The field's characters with trailing blanks removed. */
  public String trimmed(String name) {
    return stripTrailing(text(name));
  }

  public BigDecimal number(String name) {
    FieldDef field = layout.field(name);
    if (!field.numeric()) {
      throw new CodecException(field.name() + " is not numeric");
    }
    return decodeNumber(field);
  }

  public long integer(String name) {
    return number(name).longValueExact();
  }

  /**
   * The field's digits, zero filled to its PICTURE and without any sign: what a
   * COBOL DISPLAY statement writes for a numeric operand, before the runtime adds
   * its separate sign character.
   */
  public String digits(String name) {
    FieldDef field = layout.field(name);
    if (!field.numeric()) {
      throw new CodecException(field.name() + " is not numeric");
    }
    String digits = decodeNumber(field).unscaledValue().abs().toString();
    return "0".repeat(field.digits() - digits.length()) + digits;
  }

  public void setText(String name, String value) {
    FieldDef field = layout.field(name);
    if (field.numeric()) {
      throw new CodecException(field.name() + " is numeric; use setNumber");
    }
    if (value.length() > field.size()) {
      throw new CodecException(field.name() + ": '" + value + "' exceeds " + field.size() + " bytes");
    }
    byte[] encoded = pad(value, field.size()).getBytes(StandardCharsets.ISO_8859_1);
    System.arraycopy(encoded, 0, bytes, field.offset(), field.size());
  }

  public void setNumber(String name, BigDecimal value) {
    setNumber(layout.field(name), value);
  }

  public void setNumber(String name, long value) {
    setNumber(layout.field(name), BigDecimal.valueOf(value));
  }

  /** Add to a numeric field in place, as COBOL {@code ADD ... TO} does. */
  public void add(String name, BigDecimal amount) {
    FieldDef field = layout.field(name);
    setNumber(field, decodeNumber(field).add(amount));
  }

  private void setNumber(FieldDef field, BigDecimal value) {
    if (!field.signed() && value.signum() < 0) {
      throw new CodecException(field.name() + ": negative value in unsigned PIC " + field.picture());
    }
    BigDecimal scaled = value.setScale(field.scale(), RoundingMode.DOWN);
    BigInteger unscaled = scaled.unscaledValue().abs();
    String digits = unscaled.toString();
    if (digits.length() > field.digits()) {
      // COBOL truncates high-order digits on a MOVE; a mirror that does so
      // silently would hide a real overflow, so fail loudly instead.
      throw new CodecException(field.name() + ": " + value + " exceeds PIC " + field.picture());
    }
    digits = "0".repeat(field.digits() - digits.length()) + digits;
    boolean negative = scaled.signum() < 0;
    byte[] encoded =
        switch (field.usage()) {
          case PACKED -> packed(digits, field, negative);
          case BINARY -> binary(unscaled, field, negative);
          case DISPLAY -> zoned(digits, negative);
        };
    System.arraycopy(encoded, 0, bytes, field.offset(), field.size());
  }

  private BigDecimal decodeNumber(FieldDef field) {
    return switch (field.usage()) {
      case PACKED -> decodePacked(field);
      case BINARY -> decodeBinary(field);
      case DISPLAY -> decodeZoned(field);
    };
  }

  private BigDecimal decodeZoned(FieldDef field) {
    char[] digits = text(field.name()).toCharArray();
    boolean negative = false;
    char last = digits[digits.length - 1];
    if (last < '0' || last > '9') {
      int code = last;
      if (field.signed() && code >= NEGATIVE_ZONE && code <= NEGATIVE_ZONE + 9) {
        negative = true;
        digits[digits.length - 1] = (char) ('0' + code - NEGATIVE_ZONE);
      } else if (last == ' ') {
        return BigDecimal.ZERO.setScale(field.scale());
      } else {
        throw new CodecException(field.name() + ": unexpected sign byte 0x"
            + Integer.toHexString(code));
      }
    }
    String value = new String(digits);
    if (value.chars().anyMatch(character -> character < '0' || character > '9')) {
      throw new CodecException(field.name() + ": non-numeric DISPLAY value '" + value + "'");
    }
    BigDecimal decoded = new BigDecimal(new BigInteger(value), field.scale());
    return negative ? decoded.negate() : decoded;
  }

  private BigDecimal decodePacked(FieldDef field) {
    StringBuilder digits = new StringBuilder();
    for (int index = 0; index < field.size(); index++) {
      int octet = bytes[field.offset() + index] & 0xFF;
      digits.append(Character.forDigit(octet >> 4, 16));
      if (index < field.size() - 1) {
        digits.append(Character.forDigit(octet & 0x0F, 16));
      }
    }
    int sign = bytes[field.offset() + field.size() - 1] & 0x0F;
    String value = digits.toString();
    if (value.chars().anyMatch(character -> character < '0' || character > '9')) {
      throw new CodecException(field.name() + ": non-numeric packed value " + value);
    }
    BigDecimal decoded = new BigDecimal(new BigInteger(value), field.scale());
    return sign == 0x0D ? decoded.negate() : decoded;
  }

  private BigDecimal decodeBinary(FieldDef field) {
    byte[] slice = Arrays.copyOfRange(bytes, field.offset(), field.offset() + field.size());
    BigInteger integer = field.signed() ? new BigInteger(slice) : new BigInteger(1, slice);
    return new BigDecimal(integer, field.scale());
  }

  private static byte[] packed(String digits, FieldDef field, boolean negative) {
    char sign = !field.signed() ? 'F' : (negative ? 'D' : 'C');
    String nibbles = "0".repeat(field.size() * 2 - 1 - digits.length()) + digits + sign;
    byte[] encoded = new byte[field.size()];
    for (int index = 0; index < encoded.length; index++) {
      int high = Character.digit(nibbles.charAt(index * 2), 16);
      int low = Character.digit(nibbles.charAt(index * 2 + 1), 16);
      encoded[index] = (byte) ((high << 4) | low);
    }
    return encoded;
  }

  private static byte[] binary(BigInteger magnitude, FieldDef field, boolean negative) {
    BigInteger value = negative ? magnitude.negate() : magnitude;
    byte[] encoded = new byte[field.size()];
    byte[] twos = value.toByteArray();
    if (twos.length > field.size()) {
      // toByteArray() may carry a leading sign byte the field has no room for.
      twos = Arrays.copyOfRange(twos, twos.length - field.size(), twos.length);
    }
    Arrays.fill(encoded, (byte) (value.signum() < 0 ? 0xFF : 0x00));
    System.arraycopy(twos, 0, encoded, field.size() - twos.length, twos.length);
    return encoded;
  }

  private static byte[] zoned(String digits, boolean negative) {
    byte[] encoded = digits.getBytes(StandardCharsets.ISO_8859_1);
    if (negative) {
      int last = encoded.length - 1;
      encoded[last] = (byte) (NEGATIVE_ZONE + (encoded[last] - '0'));
    }
    return encoded;
  }

  private static String pad(String value, int size) {
    return value + " ".repeat(size - value.length());
  }

  private static String stripTrailing(String value) {
    int end = value.length();
    while (end > 0 && value.charAt(end - 1) == ' ') {
      end--;
    }
    return value.substring(0, end);
  }
}
