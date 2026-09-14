package com.carddemo.acctupdate.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CUSTDAT")
public class CustomerRecord {
    @Id private Long custId;
    @Column(length = 25) private String firstName;
    @Column(length = 25) private String middleName;
    @Column(length = 25) private String lastName;
    @Column(length = 50) private String addrLine1;
    @Column(length = 50) private String addrLine2;
    @Column(length = 50) private String addrLine3;
    @Column(length = 2) private String addrStateCd;
    @Column(length = 3) private String addrCountryCd;
    @Column(length = 10) private String addrZip;
    @Column(length = 15) private String phoneNum1;
    @Column(length = 15) private String phoneNum2;
    private Long ssn;
    @Column(length = 20) private String govtIssuedId;
    @Column(length = 10) private String dobYyyyMmDd;
    @Column(length = 10) private String eftAccountId;
    @Column(length = 1) private String priCardHolderInd;
    private Integer ficoCreditScore;

    public Long getCustId() { return custId; } public void setCustId(Long v) { custId = v; }
    public String getFirstName() { return firstName; } public void setFirstName(String v) { firstName = v; }
    public String getMiddleName() { return middleName; } public void setMiddleName(String v) { middleName = v; }
    public String getLastName() { return lastName; } public void setLastName(String v) { lastName = v; }
    public String getAddrLine1() { return addrLine1; } public void setAddrLine1(String v) { addrLine1 = v; }
    public String getAddrLine2() { return addrLine2; } public void setAddrLine2(String v) { addrLine2 = v; }
    public String getAddrLine3() { return addrLine3; } public void setAddrLine3(String v) { addrLine3 = v; }
    public String getAddrStateCd() { return addrStateCd; } public void setAddrStateCd(String v) { addrStateCd = v; }
    public String getAddrCountryCd() { return addrCountryCd; } public void setAddrCountryCd(String v) { addrCountryCd = v; }
    public String getAddrZip() { return addrZip; } public void setAddrZip(String v) { addrZip = v; }
    public String getPhoneNum1() { return phoneNum1; } public void setPhoneNum1(String v) { phoneNum1 = v; }
    public String getPhoneNum2() { return phoneNum2; } public void setPhoneNum2(String v) { phoneNum2 = v; }
    public Long getSsn() { return ssn; } public void setSsn(Long v) { ssn = v; }
    public String getGovtIssuedId() { return govtIssuedId; } public void setGovtIssuedId(String v) { govtIssuedId = v; }
    public String getDobYyyyMmDd() { return dobYyyyMmDd; } public void setDobYyyyMmDd(String v) { dobYyyyMmDd = v; }
    public String getEftAccountId() { return eftAccountId; } public void setEftAccountId(String v) { eftAccountId = v; }
    public String getPriCardHolderInd() { return priCardHolderInd; } public void setPriCardHolderInd(String v) { priCardHolderInd = v; }
    public Integer getFicoCreditScore() { return ficoCreditScore; } public void setFicoCreditScore(Integer v) { ficoCreditScore = v; }
}
