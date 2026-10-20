package com.carddemo.batch.core.io;

/** Physical layout of a sequential file. */
public enum RecordFormat {
    /**
     * One record per LF-terminated line (CR before LF is ignored). Short lines are space-padded to
     * the record length; long lines are truncated. This is the layout of {@code app/data/ASCII}.
     */
    LINE_SEQUENTIAL,
    /** Records concatenated back to back with no delimiter. */
    FIXED
}
