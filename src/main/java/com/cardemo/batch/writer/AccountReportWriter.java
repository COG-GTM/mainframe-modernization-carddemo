package com.cardemo.batch.writer;

import com.cardemo.batch.model.AccountRecord;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.WritableResource;

/**
 * CSV writer for Account Data Report.
 * Corresponds to CBACT01C 1100-DISPLAY-ACCT-RECORD paragraph,
 * outputting all 11 displayed fields as CSV with headers.
 */
public class AccountReportWriter extends FlatFileItemWriter<AccountRecord> {

    private static final String[] FIELD_NAMES = {
            "acctId", "acctActiveStatus", "acctCurrBal", "acctCreditLimit",
            "acctCashCreditLimit", "acctOpenDate", "acctExpiraionDate",
            "acctReissueDate", "acctCurrCycCredit", "acctCurrCycDebit",
            "acctGroupId"
    };

    private static final String HEADER = "ACCT-ID,ACCT-ACTIVE-STATUS,ACCT-CURR-BAL," +
            "ACCT-CREDIT-LIMIT,ACCT-CASH-CREDIT-LIMIT,ACCT-OPEN-DATE," +
            "ACCT-EXPIRAION-DATE,ACCT-REISSUE-DATE,ACCT-CURR-CYC-CREDIT," +
            "ACCT-CURR-CYC-DEBIT,ACCT-GROUP-ID";

    public AccountReportWriter(WritableResource resource) {
        setName("accountReportWriter");
        setResource(resource);
        setHeaderCallback(writer -> writer.write(HEADER));
        setLineAggregator(createLineAggregator());
    }

    private DelimitedLineAggregator<AccountRecord> createLineAggregator() {
        DelimitedLineAggregator<AccountRecord> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<AccountRecord> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(FIELD_NAMES);

        aggregator.setFieldExtractor(extractor);
        return aggregator;
    }

    public static String getHeader() {
        return HEADER;
    }
}
