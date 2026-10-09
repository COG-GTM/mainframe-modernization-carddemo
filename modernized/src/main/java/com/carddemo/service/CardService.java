package com.carddemo.service;

import com.carddemo.domain.Card;
import com.carddemo.exception.BusinessRuleException;
import com.carddemo.exception.ConcurrentUpdateException;
import com.carddemo.exception.RecordNotFoundException;
import com.carddemo.repository.CardRepository;
import com.carddemo.util.CobolDateValidator;
import com.carddemo.web.dto.CardSummary;
import com.carddemo.web.dto.CardUpdateRequest;
import com.carddemo.web.dto.PageResponse;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** COCRDLIC (card list), COCRDSLC (card view) and COCRDUPC (card update). */
@Service
public class CardService {

    /** COCRDLIC displayed seven rows per screen. */
    public static final int DEFAULT_PAGE_SIZE = 7;

    private static final Pattern CARD_NUMBER = Pattern.compile("\\d{16}");
    private static final Pattern ALPHABETIC = Pattern.compile("[A-Za-z ]+");

    private final CardRepository cards;

    public CardService(CardRepository cards) {
        this.cards = cards;
    }

    @Transactional(readOnly = true)
    public PageResponse<CardSummary> list(Long accountId, String cardNumber, int page, int size) {
        if (cardNumber != null && !cardNumber.isBlank()) {
            validateCardNumber(cardNumber);
            Long account = accountId == null ? null : validAccountId(accountId);
            List<CardSummary> matches = cards.findById(cardNumber.trim())
                    .filter(card -> account == null || account.equals(card.getAccountId()))
                    .map(CardService::toSummary)
                    .stream()
                    .toList();
            if (matches.isEmpty() || page > 0) {
                throw new RecordNotFoundException("NO RECORDS FOUND FOR THIS SEARCH CONDITION.");
            }
            return new PageResponse<>(matches, 0, size, 1, 1);
        }
        PageRequest request = PageRequest.of(page, size, Sort.by("cardNumber"));
        Page<Card> result = accountId == null
                ? cards.findAll(request)
                : cards.findByAccountId(validAccountId(accountId), request);
        if (result.isEmpty()) {
            throw new RecordNotFoundException("NO RECORDS FOUND FOR THIS SEARCH CONDITION.");
        }
        return PageResponse.of(result, CardService::toSummary);
    }

    @Transactional(readOnly = true)
    public CardSummary view(String cardNumber) {
        validateCardNumber(cardNumber);
        return cards.findById(cardNumber)
                .map(CardService::toSummary)
                .orElseThrow(() -> new RecordNotFoundException("Did not find this account in cards database"));
    }

    @Transactional
    public CardSummary update(String cardNumber, CardUpdateRequest request) {
        validateCardNumber(cardNumber);
        Card card = cards.findById(cardNumber)
                .orElseThrow(() -> new RecordNotFoundException("Did not find this account in cards database"));
        if (request.version() == null || request.version() != card.getVersion()) {
            throw new ConcurrentUpdateException("Record changed by some one else. Please review");
        }

        String name = request.embossedName() == null ? "" : request.embossedName().trim();
        if (name.isEmpty()) {
            throw new BusinessRuleException("Card name not provided");
        }
        if (!ALPHABETIC.matcher(name).matches()) {
            throw new BusinessRuleException("Card name can only contain alphabets and spaces");
        }

        String status = request.activeStatus() == null ? "" : request.activeStatus().trim();
        if (!status.equalsIgnoreCase("Y") && !status.equalsIgnoreCase("N")) {
            throw new BusinessRuleException("Card Active Status must be Y or N");
        }

        validateExpiryDate(request.expirationDate());

        if (name.equals(card.getEmbossedName())
                && status.equalsIgnoreCase(card.getActiveStatus())
                && request.expirationDate().equals(card.getExpirationDate())) {
            throw new BusinessRuleException("No change detected with respect to values fetched.");
        }

        card.setEmbossedName(name);
        card.setActiveStatus(status.toUpperCase());
        card.setExpirationDate(request.expirationDate());
        return toSummary(cards.save(card));
    }

    private void validateExpiryDate(String expirationDate) {
        if (expirationDate == null || expirationDate.isBlank()) {
            throw new BusinessRuleException("Card expiry date must be supplied");
        }
        CobolDateValidator.Result result = CobolDateValidator.validate(expirationDate, "YYYY-MM-DD");
        if (!result.isValid()) {
            throw new BusinessRuleException("Invalid card expiry date");
        }
        int month = result.date().getMonthValue();
        if (month < 1 || month > 12) {
            throw new BusinessRuleException("Card expiry month must be between 1 and 12");
        }
        int year = result.date().getYear();
        if (year < 1950 || year > 2099) {
            throw new BusinessRuleException("Invalid card expiry year");
        }
    }

    private static void validateCardNumber(String cardNumber) {
        if (cardNumber == null || !CARD_NUMBER.matcher(cardNumber.trim()).matches()) {
            throw new BusinessRuleException("Card number if supplied must be a 16 digit number");
        }
    }

    private static Long validAccountId(Long accountId) {
        AccountService.validateAccountId(accountId);
        return accountId;
    }

    static CardSummary toSummary(Card card) {
        return new CardSummary(
                card.getCardNumber(),
                card.getAccountId(),
                card.getCvvCode(),
                card.getEmbossedName(),
                card.getExpirationDate(),
                card.getActiveStatus(),
                card.getVersion());
    }
}
