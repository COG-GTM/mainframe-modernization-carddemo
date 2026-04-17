package com.carddemo.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Maps to CARDDEMO-COMMAREA in COCOM01Y.cpy.
 *
 * <pre>
 * 01 CARDDEMO-COMMAREA.
 *   05 CDEMO-GENERAL-INFO    → GeneralInfo
 *   05 CDEMO-CUSTOMER-INFO   → CustomerInfo
 *   05 CDEMO-ACCOUNT-INFO    → AccountInfo
 *   05 CDEMO-CARD-INFO       → CardInfo
 *   05 CDEMO-MORE-INFO       → lastMap, lastMapset
 * </pre>
 */
public class CardDemoCommarea {

    @JsonProperty("generalInfo")
    private GeneralInfo generalInfo;

    @JsonProperty("customerInfo")
    private CustomerInfo customerInfo;

    @JsonProperty("accountInfo")
    private AccountInfo accountInfo;

    @JsonProperty("cardInfo")
    private CardInfo cardInfo;

    @JsonProperty("lastMap")
    private String lastMap;

    @JsonProperty("lastMapset")
    private String lastMapset;

    public CardDemoCommarea() {
    }

    public CardDemoCommarea(GeneralInfo generalInfo, CustomerInfo customerInfo,
                            AccountInfo accountInfo, CardInfo cardInfo,
                            String lastMap, String lastMapset) {
        this.generalInfo = generalInfo;
        this.customerInfo = customerInfo;
        this.accountInfo = accountInfo;
        this.cardInfo = cardInfo;
        this.lastMap = lastMap;
        this.lastMapset = lastMapset;
    }

    public GeneralInfo getGeneralInfo() {
        return generalInfo;
    }

    public void setGeneralInfo(GeneralInfo generalInfo) {
        this.generalInfo = generalInfo;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }

    public String getLastMap() {
        return lastMap;
    }

    public void setLastMap(String lastMap) {
        this.lastMap = lastMap;
    }

    public String getLastMapset() {
        return lastMapset;
    }

    public void setLastMapset(String lastMapset) {
        this.lastMapset = lastMapset;
    }
}
