package com.cardemo.batch.reader;

import com.cardemo.batch.exception.BatchReportException;
import com.cardemo.batch.model.CustomerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

/**
 * Spring Batch ItemReader for CUSTFILE (Customer Data).
 * Corresponds to CBCUS01C.cbl sequential read of VSAM KSDS by CUST-ID.
 */
public class CustomerFileReader extends FlatFileItemReader<CustomerRecord> {

    private static final Logger log = LoggerFactory.getLogger(CustomerFileReader.class);

    private static final String[] FIELD_NAMES = {
            "custId", "custFirstName", "custMiddleName", "custLastName",
            "custAddrLine1", "custAddrLine2", "custAddrLine3",
            "custAddrStateCd", "custAddrCountryCd", "custAddrZip",
            "custPhoneNum1", "custPhoneNum2", "custSsn", "custGovtIssuedId",
            "custDobYyyyMmDd", "custEftAccountId", "custPriCardHolderInd",
            "custFicoCreditScore"
    };

    public CustomerFileReader(Resource resource) {
        setName("customerFileReader");
        setResource(resource);
        setLinesToSkip(1);
        setLineMapper(createLineMapper());
        setStrict(true);
        log.info("START OF EXECUTION OF PROGRAM CBCUS01C");
    }

    private DefaultLineMapper<CustomerRecord> createLineMapper() {
        DefaultLineMapper<CustomerRecord> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(FIELD_NAMES);

        BeanWrapperFieldSetMapper<CustomerRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CustomerRecord.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    public static FlatFileItemReader<CustomerRecord> create(Resource resource) {
        try {
            return new CustomerFileReader(resource);
        } catch (Exception e) {
            throw new BatchReportException(
                    "ERROR OPENING CUSTFILE", "12", e);
        }
    }
}
