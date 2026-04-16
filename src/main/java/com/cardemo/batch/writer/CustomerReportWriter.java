package com.cardemo.batch.writer;

import com.cardemo.batch.model.CustomerRecord;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.WritableResource;

/**
 * CSV writer for Customer Data Report.
 * Corresponds to CBCUS01C displaying entire CUSTOMER-RECORD.
 */
public class CustomerReportWriter extends FlatFileItemWriter<CustomerRecord> {

    private static final String[] FIELD_NAMES = {
            "custId", "custFirstName", "custMiddleName", "custLastName",
            "custAddrLine1", "custAddrLine2", "custAddrLine3",
            "custAddrStateCd", "custAddrCountryCd", "custAddrZip",
            "custPhoneNum1", "custPhoneNum2", "custSsn", "custGovtIssuedId",
            "custDobYyyyMmDd", "custEftAccountId", "custPriCardHolderInd",
            "custFicoCreditScore"
    };

    private static final String HEADER = "CUST-ID,CUST-FIRST-NAME,CUST-MIDDLE-NAME," +
            "CUST-LAST-NAME,CUST-ADDR-LINE-1,CUST-ADDR-LINE-2,CUST-ADDR-LINE-3," +
            "CUST-ADDR-STATE-CD,CUST-ADDR-COUNTRY-CD,CUST-ADDR-ZIP," +
            "CUST-PHONE-NUM-1,CUST-PHONE-NUM-2,CUST-SSN,CUST-GOVT-ISSUED-ID," +
            "CUST-DOB-YYYY-MM-DD,CUST-EFT-ACCOUNT-ID,CUST-PRI-CARD-HOLDER-IND," +
            "CUST-FICO-CREDIT-SCORE";

    public CustomerReportWriter(WritableResource resource) {
        setName("customerReportWriter");
        setResource(resource);
        setHeaderCallback(writer -> writer.write(HEADER));
        setLineAggregator(createLineAggregator());
    }

    private DelimitedLineAggregator<CustomerRecord> createLineAggregator() {
        DelimitedLineAggregator<CustomerRecord> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<CustomerRecord> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(FIELD_NAMES);

        aggregator.setFieldExtractor(extractor);
        return aggregator;
    }

    public static String getHeader() {
        return HEADER;
    }
}
