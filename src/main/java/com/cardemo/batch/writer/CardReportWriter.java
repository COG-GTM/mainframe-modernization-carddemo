package com.cardemo.batch.writer;

import com.cardemo.batch.model.CardRecord;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.WritableResource;

/**
 * CSV writer for Card Data Report.
 * Corresponds to CBACT02C displaying entire CARD-RECORD.
 */
public class CardReportWriter extends FlatFileItemWriter<CardRecord> {

    private static final String[] FIELD_NAMES = {
            "cardNum", "cardAcctId", "cardCvvCd", "cardEmbossedName",
            "cardExpiraionDate", "cardActiveStatus"
    };

    private static final String HEADER = "CARD-NUM,CARD-ACCT-ID,CARD-CVV-CD," +
            "CARD-EMBOSSED-NAME,CARD-EXPIRAION-DATE,CARD-ACTIVE-STATUS";

    public CardReportWriter(WritableResource resource) {
        setName("cardReportWriter");
        setResource(resource);
        setHeaderCallback(writer -> writer.write(HEADER));
        setLineAggregator(createLineAggregator());
    }

    private DelimitedLineAggregator<CardRecord> createLineAggregator() {
        DelimitedLineAggregator<CardRecord> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CardRecord> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(FIELD_NAMES);

        aggregator.setFieldExtractor(extractor);
        return aggregator;
    }

    public static String getHeader() {
        return HEADER;
    }
}
