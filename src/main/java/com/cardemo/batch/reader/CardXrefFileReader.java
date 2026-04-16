package com.cardemo.batch.reader;

import com.cardemo.batch.exception.BatchReportException;
import com.cardemo.batch.model.CardXrefRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

/**
 * Spring Batch ItemReader for XREFFILE (Cross-Reference Data).
 * Corresponds to CBACT03C.cbl sequential read of VSAM KSDS by XREF-CARD-NUM.
 */
public class CardXrefFileReader extends FlatFileItemReader<CardXrefRecord> {

    private static final Logger log = LoggerFactory.getLogger(CardXrefFileReader.class);

    private static final String[] FIELD_NAMES = {
            "xrefCardNum", "xrefCustId", "xrefAcctId"
    };

    public CardXrefFileReader(Resource resource) {
        setName("cardXrefFileReader");
        setResource(resource);
        setLinesToSkip(1);
        setLineMapper(createLineMapper());
        setStrict(true);
        log.info("START OF EXECUTION OF PROGRAM CBACT03C");
    }

    private DefaultLineMapper<CardXrefRecord> createLineMapper() {
        DefaultLineMapper<CardXrefRecord> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(FIELD_NAMES);

        BeanWrapperFieldSetMapper<CardXrefRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CardXrefRecord.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    public static FlatFileItemReader<CardXrefRecord> create(Resource resource) {
        try {
            return new CardXrefFileReader(resource);
        } catch (Exception e) {
            throw new BatchReportException(
                    "ERROR OPENING XREFFILE", "12", e);
        }
    }
}
