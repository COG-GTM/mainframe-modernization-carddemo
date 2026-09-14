package com.carddemo.acctupdate.api;

public class AccountUpdateDetails {
    private String acctId;
    private String activeStatus;
    private String currBal;
    private String creditLimit;
    private String cashCreditLimit;
    private String currCycCredit;
    private String currCycDebit;
    private String openYear;
    private String openMon;
    private String openDay;
    private String expYear;
    private String expMon;
    private String expDay;
    private String reissueYear;
    private String reissueMon;
    private String reissueDay;
    private String groupId;
    private String custId;
    private String ssn1;
    private String ssn2;
    private String ssn3;
    private String dobYear;
    private String dobMon;
    private String dobDay;
    private String ficoScore;
    private String firstName;
    private String middleName;
    private String lastName;
    private String addrLine1;
    private String addrLine2;
    private String addrLine3;
    private String addrStateCd;
    private String addrCountryCd;
    private String addrZip;
    private String phone1A;
    private String phone1B;
    private String phone1C;
    private String phone2A;
    private String phone2B;
    private String phone2C;
    private String govtIssuedId;
    private String eftAccountId;
    private String priHolderInd;
    private String cardNum;

    public AccountUpdateDetails copy() {
        AccountUpdateDetails copy = new AccountUpdateDetails();
        copy.setAcctId(acctId);
        copy.setActiveStatus(activeStatus);
        copy.setCurrBal(currBal);
        copy.setCreditLimit(creditLimit);
        copy.setCashCreditLimit(cashCreditLimit);
        copy.setCurrCycCredit(currCycCredit);
        copy.setCurrCycDebit(currCycDebit);
        copy.setOpenYear(openYear);
        copy.setOpenMon(openMon);
        copy.setOpenDay(openDay);
        copy.setExpYear(expYear);
        copy.setExpMon(expMon);
        copy.setExpDay(expDay);
        copy.setReissueYear(reissueYear);
        copy.setReissueMon(reissueMon);
        copy.setReissueDay(reissueDay);
        copy.setGroupId(groupId);
        copy.setCustId(custId);
        copy.setSsn1(ssn1);
        copy.setSsn2(ssn2);
        copy.setSsn3(ssn3);
        copy.setDobYear(dobYear);
        copy.setDobMon(dobMon);
        copy.setDobDay(dobDay);
        copy.setFicoScore(ficoScore);
        copy.setFirstName(firstName);
        copy.setMiddleName(middleName);
        copy.setLastName(lastName);
        copy.setAddrLine1(addrLine1);
        copy.setAddrLine2(addrLine2);
        copy.setAddrLine3(addrLine3);
        copy.setAddrStateCd(addrStateCd);
        copy.setAddrCountryCd(addrCountryCd);
        copy.setAddrZip(addrZip);
        copy.setPhone1A(phone1A);
        copy.setPhone1B(phone1B);
        copy.setPhone1C(phone1C);
        copy.setPhone2A(phone2A);
        copy.setPhone2B(phone2B);
        copy.setPhone2C(phone2C);
        copy.setGovtIssuedId(govtIssuedId);
        copy.setEftAccountId(eftAccountId);
        copy.setPriHolderInd(priHolderInd);
        copy.setCardNum(cardNum);
        return copy;
    }

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getCurrBal() {
        return currBal;
    }

    public void setCurrBal(String currBal) {
        this.currBal = currBal;
    }

    public String getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(String creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getCashCreditLimit() {
        return cashCreditLimit;
    }

    public void setCashCreditLimit(String cashCreditLimit) {
        this.cashCreditLimit = cashCreditLimit;
    }

    public String getCurrCycCredit() {
        return currCycCredit;
    }

    public void setCurrCycCredit(String currCycCredit) {
        this.currCycCredit = currCycCredit;
    }

    public String getCurrCycDebit() {
        return currCycDebit;
    }

    public void setCurrCycDebit(String currCycDebit) {
        this.currCycDebit = currCycDebit;
    }

    public String getOpenYear() {
        return openYear;
    }

    public void setOpenYear(String openYear) {
        this.openYear = openYear;
    }

    public String getOpenMon() {
        return openMon;
    }

    public void setOpenMon(String openMon) {
        this.openMon = openMon;
    }

    public String getOpenDay() {
        return openDay;
    }

    public void setOpenDay(String openDay) {
        this.openDay = openDay;
    }

    public String getExpYear() {
        return expYear;
    }

    public void setExpYear(String expYear) {
        this.expYear = expYear;
    }

    public String getExpMon() {
        return expMon;
    }

    public void setExpMon(String expMon) {
        this.expMon = expMon;
    }

    public String getExpDay() {
        return expDay;
    }

    public void setExpDay(String expDay) {
        this.expDay = expDay;
    }

    public String getReissueYear() {
        return reissueYear;
    }

    public void setReissueYear(String reissueYear) {
        this.reissueYear = reissueYear;
    }

    public String getReissueMon() {
        return reissueMon;
    }

    public void setReissueMon(String reissueMon) {
        this.reissueMon = reissueMon;
    }

    public String getReissueDay() {
        return reissueDay;
    }

    public void setReissueDay(String reissueDay) {
        this.reissueDay = reissueDay;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getSsn1() {
        return ssn1;
    }

    public void setSsn1(String ssn1) {
        this.ssn1 = ssn1;
    }

    public String getSsn2() {
        return ssn2;
    }

    public void setSsn2(String ssn2) {
        this.ssn2 = ssn2;
    }

    public String getSsn3() {
        return ssn3;
    }

    public void setSsn3(String ssn3) {
        this.ssn3 = ssn3;
    }

    public String getDobYear() {
        return dobYear;
    }

    public void setDobYear(String dobYear) {
        this.dobYear = dobYear;
    }

    public String getDobMon() {
        return dobMon;
    }

    public void setDobMon(String dobMon) {
        this.dobMon = dobMon;
    }

    public String getDobDay() {
        return dobDay;
    }

    public void setDobDay(String dobDay) {
        this.dobDay = dobDay;
    }

    public String getFicoScore() {
        return ficoScore;
    }

    public void setFicoScore(String ficoScore) {
        this.ficoScore = ficoScore;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddrLine1() {
        return addrLine1;
    }

    public void setAddrLine1(String addrLine1) {
        this.addrLine1 = addrLine1;
    }

    public String getAddrLine2() {
        return addrLine2;
    }

    public void setAddrLine2(String addrLine2) {
        this.addrLine2 = addrLine2;
    }

    public String getAddrLine3() {
        return addrLine3;
    }

    public void setAddrLine3(String addrLine3) {
        this.addrLine3 = addrLine3;
    }

    public String getAddrStateCd() {
        return addrStateCd;
    }

    public void setAddrStateCd(String addrStateCd) {
        this.addrStateCd = addrStateCd;
    }

    public String getAddrCountryCd() {
        return addrCountryCd;
    }

    public void setAddrCountryCd(String addrCountryCd) {
        this.addrCountryCd = addrCountryCd;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getPhone1A() {
        return phone1A;
    }

    public void setPhone1A(String phone1A) {
        this.phone1A = phone1A;
    }

    public String getPhone1B() {
        return phone1B;
    }

    public void setPhone1B(String phone1B) {
        this.phone1B = phone1B;
    }

    public String getPhone1C() {
        return phone1C;
    }

    public void setPhone1C(String phone1C) {
        this.phone1C = phone1C;
    }

    public String getPhone2A() {
        return phone2A;
    }

    public void setPhone2A(String phone2A) {
        this.phone2A = phone2A;
    }

    public String getPhone2B() {
        return phone2B;
    }

    public void setPhone2B(String phone2B) {
        this.phone2B = phone2B;
    }

    public String getPhone2C() {
        return phone2C;
    }

    public void setPhone2C(String phone2C) {
        this.phone2C = phone2C;
    }

    public String getGovtIssuedId() {
        return govtIssuedId;
    }

    public void setGovtIssuedId(String govtIssuedId) {
        this.govtIssuedId = govtIssuedId;
    }

    public String getEftAccountId() {
        return eftAccountId;
    }

    public void setEftAccountId(String eftAccountId) {
        this.eftAccountId = eftAccountId;
    }

    public String getPriHolderInd() {
        return priHolderInd;
    }

    public void setPriHolderInd(String priHolderInd) {
        this.priHolderInd = priHolderInd;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }
}
