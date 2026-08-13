package com.carddemo.online.card;

import com.carddemo.model.entity.Card;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * COBOL program: COCRDLIC — browse of VSAM KSDS CARDDAT (record layout CVACT02Y),
 * keyed on CARD-NUM.
 *
 * <p>The list screen uses EXEC CICS STARTBR ... GTEQ followed by READNEXT (page down)
 * or READPREV (page up); these two queries are the SQL equivalent of that browse and
 * are not part of the shared {@code CardRepository}, which has no key range access.</p>
 */
@Repository
public interface CardBrowseRepository extends JpaRepository<Card, String> {

    /** STARTBR GTEQ followed by READNEXT. */
    List<Card> findByCardNumberGreaterThanEqualOrderByCardNumberAsc(String cardNumber, Pageable pageable);

    /** The READNEXT that follows one already positioned on {@code cardNumber}. */
    List<Card> findByCardNumberGreaterThanOrderByCardNumberAsc(String cardNumber, Pageable pageable);

    /** STARTBR GTEQ followed by READPREV, which first re-reads the start key itself. */
    List<Card> findByCardNumberLessThanOrderByCardNumberDesc(String cardNumber, Pageable pageable);
}
