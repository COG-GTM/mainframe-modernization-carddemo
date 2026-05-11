package uk.co.nationwide.unified.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.co.nationwide.unified.domain.Account;
import uk.co.nationwide.unified.domain.Member;
import uk.co.nationwide.unified.domain.MemberId;
import uk.co.nationwide.unified.domain.SourceSystem;
import uk.co.nationwide.unified.virginmoney.VmPrimaryAccountEntity;
import uk.co.nationwide.unified.virginmoney.VmSavingsAccountEntity;
import uk.co.nationwide.unified.virginmoney.VmUserEntity;
import uk.co.nationwide.unified.virginmoney.VmUserRepository;

/**
 * Anti-corruption adapter for the Virgin Money online-banking platform.
 *
 * <p>Knows about Spring/JPA-shaped records (user + primary + savings) and
 * maps them to the canonical {@link Member} domain. Knows nothing about
 * Nationwide.
 */
@Component
public class VirginMoneyMemberAdapter implements MemberAdapter {

    private final VmUserRepository userRepo;

    public VirginMoneyMemberAdapter(VmUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public SourceSystem source() {
        return SourceSystem.VIRGIN_MONEY;
    }

    @Override
    public Optional<Member> findMember(String memberId) {
        if (MemberId.sourceOf(memberId) != SourceSystem.VIRGIN_MONEY) {
            return Optional.empty();
        }
        Long userId = MemberId.virginMoneyUserId(memberId);
        return userRepo.findById(userId).map(this::toMember);
    }

    private Member toMember(VmUserEntity u) {
        List<Account> accounts = new ArrayList<>();
        if (u.getPrimaryAccount() != null) {
            accounts.add(toCurrentAccount(u.getPrimaryAccount()));
        }
        if (u.getSavingsAccount() != null) {
            accounts.add(toSavingsAccount(u.getSavingsAccount()));
        }
        return new Member(
                MemberId.forVirginMoney(u.getUserId()),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getPhone(),
                null,
                null,
                accounts,
                SourceSystem.VIRGIN_MONEY
        );
    }

    private Account toCurrentAccount(VmPrimaryAccountEntity p) {
        return new Account(
                String.valueOf(p.getId()),
                Account.AccountType.CURRENT,
                String.valueOf(p.getAccountNumber()),
                p.getAccountBalance(),
                null,
                "GBP",
                Account.AccountStatus.ACTIVE,
                SourceSystem.VIRGIN_MONEY
        );
    }

    private Account toSavingsAccount(VmSavingsAccountEntity s) {
        return new Account(
                String.valueOf(s.getId()),
                Account.AccountType.SAVINGS,
                String.valueOf(s.getAccountNumber()),
                s.getAccountBalance(),
                null,
                "GBP",
                Account.AccountStatus.ACTIVE,
                SourceSystem.VIRGIN_MONEY
        );
    }
}
