package com.carddemo.repository.memory;

import com.carddemo.domain.TransactionCategory;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.util.CobolCodec;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** TRANCATG reference table seeded from trancatg.txt. */
@Repository
public class InMemoryTransactionCategoryRepository implements TransactionCategoryRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, TransactionCategory> categories = new LinkedHashMap<>();

    public InMemoryTransactionCategoryRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        categories.clear();
        dataSet.transactionCategories().forEach(category -> categories.put(category.key(), category));
    }

    @Override
    public Optional<TransactionCategory> find(String typeCode, int categoryCode) {
        return Optional.ofNullable(
                categories.get(CobolCodec.encodeText(typeCode, 2) + CobolCodec.encodeNumeric(categoryCode, 4)));
    }

    @Override
    public List<TransactionCategory> findAll() {
        return new ArrayList<>(categories.values());
    }
}
