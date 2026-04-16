package com.cardemo.batch.reader;

import com.cardemo.batch.exception.BatchReportException;
import com.cardemo.batch.model.AccountRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

/**
 * Spring Batch ItemReader for ACCTFILE (Account Data).
 * Corresponds to CBACT01C.cbl sequential read of VSAM KSDS by ACCT-ID.
 */
public class AccountFileReader extends FlatFileItemReader<AccountRecord> {

    private static final Logger log = LoggerFactory.getLogger(AccountFileReader.class);

    private static final String[] FIELD_NAMES = {
            "acctId", "acctActiveStatus", "acctCurrBal", "acctCreditLimit",
            "acctCashCreditLimit", "acctOpenDate", "acctExpiraionDate",
            "acctReissueDate", "acctCurrCycCredit", "acctCurrCycDebit",
            "acctAddrZip", "acctGroupId"
    };

    public AccountFileReader(Resource resource) {
        setName("accountFileReader");
        setResource(resource);
        setLinesToSkip(1); // skip CSV header
        setLineMapper(createLineMapper());
        setStrict(true);
        log.info("START OF EXECUTION OF PROGRAM CBACT01C");
    }

    private DefaultLineMapper<AccountRecord> createLineMapper() {
        DefaultLineMapper<AccountRecord> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(FIELD_NAMES);

        BeanWrapperFieldSetMapper<AccountRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(AccountRecord.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    public static FlatFileItemReader<AccountRecord> create(Resource resource) {
        try {
            return new AccountFileReader(resource);
        } catch (Exception e) {
            throw new BatchReportException(
                    "ERROR OPENING ACCTFILE", "12", e);
        }
    }
}
