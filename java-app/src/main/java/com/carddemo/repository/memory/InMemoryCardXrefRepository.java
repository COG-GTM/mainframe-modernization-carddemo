package com.carddemo.repository.memory;

import com.carddemo.domain.CardXref;
import com.carddemo.fixture.CardDemoDataSet;
import com.carddemo.repository.CardXrefRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** CARDXREF backed by a map seeded from cardxref.txt. */
@Repository
public class InMemoryCardXrefRepository implements CardXrefRepository, Reseedable {

    private final CardDemoDataSet dataSet;
    private final Map<String, CardXref> xrefs = new LinkedHashMap<>();

    public InMemoryCardXrefRepository(CardDemoDataSet dataSet) {
        this.dataSet = dataSet;
        seed();
    }

    @Override
    public void reseed() {
        seed();
    }

    private void seed() {
        xrefs.clear();
        dataSet.cardXrefs().forEach(xref -> xrefs.put(xref.getCardNumber(), CardXref.parse(xref.format())));
    }

    @Override
    public Optional<CardXref> findByCardNumber(String cardNumber) {
        return Optional.ofNullable(xrefs.get(RecordKeys.cardNumber(cardNumber)));
    }

    @Override
    public List<CardXref> findByAccountId(String accountId) {
        String key = RecordKeys.accountId(accountId);
        return xrefs.values().stream().filter(xref -> key.equals(xref.getAccountId())).toList();
    }

    @Override
    public List<CardXref> findByCustomerId(String customerId) {
        String key = RecordKeys.customerId(customerId);
        return xrefs.values().stream().filter(xref -> key.equals(xref.getCustomerId())).toList();
    }

    @Override
    public List<CardXref> findAll() {
        return new ArrayList<>(xrefs.values());
    }

    @Override
    public CardXref save(CardXref xref) {
        xrefs.put(RecordKeys.cardNumber(xref.getCardNumber()), xref);
        return xref;
    }
}
