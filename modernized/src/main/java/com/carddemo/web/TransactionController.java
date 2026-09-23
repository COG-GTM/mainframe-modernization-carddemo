package com.carddemo.web;

import com.carddemo.service.TransactionService;
import com.carddemo.web.dto.PageResponse;
import com.carddemo.web.dto.TransactionAddRequest;
import com.carddemo.web.dto.TransactionView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** COTRN00C (list), COTRN01C (view) and COTRN02C (add). */
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public PageResponse<TransactionView> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return transactionService.list(page, size);
    }

    @GetMapping("/{transactionId}")
    public TransactionView view(@PathVariable String transactionId) {
        return transactionService.view(transactionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionView add(@RequestBody TransactionAddRequest request) {
        return transactionService.add(request);
    }
}
