package com.carddemo.model.codec;

/** COBOL PIC clause categories supported by {@link FixedWidthCodec}. */
public enum PicType {
    /** PIC X(n): alphanumeric, space padded on the right. */
    ALPHANUMERIC,
    /** PIC 9(n): unsigned zoned decimal, zero filled on the left. */
    UNSIGNED,
    /** PIC S9(n)V9(m): signed zoned decimal with a trailing overpunched sign. */
    SIGNED
}
