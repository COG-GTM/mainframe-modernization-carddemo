package com.carddemo.batch.account;

import com.carddemo.domain.TransactionCategoryBalance;
import com.carddemo.domain.TransactionCategoryBalanceId;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.item.ItemReader;

/**
 * {@link ItemReader} that replaces the sequential {@code TCATBAL} read loop of {@code CBACT04C}.
 *
 * <p>{@code CBACT04C} opens {@code TCATBALF} (a VSAM KSDS keyed on
 * {@code acct-id + type-cd + cat-cd}) with {@code ACCESS MODE IS SEQUENTIAL} and walks it in key
 * order, grouping consecutive rows by account. This reader loads the balances from the JPA
 * repository, sorts them by the same composite key, groups them by account preserving that order
 * and hands one {@link AccountInterestItem} (account + its balances) to the processor at a time.</p>
 */
public class TransactionCategoryBalanceItemReader implements ItemReader<AccountInterestItem> {

    private static final Comparator<TransactionCategoryBalance> BY_KEY =
            Comparator.comparing((TransactionCategoryBalance b) -> b.getId().getTrancatAcctId())
                    .thenComparing(b -> b.getId().getTrancatTypeCd())
                    .thenComparing(b -> b.getId().getTrancatCd());

    private final TransactionCategoryBalanceRepository repository;

    private Iterator<AccountInterestItem> iterator;

    public TransactionCategoryBalanceItemReader(TransactionCategoryBalanceRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountInterestItem read() {
        if (iterator == null) {
            iterator = groupByAccount().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<AccountInterestItem> groupByAccount() {
        List<TransactionCategoryBalance> all = new ArrayList<>(repository.findAll());
        all.sort(BY_KEY);

        Map<String, List<TransactionCategoryBalance>> grouped = new LinkedHashMap<>();
        for (TransactionCategoryBalance balance : all) {
            TransactionCategoryBalanceId id = balance.getId();
            grouped.computeIfAbsent(id.getTrancatAcctId(), k -> new ArrayList<>()).add(balance);
        }

        List<AccountInterestItem> items = new ArrayList<>(grouped.size());
        grouped.forEach((acctId, balances) -> items.add(new AccountInterestItem(acctId, balances)));
        return items;
    }
}
