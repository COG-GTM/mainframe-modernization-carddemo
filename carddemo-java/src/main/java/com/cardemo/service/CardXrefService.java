package com.cardemo.service;

import com.cardemo.entity.CardXref;
import com.cardemo.repository.CardXrefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for CardXref entity operations.
 * Mirrors COBOL operations: keyed READ, sequential READ, WRITE, REWRITE.
 */
@Service
@Transactional
public class CardXrefService {

    private final CardXrefRepository cardXrefRepository;

    public CardXrefService(CardXrefRepository cardXrefRepository) {
        this.cardXrefRepository = cardXrefRepository;
    }

    @Transactional(readOnly = true)
    public Optional<CardXref> findById(String cardNum) {
        return cardXrefRepository.findById(cardNum);
    }

    @Transactional(readOnly = true)
    public List<CardXref> findAll() {
        return cardXrefRepository.findAll();
    }

    public CardXref save(CardXref xref) {
        return cardXrefRepository.save(xref);
    }

    public CardXref update(CardXref xref) {
        return cardXrefRepository.save(xref);
    }

    @Transactional(readOnly = true)
    public List<CardXref> findByCustId(Long custId) {
        return cardXrefRepository.findByXrefCustId(custId);
    }

    @Transactional(readOnly = true)
    public List<CardXref> findByAcctId(Long acctId) {
        return cardXrefRepository.findByXrefAcctId(acctId);
    }
}
