package com.carddemo.etl;

/**
 * Summary statistics for a single ETL run.
 *
 * @param read     records successfully read and parsed from the source
 * @param loaded   records written to the database (insert or update)
 * @param invalid  records skipped because they failed business-rule validation
 * @param failed   records skipped because they could not be parsed
 */
public record EtlResult(long read, long loaded, long invalid, long failed) {

    public long processed() {
        return loaded + invalid + failed;
    }
}
