package com.carddemo.interestcalc.batch;

import com.carddemo.interestcalc.repository.InMemoryAccountRepository;
import com.carddemo.interestcalc.repository.InMemoryCardXrefRepository;
import com.carddemo.interestcalc.repository.InMemoryDisclosureGroupRepository;
import com.carddemo.interestcalc.service.InterestCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Clock;

/**
 * Batch driver equivalent of JCL job INTCALC running CBACT04C: loads the input files,
 * runs the interest calculation, and writes the TRANSACT output file.
 *
 * <p>Input/output locations and the PARM date are configurable via properties
 * (see {@code application.yml}).
 */
@Component
public class InterestCalculationJob implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationJob.class);

    private final Path dataDir;
    private final Path outputFile;
    private final String parmDate;

    public InterestCalculationJob(
            @Value("${carddemo.data-dir:../app/data/ASCII}") String dataDir,
            @Value("${carddemo.transact-output:transact.txt}") String outputFile,
            @Value("${carddemo.parm-date:2022-07-19}") String parmDate) {
        this.dataDir = Path.of(dataDir);
        this.outputFile = Path.of(outputFile);
        this.parmDate = parmDate;
    }

    @Override
    public void run(String... args) {
        log.info("START OF EXECUTION OF PROGRAM CBACT04C");

        var tcatbalRecords = AsciiFileLoader.loadTranCatBal(dataDir.resolve("tcatbal.txt"));

        var accountRepository = new InMemoryAccountRepository();
        AsciiFileLoader.loadAccounts(dataDir.resolve("acctdata.txt")).forEach(accountRepository::load);

        var xrefRepository = new InMemoryCardXrefRepository();
        AsciiFileLoader.loadCardXrefs(dataDir.resolve("cardxref.txt")).forEach(xrefRepository::load);

        var discgrpRepository = new InMemoryDisclosureGroupRepository();
        AsciiFileLoader.loadDisclosureGroups(dataDir.resolve("discgrp.txt")).forEach(discgrpRepository::load);

        try (var transactionWriter = new FlatFileTransactionWriter(outputFile)) {
            var service = new InterestCalculationService(
                    accountRepository, xrefRepository, discgrpRepository, transactionWriter,
                    Clock.systemDefaultZone());
            long processed = service.run(tcatbalRecords, parmDate);
            log.info("TCATBAL records processed: {}", processed);
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT04C");
    }
}
