package uk.co.nationwide.unified;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.co.nationwide.unified.domain.Account;
import uk.co.nationwide.unified.domain.Member;
import uk.co.nationwide.unified.domain.SourceSystem;
import uk.co.nationwide.unified.nationwide.NwAccountEntity;
import uk.co.nationwide.unified.nationwide.NwAccountRepository;
import uk.co.nationwide.unified.nationwide.NwCardXrefEntity;
import uk.co.nationwide.unified.nationwide.NwCardXrefRepository;
import uk.co.nationwide.unified.nationwide.NwCustomerEntity;
import uk.co.nationwide.unified.nationwide.NwCustomerRepository;
import uk.co.nationwide.unified.service.MemberLookupService;
import uk.co.nationwide.unified.virginmoney.VmPrimaryAccountEntity;
import uk.co.nationwide.unified.virginmoney.VmPrimaryAccountRepository;
import uk.co.nationwide.unified.virginmoney.VmSavingsAccountEntity;
import uk.co.nationwide.unified.virginmoney.VmSavingsAccountRepository;
import uk.co.nationwide.unified.virginmoney.VmUserEntity;
import uk.co.nationwide.unified.virginmoney.VmUserRepository;

/**
 * End-to-end test of the unified member-lookup flow. Seeds one member in
 * each source system and asserts the unified API routes correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class MemberLookupIntegrationTest {

    @Autowired private MemberLookupService memberLookupService;
    @Autowired private NwCustomerRepository nwCustomerRepo;
    @Autowired private NwAccountRepository nwAccountRepo;
    @Autowired private NwCardXrefRepository nwXrefRepo;
    @Autowired private VmUserRepository vmUserRepo;
    @Autowired private VmPrimaryAccountRepository vmPrimaryRepo;
    @Autowired private VmSavingsAccountRepository vmSavingsRepo;

    @BeforeEach
    void seed() {
        nwXrefRepo.deleteAll();
        nwAccountRepo.deleteAll();
        nwCustomerRepo.deleteAll();
        vmUserRepo.deleteAll();
        vmPrimaryRepo.deleteAll();
        vmSavingsRepo.deleteAll();

        NwCustomerEntity helen = new NwCustomerEntity();
        helen.setCustId(1234L);
        helen.setCustFirstName("Helen");
        helen.setCustLastName("Wright");
        helen.setCustAddrLine1("Pipers Way");
        helen.setCustAddrZip("SN38 1NW");
        helen.setCustAddrCountryCd("GBR");
        helen.setCustPhoneNum1("01793 655555");
        helen.setCustDob(LocalDate.of(1973, 8, 22));
        nwCustomerRepo.save(helen);

        NwAccountEntity helensAcct = new NwAccountEntity();
        helensAcct.setAcctId(12345678901L);
        helensAcct.setAcctActiveStatus("Y");
        helensAcct.setAcctCurrBal(new BigDecimal("245.50"));
        helensAcct.setAcctCreditLimit(new BigDecimal("5000.00"));
        nwAccountRepo.save(helensAcct);

        NwCardXrefEntity helensXref = new NwCardXrefEntity();
        helensXref.setXrefCardNum("4929123456789010");
        helensXref.setXrefCustId(1234L);
        helensXref.setXrefAcctId(12345678901L);
        nwXrefRepo.save(helensXref);

        VmPrimaryAccountEntity sanjaysPrimary = new VmPrimaryAccountEntity();
        sanjaysPrimary.setAccountNumber(30100001);
        sanjaysPrimary.setAccountBalance(new BigDecimal("1842.75"));
        vmPrimaryRepo.save(sanjaysPrimary);

        VmSavingsAccountEntity sanjaysSavings = new VmSavingsAccountEntity();
        sanjaysSavings.setAccountNumber(30200001);
        sanjaysSavings.setAccountBalance(new BigDecimal("18430.00"));
        vmSavingsRepo.save(sanjaysSavings);

        VmUserEntity sanjay = new VmUserEntity();
        sanjay.setUsername("sgupta");
        sanjay.setFirstName("Sanjay");
        sanjay.setLastName("Gupta");
        sanjay.setEmail("sanjay.gupta@example.co.uk");
        sanjay.setPhone("07700 900111");
        sanjay.setPrimaryAccount(sanjaysPrimary);
        sanjay.setSavingsAccount(sanjaysSavings);
        vmUserRepo.save(sanjay);
    }

    @Test
    @DisplayName("routes nbs- IDs to the Nationwide adapter and returns card account")
    void routesToNationwide() {
        Optional<Member> found = memberLookupService.findMember("nbs-000001234");
        assertThat(found).isPresent();
        Member member = found.get();

        assertThat(member.firstName()).isEqualTo("Helen");
        assertThat(member.lastName()).isEqualTo("Wright");
        assertThat(member.primarySource()).isEqualTo(SourceSystem.NATIONWIDE);
        assertThat(member.address().postcode()).isEqualTo("SN38 1NW");
        assertThat(member.accounts()).hasSize(1);
        Account card = member.accounts().get(0);
        assertThat(card.type()).isEqualTo(Account.AccountType.CARD);
        assertThat(card.source()).isEqualTo(SourceSystem.NATIONWIDE);
        assertThat(card.balance()).isEqualByComparingTo("245.50");
        assertThat(card.displayNumber()).startsWith("****-****-****-").endsWith("9010");
    }

    @Test
    @DisplayName("routes vm- IDs to the Virgin Money adapter and returns current and savings accounts")
    void routesToVirginMoney() {
        Long sanjayId = vmUserRepo.findByEmail("sanjay.gupta@example.co.uk").orElseThrow().getUserId();
        Optional<Member> found = memberLookupService.findMember("vm-" + sanjayId);
        assertThat(found).isPresent();
        Member member = found.get();

        assertThat(member.firstName()).isEqualTo("Sanjay");
        assertThat(member.lastName()).isEqualTo("Gupta");
        assertThat(member.email()).isEqualTo("sanjay.gupta@example.co.uk");
        assertThat(member.primarySource()).isEqualTo(SourceSystem.VIRGIN_MONEY);
        assertThat(member.accounts()).hasSize(2);
        assertThat(member.accounts()).extracting(Account::type)
                .containsExactlyInAnyOrder(Account.AccountType.CURRENT, Account.AccountType.SAVINGS);
        assertThat(member.accounts()).allSatisfy(a -> {
            assertThat(a.source()).isEqualTo(SourceSystem.VIRGIN_MONEY);
            assertThat(a.currency()).isEqualTo("GBP");
        });
    }

    @Test
    @DisplayName("returns empty for unknown member ID in the right estate")
    void emptyForUnknownNationwide() {
        Optional<Member> found = memberLookupService.findMember("nbs-999999999");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("rejects member IDs without a recognised prefix")
    void rejectsUnknownPrefix() {
        try {
            memberLookupService.findMember("unknown-1");
            assert false : "expected IllegalArgumentException";
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }
}
