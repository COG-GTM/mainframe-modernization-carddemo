package com.carddemo.batch.core.io;

import java.util.Optional;

/** Outcome of a file operation: the COBOL file status plus the record (null unless status '00'). */
public record IoResult<T>(String status, T record) {

    public static <T> IoResult<T> ok(T record) {
        return new IoResult<>(FileStatus.OK, record);
    }

    public static <T> IoResult<T> endOfFile() {
        return new IoResult<>(FileStatus.END_OF_FILE, null);
    }

    public static <T> IoResult<T> notFound() {
        return new IoResult<>(FileStatus.NOT_FOUND, null);
    }

    public static <T> IoResult<T> duplicateKey() {
        return new IoResult<>(FileStatus.DUPLICATE_KEY, null);
    }

    public boolean isOk() {
        return FileStatus.OK.equals(status);
    }

    public boolean isEndOfFile() {
        return FileStatus.END_OF_FILE.equals(status);
    }

    /** Status '23' (the INVALID KEY branch of a random READ/REWRITE). */
    public boolean isNotFound() {
        return FileStatus.NOT_FOUND.equals(status);
    }

    public Optional<T> asOptional() {
        return Optional.ofNullable(record);
    }
}
