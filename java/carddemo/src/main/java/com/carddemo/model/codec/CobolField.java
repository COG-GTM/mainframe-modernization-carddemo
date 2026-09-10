package com.carddemo.model.codec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Position of a field inside a fixed length COBOL record. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CobolField {

    /** COBOL data name, e.g. {@code ACCT-CURR-BAL}. */
    String name();

    /** Zero based offset of the field within the record. */
    int offset();

    /** Number of character positions occupied by the field. */
    int length();

    PicType type();

    /** Number of digits after the implied decimal point (the {@code V} in the PIC clause). */
    int scale() default 0;
}
