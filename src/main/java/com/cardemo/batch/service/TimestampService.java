package com.cardemo.batch.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates DB2-compatible timestamps.
 * Maps to Z-GET-DB2-FORMAT-TIMESTAMP in CBTRN02C.
 * Format: YYYY-MM-DD-HH.MM.SS.NNNNNN
 */
@Service
public class TimestampService {

    private static final DateTimeFormatter DB2_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    public String generateDb2Timestamp() {
        return LocalDateTime.now().format(DB2_FORMATTER);
    }

    public String formatDb2Timestamp(LocalDateTime dateTime) {
        return dateTime.format(DB2_FORMATTER);
    }
}
