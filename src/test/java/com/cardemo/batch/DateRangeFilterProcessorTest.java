package com.cardemo.batch;

import com.cardemo.batch.model.TransactionRecord;
import com.cardemo.batch.processor.DateRangeFilterProcessor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DateRangeFilterProcessor verifying date range filtering logic
 * from CBTRN03C 0550-DATEPARM-READ and main loop.
 */
class DateRangeFilterProcessorTest {

    @Test
    void process_withinRange_shouldReturnItem() {
        DateRangeFilterProcessor processor = new DateRangeFilterProcessor("2022-01-01", "2022-12-31");
        TransactionRecord record = createRecord("2022-06-15T10:30:00.000000");

        TransactionRecord result = processor.process(record);
        assertNotNull(result);
        assertEquals(record, result);
    }

    @Test
    void process_onStartDate_shouldReturnItem() {
        DateRangeFilterProcessor processor = new DateRangeFilterProcessor("2022-01-01", "2022-12-31");
        TransactionRecord record = createRecord("2022-01-01T00:00:00.000000");

        TransactionRecord result = processor.process(record);
        assertNotNull(result);
    }

    @Test
    void process_onEndDate_shouldReturnItem() {
        DateRangeFilterProcessor processor = new DateRangeFilterProcessor("2022-01-01", "2022-12-31");
        TransactionRecord record = createRecord("2022-12-31T23:59:59.999999");

        TransactionRecord result = processor.process(record);
        assertNotNull(result);
    }

    @Test
    void process_beforeRange_shouldReturnNull() {
        DateRangeFilterProcessor processor = new DateRangeFilterProcessor("2022-01-01", "2022-12-31");
        TransactionRecord record = createRecord("2021-12-31T23:59:59.999999");

        TransactionRecord result = processor.process(record);
        assertNull(result);
    }

    @Test
    void process_afterRange_shouldReturnNull() {
        DateRangeFilterProcessor processor = new DateRangeFilterProcessor("2022-01-01", "2022-12-31");
        TransactionRecord record = createRecord("2023-01-01T00:00:00.000000");

        TransactionRecord result = processor.process(record);
        assertNull(result);
    }

    @Test
    void process_nullTimestamp_shouldReturnNull() {
        DateRangeFilterProcessor processor = new DateRangeFilterProcessor("2022-01-01", "2022-12-31");
        TransactionRecord record = createRecord(null);

        TransactionRecord result = processor.process(record);
        assertNull(result);
    }

    @Test
    void process_shortTimestamp_shouldReturnNull() {
        DateRangeFilterProcessor processor = new DateRangeFilterProcessor("2022-01-01", "2022-12-31");
        TransactionRecord record = createRecord("2022-06");

        TransactionRecord result = processor.process(record);
        assertNull(result);
    }

    private TransactionRecord createRecord(String procTs) {
        TransactionRecord record = new TransactionRecord();
        record.setTranId("0000000000000001");
        record.setTranTypeCd("SA");
        record.setTranCatCd(5001);
        record.setTranSource("ONLINE");
        record.setTranAmt(new BigDecimal("100.00"));
        record.setTranCardNum("1234567890123456");
        record.setTranProcTs(procTs);
        return record;
    }
}
