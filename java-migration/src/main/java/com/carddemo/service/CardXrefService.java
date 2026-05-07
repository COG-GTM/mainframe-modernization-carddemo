package com.carddemo.service;

import com.carddemo.entity.CardXref;
import com.carddemo.repository.CardXrefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for {@link CardXref} operations.
 * <p>
 * Replaces CICS file-control logic from COBOL programs:
 * COACTVWC (READ), COCRDSLC (READ), COCRDUPC (READ), COACTUPC (READ),
 * COTRN02C (READ/STARTBR/READPREV/WRITE), COBIL00C (READ/REWRITE/STARTBR/READPREV/WRITE).
 */
@Service
@Transactional
public class CardXrefService {

    private final CardXrefRepository repository;

    public CardXrefService(CardXrefRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<CardXref> findById(String cardNum) {
        return repository.findById(cardNum);
    }

    @Transactional(readOnly = true)
    public List<CardXref> findAll() {
        return repository.findAll();
    }

    public CardXref save(CardXref cardXref) {
        return repository.save(cardXref);
    }

    public CardXref update(CardXref cardXref) {
        return repository.save(cardXref);
    }

    public void delete(String cardNum) {
        repository.deleteById(cardNum);
    }

    @Transactional(readOnly = true)
    public List<CardXref> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<CardXref> findByAccountId(Long accountId) {
        return repository.findByAccountId(accountId);
    }
}
