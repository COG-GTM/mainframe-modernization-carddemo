package com.carddemo.batch;

import com.carddemo.domain.Account;
import com.carddemo.domain.Card;
import com.carddemo.domain.CardXref;
import com.carddemo.domain.Customer;
import com.carddemo.domain.DailyTransaction;
import com.carddemo.domain.DisclosureGroup;
import com.carddemo.domain.DisclosureGroupId;
import com.carddemo.domain.SecurityUser;
import com.carddemo.domain.TransactionBase;
import com.carddemo.domain.TransactionCategory;
import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionCategoryBalanceId;
import com.carddemo.domain.TransactionCategoryId;
import com.carddemo.domain.TransactionType;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.SecurityUserRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.repository.TransactionTypeRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch {@code dataLoadJob} — the Java replacement for the legacy IDCAMS load JCLs
 * (ACCTFILE / CARDFILE / CUSTFILE / XREFFILE / DISCGRP / TCATBALF / TRANCATG / TRANTYPE /
 * DUSRSECJ and the DALYTRAN input).
 *
 * <p>Each step reads one fixed-width ASCII seed file from the classpath ({@code seed/*.txt}),
 * maps the copybook fields (see the {@code *Processor} lambdas) and writes via the entity's
 * Spring Data repository ({@code save} = upsert, so the job is idempotent). Steps are ordered
 * so that referenced rows exist before their foreign keys (customer/account/card before the
 * card cross-reference).</p>
 */
@Configuration
public class DataLoadJobConfig {

    static final int CHUNK = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;

    public DataLoadJobConfig(JobRepository jobRepository, PlatformTransactionManager txManager) {
        this.jobRepository = jobRepository;
        this.txManager = txManager;
    }

    @Bean
    public Job dataLoadJob(Step loadCustomerStep,
                           Step loadAccountStep,
                           Step loadCardStep,
                           Step loadCardXrefStep,
                           Step loadTransactionTypeStep,
                           Step loadTransactionCategoryStep,
                           Step loadDisclosureGroupStep,
                           Step loadTranCatBalanceStep,
                           Step loadDailyTransactionStep,
                           Step loadSecurityUserStep) {
        return new JobBuilder("dataLoadJob", jobRepository)
                .start(loadCustomerStep)
                .next(loadAccountStep)
                .next(loadCardStep)
                .next(loadCardXrefStep)
                .next(loadTransactionTypeStep)
                .next(loadTransactionCategoryStep)
                .next(loadDisclosureGroupStep)
                .next(loadTranCatBalanceStep)
                .next(loadDailyTransactionStep)
                .next(loadSecurityUserStep)
                .build();
    }

    // --- steps -----------------------------------------------------------------------------

    @Bean
    public Step loadCustomerStep(CustomerRepository repo) {
        return step("loadCustomerStep", "seed/custdata.txt", customerProcessor(), repo);
    }

    @Bean
    public Step loadAccountStep(AccountRepository repo) {
        return step("loadAccountStep", "seed/acctdata.txt", accountProcessor(), repo);
    }

    @Bean
    public Step loadCardStep(CardRepository repo) {
        return step("loadCardStep", "seed/carddata.txt", cardProcessor(), repo);
    }

    @Bean
    public Step loadCardXrefStep(CardXrefRepository repo) {
        return step("loadCardXrefStep", "seed/cardxref.txt", cardXrefProcessor(), repo);
    }

    @Bean
    public Step loadTransactionTypeStep(TransactionTypeRepository repo) {
        return step("loadTransactionTypeStep", "seed/trantype.txt", transactionTypeProcessor(), repo);
    }

    @Bean
    public Step loadTransactionCategoryStep(TransactionCategoryRepository repo) {
        return step("loadTransactionCategoryStep", "seed/trancatg.txt", transactionCategoryProcessor(), repo);
    }

    @Bean
    public Step loadDisclosureGroupStep(DisclosureGroupRepository repo) {
        return step("loadDisclosureGroupStep", "seed/discgrp.txt", disclosureGroupProcessor(), repo);
    }

    @Bean
    public Step loadTranCatBalanceStep(TransactionCategoryBalanceRepository repo) {
        return step("loadTranCatBalanceStep", "seed/tcatbal.txt", tranCatBalanceProcessor(), repo);
    }

    @Bean
    public Step loadDailyTransactionStep(DailyTransactionRepository repo) {
        return step("loadDailyTransactionStep", "seed/dailytran.txt", dailyTransactionProcessor(), repo);
    }

    @Bean
    public Step loadSecurityUserStep(SecurityUserRepository repo) {
        return step("loadSecurityUserStep", "seed/usrsec.txt", securityUserProcessor(), repo);
    }

    private <T> Step step(String name, String resource, ItemProcessor<String, T> processor,
                          CrudRepository<T, ?> repo) {
        return new StepBuilder(name, jobRepository)
                .<String, T>chunk(CHUNK, txManager)
                .reader(reader(name + "Reader", resource))
                .processor(processor)
                .writer(writer(repo))
                .build();
    }

    private FlatFileItemReader<String> reader(String name, String resource) {
        return new FlatFileItemReaderBuilder<String>()
                .name(name)
                .resource(new ClassPathResource(resource))
                .lineMapper(new PassThroughLineMapper())
                .strict(true)
                .build();
    }

    private <T> RepositoryItemWriter<T> writer(CrudRepository<T, ?> repo) {
        return new RepositoryItemWriterBuilder<T>()
                .repository(repo)
                .methodName("save")
                .build();
    }

    // --- processors (fixed-width field mapping per copybook) -------------------------------

    private static boolean blank(String line) {
        return line == null || line.trim().isEmpty();
    }

    private static void copyTransactionBody(String line, TransactionBase t) {
        t.setTranTypeCd(SeedFieldParser.str(line, 16, 2));
        t.setTranCatCd(SeedFieldParser.integer(line, 18, 4));
        t.setTranSource(SeedFieldParser.str(line, 22, 10));
        t.setTranDesc(SeedFieldParser.str(line, 32, 100));
        t.setTranAmt(SeedFieldParser.signedDecimal(line, 132, 11, 2));
        t.setTranMerchantId(SeedFieldParser.id(line, 143, 9));
        t.setTranMerchantName(SeedFieldParser.str(line, 152, 50));
        t.setTranMerchantCity(SeedFieldParser.str(line, 202, 50));
        t.setTranMerchantZip(SeedFieldParser.str(line, 252, 10));
        t.setTranCardNum(SeedFieldParser.id(line, 262, 16));
        t.setTranOrigTs(SeedFieldParser.str(line, 278, 26));
        t.setTranProcTs(SeedFieldParser.str(line, 304, 26));
    }

    ItemProcessor<String, Account> accountProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            Account a = new Account();
            a.setAcctId(SeedFieldParser.id(line, 0, 11));
            a.setAcctActiveStatus(SeedFieldParser.str(line, 11, 1));
            a.setAcctCurrBal(SeedFieldParser.signedDecimal(line, 12, 12, 2));
            a.setAcctCreditLimit(SeedFieldParser.signedDecimal(line, 24, 12, 2));
            a.setAcctCashCreditLimit(SeedFieldParser.signedDecimal(line, 36, 12, 2));
            a.setAcctOpenDate(SeedFieldParser.str(line, 48, 10));
            a.setAcctExpirationDate(SeedFieldParser.str(line, 58, 10));
            a.setAcctReissueDate(SeedFieldParser.str(line, 68, 10));
            a.setAcctCurrCycCredit(SeedFieldParser.signedDecimal(line, 78, 12, 2));
            a.setAcctCurrCycDebit(SeedFieldParser.signedDecimal(line, 90, 12, 2));
            a.setAcctAddrZip(SeedFieldParser.str(line, 102, 10));
            a.setAcctGroupId(SeedFieldParser.str(line, 112, 10));
            return a;
        };
    }

    ItemProcessor<String, Card> cardProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            Card c = new Card();
            c.setCardNum(SeedFieldParser.id(line, 0, 16));
            c.setCardAcctId(SeedFieldParser.id(line, 16, 11));
            c.setCardCvvCd(SeedFieldParser.id(line, 27, 3));
            c.setCardEmbossedName(SeedFieldParser.str(line, 30, 50));
            c.setCardExpirationDate(SeedFieldParser.str(line, 80, 10));
            c.setCardActiveStatus(SeedFieldParser.str(line, 90, 1));
            return c;
        };
    }

    ItemProcessor<String, CardXref> cardXrefProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            CardXref x = new CardXref();
            x.setXrefCardNum(SeedFieldParser.id(line, 0, 16));
            x.setXrefCustId(SeedFieldParser.id(line, 16, 9));
            x.setXrefAcctId(SeedFieldParser.id(line, 25, 11));
            return x;
        };
    }

    ItemProcessor<String, Customer> customerProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            Customer c = new Customer();
            c.setCustId(SeedFieldParser.id(line, 0, 9));
            c.setCustFirstName(SeedFieldParser.str(line, 9, 25));
            c.setCustMiddleName(SeedFieldParser.str(line, 34, 25));
            c.setCustLastName(SeedFieldParser.str(line, 59, 25));
            c.setCustAddrLine1(SeedFieldParser.str(line, 84, 50));
            c.setCustAddrLine2(SeedFieldParser.str(line, 134, 50));
            c.setCustAddrLine3(SeedFieldParser.str(line, 184, 50));
            c.setCustAddrStateCd(SeedFieldParser.str(line, 234, 2));
            c.setCustAddrCountryCd(SeedFieldParser.str(line, 236, 3));
            c.setCustAddrZip(SeedFieldParser.str(line, 239, 10));
            c.setCustPhoneNum1(SeedFieldParser.str(line, 249, 15));
            c.setCustPhoneNum2(SeedFieldParser.str(line, 264, 15));
            c.setCustSsn(SeedFieldParser.id(line, 279, 9));
            c.setCustGovtIssuedId(SeedFieldParser.str(line, 288, 20));
            c.setCustDobYyyyMmDd(SeedFieldParser.str(line, 308, 10));
            c.setCustEftAccountId(SeedFieldParser.str(line, 318, 10));
            c.setCustPriCardHolderInd(SeedFieldParser.str(line, 328, 1));
            c.setCustFicoCreditScore(SeedFieldParser.integer(line, 329, 3));
            return c;
        };
    }

    ItemProcessor<String, DailyTransaction> dailyTransactionProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            DailyTransaction t = new DailyTransaction();
            t.setDalytranId(SeedFieldParser.id(line, 0, 16));
            copyTransactionBody(line, t);
            return t;
        };
    }

    ItemProcessor<String, TransactionType> transactionTypeProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            TransactionType t = new TransactionType();
            t.setTranType(SeedFieldParser.id(line, 0, 2));
            t.setTranTypeDesc(SeedFieldParser.str(line, 2, 50));
            return t;
        };
    }

    ItemProcessor<String, TransactionCategory> transactionCategoryProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            TransactionCategory c = new TransactionCategory();
            c.setId(new TransactionCategoryId(
                    SeedFieldParser.id(line, 0, 2),
                    SeedFieldParser.integer(line, 2, 4)));
            c.setTranCatTypeDesc(SeedFieldParser.str(line, 6, 50));
            return c;
        };
    }

    ItemProcessor<String, DisclosureGroup> disclosureGroupProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            DisclosureGroup d = new DisclosureGroup();
            d.setId(new DisclosureGroupId(
                    SeedFieldParser.id(line, 0, 10),
                    SeedFieldParser.id(line, 10, 2),
                    SeedFieldParser.integer(line, 12, 4)));
            d.setDisIntRate(SeedFieldParser.signedDecimal(line, 16, 6, 2));
            return d;
        };
    }

    ItemProcessor<String, TransactionCategoryBalance> tranCatBalanceProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            TransactionCategoryBalance b = new TransactionCategoryBalance();
            b.setId(new TransactionCategoryBalanceId(
                    SeedFieldParser.id(line, 0, 11),
                    SeedFieldParser.id(line, 11, 2),
                    SeedFieldParser.integer(line, 13, 4)));
            b.setTranCatBal(SeedFieldParser.signedDecimal(line, 17, 11, 2));
            return b;
        };
    }

    ItemProcessor<String, SecurityUser> securityUserProcessor() {
        return line -> {
            if (blank(line)) {
                return null;
            }
            SecurityUser u = new SecurityUser();
            u.setSecUsrId(SeedFieldParser.id(line, 0, 8));
            u.setSecUsrFname(SeedFieldParser.str(line, 8, 20));
            u.setSecUsrLname(SeedFieldParser.str(line, 28, 20));
            u.setSecUsrPwd(SeedFieldParser.str(line, 48, 8));
            u.setSecUsrType(SeedFieldParser.str(line, 56, 1));
            return u;
        };
    }
}
