package com.carddemo.dto;

public class MenuOption {
    private int number;
    private String transactionCode;
    private String description;
    public MenuOption() {}
    public MenuOption(int number, String transactionCode, String description) {
        this.number = number; this.transactionCode = transactionCode; this.description = description;
    }
    public int getNumber() { return number; }
    public void setNumber(int v) { this.number = v; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String v) { this.transactionCode = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
}
