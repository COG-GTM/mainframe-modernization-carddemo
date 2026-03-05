package com.carddemo.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO for account update requests.
 * Includes Jakarta Bean Validation annotations mirroring COACTUPC.cbl field validation.
 */
public class AccountUpdateRequest {

    @NotNull(message = "Active status is required")
    @Pattern(regexp = "[YN]", message = "Active status must be Y or N")
    private String activeStatus;

    @Digits(integer = 10, fraction = 2, message = "Credit limit must have at most 10 integer digits and 2 decimal places")
    private BigDecimal creditLimit;

    @Digits(integer = 10, fraction = 2, message = "Cash credit limit must have at most 10 integer digits and 2 decimal places")
    private BigDecimal cashCreditLimit;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Open date must be in YYYY-MM-DD format")
    private String openDate;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Expiration date must be in YYYY-MM-DD format")
    private String expirationDate;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Reissue date must be in YYYY-MM-DD format")
    private String reissueDate;

    @Size(max = 10, message = "Address ZIP must be at most 10 characters")
    private String addrZip;

    @Size(max = 10, message = "Group ID must be at most 10 characters")
    private String groupId;

    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    public BigDecimal getCashCreditLimit() { return cashCreditLimit; }
    public void setCashCreditLimit(BigDecimal cashCreditLimit) { this.cashCreditLimit = cashCreditLimit; }
    public String getOpenDate() { return openDate; }
    public void setOpenDate(String openDate) { this.openDate = openDate; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public String getReissueDate() { return reissueDate; }
    public void setReissueDate(String reissueDate) { this.reissueDate = reissueDate; }
    public String getAddrZip() { return addrZip; }
    public void setAddrZip(String addrZip) { this.addrZip = addrZip; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
}
