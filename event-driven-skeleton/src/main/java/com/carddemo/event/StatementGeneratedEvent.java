package com.carddemo.event;

/**
 * Event published after a customer statement is generated.
 *
 * Replaces: writing to STATEMNT.PS (plain text) and STATEMNT.HTML
 * output files by CBSTM03A.
 *
 * Published to the "statement.generated" Kafka topic.
 * Downstream consumers can persist, email, or archive the statement.
 */
public record StatementGeneratedEvent(
    /** Account ID for which the statement was generated */
    long accountId,
    /** Customer ID */
    long customerId,
    /** Card number */
    String cardNumber,
    /** Plain-text statement content (replaces STATEMNT.PS output) */
    String textStatement,
    /** HTML statement content (replaces STATEMNT.HTML output) */
    String htmlStatement,
    /** Number of transactions included in the statement */
    int transactionCount,
    /** Total expense amount across all transactions */
    String totalExpense
) {}
