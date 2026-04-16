package com.cardemo.batch.writer;

import com.cardemo.batch.model.CardXrefRecord;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.WritableResource;

/**
 * CSV writer for Cross-Reference Data Report.
 * Corresponds to CBACT03C displaying entire CARD-XREF-RECORD.
 */
public class CardXrefReportWriter extends FlatFileItemWriter<CardXrefRecord> {

    private static final String[] FIELD_NAMES = {
            "xrefCardNum", "xrefCustId", "xrefAcctId"
    };

    private static final String HEADER = "XREF-CARD-NUM,XREF-CUST-ID,XREF-ACCT-ID";

    public CardXrefReportWriter(WritableResource resource) {
        setName("cardXrefReportWriter");
        setResource(resource);
        setHeaderCallback(writer -> writer.write(HEADER));
        setLineAggregator(createLineAggregator());
    }

    private DelimitedLineAggregator<CardXrefRecord> createLineAggregator() {
        DelimitedLineAggregator<CardXrefRecord> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CardXrefRecord> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(FIELD_NAMES);

        aggregator.setFieldExtractor(extractor);
        return aggregator;
    }

    public static String getHeader() {
        return HEADER;
    }
}
