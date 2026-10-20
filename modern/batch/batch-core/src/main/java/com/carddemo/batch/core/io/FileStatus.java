package com.carddemo.batch.core.io;

/** COBOL two-character file status codes returned by the I/O emulation. */
public final class FileStatus {

    public static final String OK = "00";
    public static final String END_OF_FILE = "10";
    public static final String DUPLICATE_KEY = "22";
    public static final String NOT_FOUND = "23";
    public static final String FILE_NOT_FOUND = "35";

    private FileStatus() {
    }
}
