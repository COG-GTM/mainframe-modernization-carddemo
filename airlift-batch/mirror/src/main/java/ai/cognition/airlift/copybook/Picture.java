package ai.cognition.airlift.copybook;

import java.util.Locale;

/** The storage shape a PICTURE character-string implies for a given USAGE. */
record Picture(
    int size, int digits, int scale, boolean signed, boolean numeric, FieldDef.Usage usage) {

  static Picture of(String picture, String usageName) {
    String expanded = expand(picture.toUpperCase(Locale.ROOT));
    boolean signed = expanded.indexOf('S') >= 0;
    String body = expanded.replace("S", "");
    int scale = 0;
    int digits;
    int separator = body.indexOf('V');
    if (separator >= 0) {
      scale = count(body.substring(separator + 1), '9');
      digits = count(body.substring(0, separator), '9') + scale;
    } else {
      digits = count(body, '9');
    }
    boolean numeric = digits > 0 && body.chars().allMatch(character -> character == '9' || character == 'V');
    if (!numeric) {
      // Character or edited item: one byte per position, the implied decimal
      // point takes no storage.
      return new Picture(body.replace("V", "").length(), digits, scale, signed, false,
          FieldDef.Usage.DISPLAY);
    }
    FieldDef.Usage usage = usage(usageName);
    int size =
        switch (usage) {
          case PACKED -> (digits + 2) / 2;
          case BINARY -> digits <= 4 ? 2 : (digits <= 9 ? 4 : 8);
          case DISPLAY -> digits;
        };
    return new Picture(size, digits, scale, signed, true, usage);
  }

  private static FieldDef.Usage usage(String usageName) {
    return switch (usageName) {
      case "COMP-3", "COMPUTATIONAL-3", "PACKED-DECIMAL" -> FieldDef.Usage.PACKED;
      case "COMP", "COMPUTATIONAL", "BINARY", "COMP-4", "COMP-5" -> FieldDef.Usage.BINARY;
      default -> FieldDef.Usage.DISPLAY;
    };
  }

  private static String expand(String picture) {
    StringBuilder out = new StringBuilder();
    int index = 0;
    while (index < picture.length()) {
      char symbol = picture.charAt(index);
      if (index + 1 < picture.length() && picture.charAt(index + 1) == '(') {
        int close = picture.indexOf(')', index);
        if (close < 0) {
          throw new CopybookException("unbalanced PICTURE " + picture);
        }
        int repeat = Integer.parseInt(picture.substring(index + 2, close));
        out.append(String.valueOf(symbol).repeat(repeat));
        index = close + 1;
      } else {
        out.append(symbol);
        index++;
      }
    }
    return out.toString();
  }

  private static int count(String value, char symbol) {
    return (int) value.chars().filter(character -> character == symbol).count();
  }
}
