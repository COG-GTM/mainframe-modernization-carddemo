package com.carddemo.online.transaction;

import com.carddemo.model.dto.CardDemoCommarea;
import com.carddemo.online.transaction.dto.TransactionAddRequest;
import com.carddemo.online.transaction.dto.TransactionAddResponse;
import com.carddemo.online.transaction.dto.TransactionListRequest;
import com.carddemo.online.transaction.dto.TransactionListResponse;
import com.carddemo.online.transaction.dto.TransactionViewResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * COBOL programs: COTRN00C (CT00, list), COTRN01C (CT01, view) and COTRN02C (CT02, add).
 *
 * <p>Each 3270 interaction becomes one request. The browse position that CICS kept in the
 * CDEMO-CT00-INFO part of the COMMAREA lives in the HTTP session, next to the COCOM01Y
 * {@link CardDemoCommarea}, so paging survives requests exactly as the pseudo-conversational
 * program expects. Screen errors keep their COBOL text and are returned with a 400 status.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionListService listService;
    private final TransactionViewService viewService;
    private final TransactionAddService addService;

    public TransactionController(
            TransactionListService listService,
            TransactionViewService viewService,
            TransactionAddService addService) {
        this.listService = listService;
        this.viewService = viewService;
        this.addService = addService;
    }

    /** COTRN00C — ENTER, PF7 and PF8 on map COTRN0A. */
    @PostMapping("/list")
    public ResponseEntity<TransactionListResponse> list(
            @RequestBody(required = false) TransactionListRequest request, HttpSession session) {

        TransactionListRequest effective = request == null ? new TransactionListRequest() : request;
        TransactionListState state = listState(session);
        TransactionListResponse response = listService.handle(effective, state);
        session.setAttribute(TransactionListState.SESSION_KEY, state);

        if (response.getNextProgram() != null) {
            // XCTL to COTRN01C carries the selected transaction id in the COMMAREA.
            commarea(session).setToProgram(response.getNextProgram());
        }
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    /** COTRN01C — a transaction id typed on map COTRN1A, or handed over by CT00. */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionViewResponse> view(@PathVariable String transactionId) {
        TransactionViewResponse response = viewService.view(transactionId);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    /** COTRN02C — ENTER on map COTRN2A, including the CONFIRM field. */
    @PostMapping
    public ResponseEntity<TransactionAddResponse> add(@RequestBody TransactionAddRequest request) {
        TransactionAddResponse response = addService.add(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    private TransactionListState listState(HttpSession session) {
        Object existing = session.getAttribute(TransactionListState.SESSION_KEY);
        if (existing instanceof TransactionListState state) {
            return state;
        }
        return new TransactionListState();
    }

    private CardDemoCommarea commarea(HttpSession session) {
        Object existing = session.getAttribute(CardDemoCommarea.SESSION_KEY);
        if (existing instanceof CardDemoCommarea commarea) {
            return commarea;
        }
        CardDemoCommarea commarea = new CardDemoCommarea();
        session.setAttribute(CardDemoCommarea.SESSION_KEY, commarea);
        return commarea;
    }
}
