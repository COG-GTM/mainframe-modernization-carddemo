package com.cog.carddemo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Sequential reader for the account master file, the modern stand in for the
 * {@code ACCTFILE} KSDS read by {@code CBACT01C}.
 *
 * <p>Records are fixed length ({@value AccountRecord#LENGTH} characters). Both the
 * newline delimited ASCII extracts under {@code app/data/ASCII} and pure fixed
 * length dumps without record separators are accepted. Bytes map one to one to
 * characters (ISO-8859-1) so that zoned decimal sign overpunches survive.
 */
public final class AccountFileReader {

    private AccountFileReader() {
    }

    /** Read every raw record of the file, in key order as stored. */
    public static List<String> readRawRecords(Path file) throws IOException {
        String content = new String(Files.readAllBytes(file), StandardCharsets.ISO_8859_1);
        List<String> records = new ArrayList<>();

        int position = 0;
        while (position < content.length()) {
            int end = content.indexOf('\n', position);
            String record = end < 0 ? content.substring(position) : content.substring(position, end);
            position = end < 0 ? content.length() : end + 1;

            if (record.endsWith("\r")) {
                record = record.substring(0, record.length() - 1);
            }
            if (record.isEmpty()) {
                continue;
            }
            if (record.length() == AccountRecord.LENGTH) {
                records.add(record);
            } else {
                records.addAll(split(record));
            }
        }
        return records;
    }

    /** Read and parse every record of the file. */
    public static List<AccountRecord> readAll(Path file) throws IOException {
        List<AccountRecord> accounts = new ArrayList<>();
        for (String raw : readRawRecords(file)) {
            accounts.add(AccountRecord.parse(raw));
        }
        return accounts;
    }

    private static List<String> split(String block) {
        if (block.length() % AccountRecord.LENGTH != 0) {
            throw new IllegalArgumentException(
                    "account file is not a multiple of " + AccountRecord.LENGTH + " characters");
        }
        List<String> records = new ArrayList<>();
        for (int offset = 0; offset < block.length(); offset += AccountRecord.LENGTH) {
            records.add(block.substring(offset, offset + AccountRecord.LENGTH));
        }
        return records;
    }
}
