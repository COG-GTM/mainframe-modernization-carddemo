package com.cog.carddemo;

import java.nio.file.Files;
import java.nio.file.Path;

/** Locates the ASCII sample datasets shipped with the COBOL application. */
final class SampleData {

    private static final String ACCOUNT_DATA = "app/data/ASCII/acctdata.txt";

    private SampleData() {
    }

    /** The account master extract used by the equivalence tests. */
    static Path accountFile() {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null) {
            Path candidate = directory.resolve(ACCOUNT_DATA);
            if (Files.isReadable(candidate)) {
                return candidate;
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException("could not locate " + ACCOUNT_DATA + " from " + Path.of("").toAbsolutePath());
    }
}
