package com.carddemo.repository.memory;

import com.carddemo.domain.TransactionType;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.TransactionTypeRepository;
import com.carddemo.util.CobolCodec;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** TRANTYPE reference table seeded from trantype.txt. */
@Repository
public class InMemoryTransactionTypeRepository implements TransactionTypeRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, TransactionType> transactionTypes = new LinkedHashMap<>();

    public InMemoryTransactionTypeRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        transactionTypes.clear();
        dataSet.transactionTypes().forEach(type -> transactionTypes.put(type.typeCode(), type));
    }

    @Override
    public Optional<TransactionType> findByCode(String typeCode) {
        return Optional.ofNullable(transactionTypes.get(CobolCodec.encodeText(typeCode, 2)));
    }

    @Override
    public List<TransactionType> findAll() {
        return new ArrayList<>(transactionTypes.values());
    }
}
