package com.carddemo.etl;

import com.carddemo.etl.reader.SourceFormat;
import com.carddemo.etl.validation.AccountValidator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AccountEtlPipelineTest {

    private static String validRecord(long id, char status) {
        return String.format("%011d", id)
                + status
                + "00000001940{00000020200{00000010200{"
                + "2014-11-202025-05-202025-05-20"
                + "00000000000{00000000000{"
                + "A000000000"
                + " ".repeat(10)
                + " ".repeat(178);
    }

    @Test
    void dryRunCountsValidInvalidAndFailedRecords() throws Exception {
        String good = validRecord(1, 'Y');
        String invalidStatus = validRecord(2, 'X');     // fails validation
        String unparseable = "0000000000XY";            // too short -> parse failure
        String content = String.join("\n", good, invalidStatus, unparseable) + "\n";

        AccountEtlPipeline pipeline =
                new AccountEtlPipeline(SourceFormat.ASCII, new AccountValidator(), 10, true);

        EtlResult result;
        try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.ISO_8859_1))) {
            result = pipeline.run(in, null);
        }

        assertThat(result.read()).isEqualTo(2);     // good + invalidStatus parsed
        assertThat(result.loaded()).isEqualTo(1);   // only the good record is loadable
        assertThat(result.invalid()).isEqualTo(1);  // invalidStatus
        assertThat(result.failed()).isEqualTo(1);   // unparseable
    }

    @Test
    void batchingFlushesRemainder() throws Exception {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 7; i++) {
            content.append(validRecord(i, 'Y')).append('\n');
        }
        AccountEtlPipeline pipeline =
                new AccountEtlPipeline(SourceFormat.ASCII, new AccountValidator(), 3, true);

        EtlResult result;
        try (InputStream in = new ByteArrayInputStream(content.toString().getBytes(StandardCharsets.ISO_8859_1))) {
            result = pipeline.run(in, null);
        }
        assertThat(result.read()).isEqualTo(7);
        assertThat(result.loaded()).isEqualTo(7);
    }
}
