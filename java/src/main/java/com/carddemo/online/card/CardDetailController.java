package com.carddemo.online.card;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.online.account.OnlineSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * COBOL program: COCRDSLC — transaction CCDL (Credit Card Detail).
 */
@RestController
@RequestMapping("/api/cards")
public class CardDetailController {

    private final CardDetailService cardDetailService;

    public CardDetailController(CardDetailService cardDetailService) {
        this.cardDetailService = cardDetailService;
    }

    @PostMapping("/detail")
    public ResponseEntity<CardDetailResponse> detail(@RequestBody CardDetailRequest request, HttpSession session) {
        CardDemoCommarea commarea = OnlineSession.commarea(session);
        CardDetailResponse response = cardDetailService.view(request, commarea);
        OnlineSession.store(session, commarea);
        return ResponseEntity.ok(response);
    }
}
