package com.carddemo.statement.dto;

/**
 * Response DTO containing the generated statement in both text and HTML formats.
 * Mirrors CBSTM03A's dual-output approach: STMT-FILE (plain text) and HTML-FILE.
 */
public class StatementResponse {

    private String accountId;
    private String statementPeriod;
    private String textStatement;
    private String htmlStatement;

    public StatementResponse() {
    }

    public StatementResponse(String accountId, String statementPeriod,
                             String textStatement, String htmlStatement) {
        this.accountId = accountId;
        this.statementPeriod = statementPeriod;
        this.textStatement = textStatement;
        this.htmlStatement = htmlStatement;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getStatementPeriod() {
        return statementPeriod;
    }

    public void setStatementPeriod(String statementPeriod) {
        this.statementPeriod = statementPeriod;
    }

    public String getTextStatement() {
        return textStatement;
    }

    public void setTextStatement(String textStatement) {
        this.textStatement = textStatement;
    }

    public String getHtmlStatement() {
        return htmlStatement;
    }

    public void setHtmlStatement(String htmlStatement) {
        this.htmlStatement = htmlStatement;
    }
}
