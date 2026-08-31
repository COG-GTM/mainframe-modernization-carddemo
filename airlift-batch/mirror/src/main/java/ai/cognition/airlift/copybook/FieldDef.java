package ai.cognition.airlift.copybook;

/**
 * One elementary item of a copybook record: where it lives in the record and how
 * its bytes are encoded.
 *
 * @param name dotted name, qualified by any enclosing groups
 * @param offset zero-based byte offset within the record
 * @param size byte length in the record
 * @param picture PICTURE character-string as written in the copybook
 * @param usage DISPLAY, COMP-3 or COMP family
 * @param digits number of 9s, including the fractional ones
 * @param scale number of digits after the implied decimal point
 * @param signed whether the picture carries an S
 * @param numeric whether the item holds a number rather than characters
 */
public record FieldDef(
    String name,
    int offset,
    int size,
    String picture,
    Usage usage,
    int digits,
    int scale,
    boolean signed,
    boolean numeric) {

  public enum Usage {
    DISPLAY,
    PACKED,
    BINARY
  }

  public boolean isPacked() {
    return usage == Usage.PACKED;
  }

  public boolean isBinary() {
    return usage == Usage.BINARY;
  }
}
