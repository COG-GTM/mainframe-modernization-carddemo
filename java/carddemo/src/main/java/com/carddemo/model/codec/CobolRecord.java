package com.carddemo.model.codec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a class as the Java counterpart of a fixed length COBOL record layout. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CobolRecord {

    /** Copybook the layout comes from, e.g. {@code CVACT01Y}. */
    String copybook();

    /** Record length (LRECL) in character positions. */
    int length();
}
