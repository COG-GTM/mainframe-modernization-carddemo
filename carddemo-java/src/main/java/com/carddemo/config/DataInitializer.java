package com.carddemo.config;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserSecurityRepository userRepo,
                                       AccountRepository accountRepo,
                                       CardRepository cardRepo,
                                       CardAccountXrefRepository xrefRepo,
                                       CustomerRepository customerRepo,
                                       TransactionTypeRepository typeRepo,
                                       DisclosureGroupRepository dgRepo,
                                       TransactionCategoryBalanceRepository tcbRepo,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            // Create default users (RACF equivalent)
            if (userRepo.count() == 0) {
                UserSecurity admin = new UserSecurity();
                admin.setUsrId("ADMIN001");
                admin.setUsrFname("ADMIN");
                admin.setUsrLname("USER");
                admin.setUsrPwd(passwordEncoder.encode("PASSWORD"));
                admin.setUsrType("A");
                userRepo.save(admin);

                UserSecurity user = new UserSecurity();
                user.setUsrId("USER0001");
                user.setUsrFname("REGULAR");
                user.setUsrLname("USER");
                user.setUsrPwd(passwordEncoder.encode("PASSWORD"));
                user.setUsrType("U");
                userRepo.save(user);
            }

            // Create sample account
            if (accountRepo.count() == 0) {
                Account acct = new Account();
                acct.setAcctId(10000000001L);
                acct.setActiveStatus("Y");
                acct.setCurrBal(new BigDecimal("1500.00"));
                acct.setCreditLimit(new BigDecimal("10000.00"));
                acct.setCashCreditLimit(new BigDecimal("2000.00"));
                acct.setOpenDate("2020-01-15");
                acct.setExpirationDate("2027-01-15");
                acct.setReissueDate("2025-01-15");
                acct.setCurrCycCredit(BigDecimal.ZERO);
                acct.setCurrCycDebit(BigDecimal.ZERO);
                acct.setAddrZip("10001");
                acct.setGroupId("GROUP001");
                accountRepo.save(acct);

                // Create sample card
                Card card = new Card();
                card.setCardNum("4111111111111111");
                card.setCardAcctId(10000000001L);
                card.setCardCvvCd(123);
                card.setCardEmbossedName("JOHN DOE");
                card.setCardExpirationDate("2027-01-15");
                card.setCardActiveStatus("Y");
                cardRepo.save(card);

                // Create cross-reference
                CardAccountXref xref = new CardAccountXref();
                xref.setCardNum("4111111111111111");
                xref.setCustId(100000001L);
                xref.setAcctId(10000000001L);
                xrefRepo.save(xref);

                // Create sample customer
                Customer cust = new Customer();
                cust.setCustId(100000001L);
                cust.setFirstName("JOHN");
                cust.setMiddleName("M");
                cust.setLastName("DOE");
                cust.setAddrLine1("123 MAIN ST");
                cust.setAddrStateCd("NY");
                cust.setAddrCountryCd("USA");
                cust.setAddrZip("10001");
                cust.setSsn(123456789L);
                cust.setFicoCreditScore(750);
                customerRepo.save(cust);

                // Create sample transaction type
                TransactionType tt = new TransactionType();
                tt.setTranType("PR");
                tt.setTranTypeDesc("Purchase");
                typeRepo.save(tt);

                // Create disclosure group
                DisclosureGroup dg = new DisclosureGroup();
                dg.setAcctGroupId("GROUP001");
                dg.setTranTypeCd("PR");
                dg.setTranCatCd(5000);
                dg.setIntRate(new BigDecimal("18.99"));
                dgRepo.save(dg);

                // Create category balance
                TransactionCategoryBalance tcb = new TransactionCategoryBalance();
                tcb.setAcctId(10000000001L);
                tcb.setTranTypeCd("PR");
                tcb.setTranCatCd(5000);
                tcb.setTranCatBal(new BigDecimal("500.00"));
                tcbRepo.save(tcb);
            }
        };
    }
}
