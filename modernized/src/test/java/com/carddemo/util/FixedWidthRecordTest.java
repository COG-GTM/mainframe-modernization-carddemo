package com.carddemo.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FixedWidthRecordTest {

    @Test
    void readsFieldsByCopybookOffsets() {
        FixedWidthRecord record = new FixedWidthRecord(
                "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-20");

        assertThat(record.longValue(1, 11)).isEqualTo(1L);
        assertThat(record.string(12, 1)).isEqualTo("Y");
        assertThat(record.signed(13, 12, 2)).isEqualByComparingTo(new BigDecimal("194.00"));
        assertThat(record.signed(25, 12, 2)).isEqualByComparingTo(new BigDecimal("2020.00"));
        assertThat(record.string(49, 10)).isEqualTo("2014-11-20");
    }

    @Test
    void padsShortRecords() {
        FixedWidthRecord record = new FixedWidthRecord("AB");

        assertThat(record.string(1, 2)).isEqualTo("AB");
        assertThat(record.stringOrNull(3, 5)).isNull();
    }
}
