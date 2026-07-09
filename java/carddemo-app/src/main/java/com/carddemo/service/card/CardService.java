package com.carddemo.service.card;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carddemo.domain.Card;
import com.carddemo.repository.CardRepository;
import com.carddemo.web.card.dto.CardDetailResponse;
import com.carddemo.web.card.dto.CardListResponse;
import com.carddemo.web.card.dto.CardSummaryResponse;
import com.carddemo.web.card.dto.CardUpdateRequest;
import com.carddemo.web.card.dto.CardUpdateResponse;

/**
 * Service port of the three online card programs — {@code COCRDLIC} (list), {@code COCRDSLC}
 * (detail) and {@code COCRDUPC} (update). It reproduces the COBOL input edits, the
 * paged-browse behaviour and the exact screen messages ({@link CardMessages}) on top of the
 * JPA {@link CardRepository}.
 *
 * <p>The legacy VSAM browse ({@code STARTBR}/{@code READNEXT}/{@code READPREV} keyed
 * ascending by {@code CARD-NUM}) is reproduced as an ordered, filtered, in-memory slice: the
 * card file is small, so candidates are loaded via the existing repository finders, sorted by
 * card number and paged {@value #PAGE_SIZE} rows at a time (COBOL {@code WS-MAX-SCREEN-LINES}).</p>
 */
@Service
public class CardService {

    /** {@code WS-MAX-SCREEN-LINES} — the card-list map shows 7 rows per page. */
    public static final int PAGE_SIZE = 7;

    private static final int ACCOUNT_ID_LENGTH = 11;
    private static final int CARD_NUM_LENGTH = 16;

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    // ---- COCRDLIC : list -------------------------------------------------------------

    /**
     * List cards with the COCRDLIC filter/paging behaviour.
     *
     * @param accountFilter optional 11-digit account filter ({@code CC-ACCT-ID}); blank/null
     *                      means no account restriction.
     * @param cardFilter    optional 16-digit card filter ({@code CC-CARD-NUM}); blank/null
     *                      means no card restriction.
     * @param page          zero-based page index.
     * @throws CardValidationException if a supplied filter fails its numeric-length edit.
     */
    @Transactional(readOnly = true)
    public CardListResponse listCards(String accountFilter, String cardFilter, int page) {
        String account = trimToNull(accountFilter);
        String card = trimToNull(cardFilter);

        // 1000-EDIT-MAPINPUTS: a supplied filter must be all-digits of the exact width.
        if (account != null && !isDigits(account, ACCOUNT_ID_LENGTH)) {
            throw new CardValidationException(CardMessages.ACCOUNT_FILTER_INVALID);
        }
        if (card != null && !isDigits(card, CARD_NUM_LENGTH)) {
            throw new CardValidationException(CardMessages.CARD_FILTER_INVALID);
        }

        int pageIndex = Math.max(page, 0);

        List<Card> ordered = selectCandidates(account, card).stream()
            .filter(c -> account == null || account.equals(c.getCardAcctId()))
            .filter(c -> card == null || card.equals(c.getCardNum()))
            .sorted(Comparator.comparing(Card::getCardNum))
            .toList();

        int total = ordered.size();
        int from = Math.min(pageIndex * PAGE_SIZE, total);
        int to = Math.min(from + PAGE_SIZE, total);
        List<CardSummaryResponse> rows = ordered.subList(from, to).stream()
            .map(CardSummaryResponse::from)
            .toList();

        boolean hasNext = to < total;
        boolean hasPrevious = pageIndex > 0;
        String message = rows.isEmpty() ? CardMessages.NO_RECORDS_FOUND : null;

        return new CardListResponse(pageIndex, PAGE_SIZE, rows, hasNext, hasPrevious, message);
    }

    /**
     * Choose the cheapest finder for the supplied filters, mirroring which VSAM key the
     * COBOL browse would ride: an exact card number reads a single record, an account filter
     * reads that account's cards, otherwise the whole file is scanned in key order.
     */
    private List<Card> selectCandidates(String account, String card) {
        if (card != null) {
            return cardRepository.findById(card).map(List::of).orElse(List.of());
        }
        if (account != null) {
            return cardRepository.findByCardAcctId(account);
        }
        return cardRepository.findAll();
    }

    // ---- COCRDSLC : detail -----------------------------------------------------------

    /**
     * Fetch one card's detail ({@code 9100-GETCARD-BYACCTCARD}).
     *
     * @param cardNumber    the 16-digit card number (read key {@code CARD-NUM}).
     * @param accountFilter optional 11-digit account; when supplied it must be non-zero and
     *                      match the card's account, reproducing the COCRDSLC account edit.
     * @throws CardValidationException if a key fails its edit.
     * @throws CardNotFoundException   if no card matches.
     */
    @Transactional(readOnly = true)
    public CardDetailResponse getCard(String cardNumber, String accountFilter) {
        String card = trimToNull(cardNumber);
        String account = trimToNull(accountFilter);

        // 2220-EDIT-CARD: card number required and a 16-digit number.
        if (card == null) {
            throw new CardValidationException(CardMessages.CARD_NOT_PROVIDED);
        }
        if (!isDigits(card, CARD_NUM_LENGTH)) {
            throw new CardValidationException(CardMessages.CARD_16_DIGIT);
        }
        // 2210-EDIT-ACCOUNT: if supplied, account must be a non-zero 11-digit number.
        if (account != null && (!isDigits(account, ACCOUNT_ID_LENGTH) || isZero(account))) {
            throw new CardValidationException(CardMessages.ACCOUNT_NON_ZERO_11);
        }

        Card found = cardRepository.findById(card)
            .filter(c -> account == null || account.equals(c.getCardAcctId()))
            .orElseThrow(() -> new CardNotFoundException(CardMessages.DID_NOT_FIND_CARDS));

        return CardDetailResponse.from(found);
    }

    // ---- COCRDUPC : update -----------------------------------------------------------

    /**
     * Apply an update to a card ({@code 1200-EDIT-MAP-INPUTS} then
     * {@code 9200-WRITE-PROCESSING}). Only the embossed name, active status and expiry
     * month/year are editable; the expiry day, CVV and account are carried unchanged from
     * the fetched record.
     *
     * @throws CardValidationException on a failed field edit or when nothing changed.
     * @throws CardNotFoundException   if the card does not exist.
     */
    @Transactional
    public CardUpdateResponse updateCard(String cardNumber, CardUpdateRequest request) {
        String card = trimToNull(cardNumber);
        if (card == null) {
            throw new CardValidationException(CardMessages.CARD_NOT_PROVIDED);
        }
        if (!isDigits(card, CARD_NUM_LENGTH)) {
            throw new CardValidationException(CardMessages.CARD_16_DIGIT);
        }

        Card existing = cardRepository.findById(card)
            .orElseThrow(() -> new CardNotFoundException(CardMessages.DID_NOT_FIND_CARDS));

        String name = request == null ? null : request.embossedName();
        String status = trimToNull(request == null ? null : request.activeStatus());
        String month = trimToNull(request == null ? null : request.expiryMonth());
        String year = trimToNull(request == null ? null : request.expiryYear());

        // 1230-EDIT-NAME: required, letters and spaces only.
        String trimmedName = name == null ? null : name.trim();
        if (trimmedName == null || trimmedName.isEmpty()) {
            throw new CardValidationException(CardMessages.NAME_NOT_PROVIDED);
        }
        if (!trimmedName.chars().allMatch(ch -> Character.isLetter(ch) || ch == ' ')) {
            throw new CardValidationException(CardMessages.NAME_MUST_BE_ALPHA);
        }
        // 1240-EDIT-CARDSTATUS: Y or N.
        if (!"Y".equals(status) && !"N".equals(status)) {
            throw new CardValidationException(CardMessages.STATUS_MUST_BE_YES_NO);
        }
        // 1250-EDIT-EXPIRY-MON: numeric 1..12.
        Integer monthValue = parseInt(month);
        if (monthValue == null || monthValue < 1 || monthValue > 12) {
            throw new CardValidationException(CardMessages.EXPIRY_MONTH_NOT_VALID);
        }
        // 1260-EDIT-EXPIRY-YEAR: numeric 1950..2099.
        Integer yearValue = parseInt(year);
        if (yearValue == null || yearValue < 1950 || yearValue > 2099) {
            throw new CardValidationException(CardMessages.EXPIRY_YEAR_NOT_VALID);
        }

        String day = expiryDay(existing.getCardExpirationDate());
        String newExpiration = String.format("%04d-%02d-%s", yearValue, monthValue, day);

        // NO-CHANGES-DETECTED: submitted values equal the fetched record (name compared
        // case-insensitively, as 9300-CHECK-CHANGE-IN-REC upper-cases the stored name).
        boolean nameSame = trimmedName.equalsIgnoreCase(safeTrim(existing.getCardEmbossedName()));
        boolean statusSame = status.equals(existing.getCardActiveStatus());
        boolean dateSame = newExpiration.equals(existing.getCardExpirationDate());
        if (nameSame && statusSame && dateSame) {
            throw new CardValidationException(CardMessages.NO_CHANGES_DETECTED);
        }

        existing.setCardEmbossedName(trimmedName);
        existing.setCardActiveStatus(status);
        existing.setCardExpirationDate(newExpiration);
        Card saved = cardRepository.save(existing);

        return new CardUpdateResponse(CardMessages.UPDATE_SUCCESS, CardDetailResponse.from(saved));
    }

    // ---- helpers ---------------------------------------------------------------------

    private static String expiryDay(String expirationDate) {
        if (expirationDate != null && expirationDate.length() >= 10) {
            String day = expirationDate.substring(8, 10);
            if (isDigits(day, 2)) {
                return day;
            }
        }
        return "01";
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isDigits(String value, int length) {
        if (value == null || value.length() != length) {
            return false;
        }
        return value.chars().allMatch(Character::isDigit);
    }

    private static boolean isZero(String value) {
        return value.chars().allMatch(ch -> ch == '0');
    }

    private static Integer parseInt(String value) {
        if (value == null || value.isEmpty() || !value.chars().allMatch(Character::isDigit)) {
            return null;
        }
        return Integer.parseInt(value);
    }
}
