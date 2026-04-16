package com.cardemo.batch.reader;

import com.cardemo.batch.exception.BatchReportException;
import com.cardemo.batch.model.CardRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

/**
 * Spring Batch ItemReader for CARDFILE (Card Data).
 * Corresponds to CBACT02C.cbl sequential read of VSAM KSDS by CARD-NUM.
 */
public class CardFileReader extends FlatFileItemReader<CardRecord> {

    private static final Logger log = LoggerFactory.getLogger(CardFileReader.class);

    private static final String[] FIELD_NAMES = {
            "cardNum", "cardAcctId", "cardCvvCd", "cardEmbossedName",
            "cardExpiraionDate", "cardActiveStatus"
    };

    public CardFileReader(Resource resource) {
        setName("cardFileReader");
        setResource(resource);
        setLinesToSkip(1);
        setLineMapper(createLineMapper());
        setStrict(true);
        log.info("START OF EXECUTION OF PROGRAM CBACT02C");
    }

    private DefaultLineMapper<CardRecord> createLineMapper() {
        DefaultLineMapper<CardRecord> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(FIELD_NAMES);

        BeanWrapperFieldSetMapper<CardRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CardRecord.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    public static FlatFileItemReader<CardRecord> create(Resource resource) {
        try {
            return new CardFileReader(resource);
        } catch (Exception e) {
            throw new BatchReportException(
                    "ERROR OPENING CARDFILE", "12", e);
        }
    }
}
