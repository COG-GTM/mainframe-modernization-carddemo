package uk.co.nationwide.unified.adapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.co.nationwide.unified.domain.Account;
import uk.co.nationwide.unified.domain.Member;
import uk.co.nationwide.unified.domain.MemberId;
import uk.co.nationwide.unified.domain.SourceSystem;
import uk.co.nationwide.unified.nationwide.NwAccountEntity;
import uk.co.nationwide.unified.nationwide.NwAccountRepository;
import uk.co.nationwide.unified.nationwide.NwCardXrefEntity;
import uk.co.nationwide.unified.nationwide.NwCardXrefRepository;
import uk.co.nationwide.unified.nationwide.NwCustomerEntity;
import uk.co.nationwide.unified.nationwide.NwCustomerRepository;

/**
 * Anti-corruption adapter for the Nationwide legacy card platform.
 *
 * <p>Knows about VSAM-shaped records (customer + xref + account) and maps
 * them to the canonical {@link Member} domain. Knows nothing about Virgin
 * Money.
 */
@Component
public class NationwideMemberAdapter implements MemberAdapter {

    private final NwCustomerRepository customerRepo;
    private final NwCardXrefRepository xrefRepo;
    private final NwAccountRepository accountRepo;

    public NationwideMemberAdapter(
            NwCustomerRepository customerRepo,
            NwCardXrefRepository xrefRepo,
            NwAccountRepository accountRepo) {
        this.customerRepo = customerRepo;
        this.xrefRepo = xrefRepo;
        this.accountRepo = accountRepo;
    }

    @Override
    public SourceSystem source() {
        return SourceSystem.NATIONWIDE;
    }

    @Override
    public Optional<Member> findMember(String memberId) {
        if (MemberId.sourceOf(memberId) != SourceSystem.NATIONWIDE) {
            return Optional.empty();
        }
        Long custId = MemberId.nationwideCustId(memberId);
        return customerRepo.findById(custId).map(c -> toMember(c, accountsFor(custId)));
    }

    private List<Account> accountsFor(Long custId) {
        List<NwCardXrefEntity> xrefs = xrefRepo.findByXrefCustId(custId);
        List<Account> out = new ArrayList<>();
        for (NwCardXrefEntity x : xrefs) {
            accountRepo.findById(x.getXrefAcctId()).ifPresent(a -> out.add(toAccount(a, x)));
        }
        return out;
    }

    private Account toAccount(NwAccountEntity a, NwCardXrefEntity x) {
        Account.AccountStatus status = mapStatus(a.getAcctActiveStatus());
        return new Account(
                String.valueOf(a.getAcctId()),
                Account.AccountType.CARD,
                maskCardNumber(x.getXrefCardNum()),
                a.getAcctCurrBal(),
                a.getAcctCreditLimit(),
                "GBP",
                status,
                SourceSystem.NATIONWIDE
        );
    }

    private Member toMember(NwCustomerEntity c, List<Account> accounts) {
        Member.Address addr = new Member.Address(
                c.getCustAddrLine1(),
                c.getCustAddrLine2(),
                c.getCustAddrLine3(),
                null,
                c.getCustAddrZip(),
                c.getCustAddrCountryCd()
        );
        return new Member(
                MemberId.forNationwide(c.getCustId()),
                trim(c.getCustFirstName()),
                trim(c.getCustLastName()),
                null,
                trim(c.getCustPhoneNum1()),
                addr,
                c.getCustDob(),
                accounts,
                SourceSystem.NATIONWIDE
        );
    }

    private static String maskCardNumber(String cardNum) {
        if (cardNum == null || cardNum.length() < 4) return cardNum;
        return "****-****-****-" + cardNum.substring(cardNum.length() - 4);
    }

    private static Account.AccountStatus mapStatus(String status) {
        if ("Y".equalsIgnoreCase(status)) return Account.AccountStatus.ACTIVE;
        return Account.AccountStatus.INACTIVE;
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    /** Used by integration tests to seed sample data. */
    public BigDecimal placeholderForJavadocLink() { return BigDecimal.ZERO; }
}
