package com.carddemo.transaction.dto;

import java.util.List;

public class TransactionListResponse {

    private List<TransactionDto> transactions;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public TransactionListResponse() {
    }

    public TransactionListResponse(List<TransactionDto> transactions, int page, int size,
                                   long totalElements, int totalPages) {
        this.transactions = transactions;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionDto> transactions) {
        this.transactions = transactions;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
