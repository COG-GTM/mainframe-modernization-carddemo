package com.carddemo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for card update requests.
 * Mirrors COCRDUPC.cbl update fields.
 */
public class CardUpdateRequest {

    @Size(max = 50, message = "Embossed name must be at most 50 characters")
    private String embossedName;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Expiration date must be in YYYY-MM-DD format")
    private String expirationDate;

    @Pattern(regexp = "[YN]", message = "Active status must be Y or N")
    private String activeStatus;

    public String getEmbossedName() { return embossedName; }
    public void setEmbossedName(String embossedName) { this.embossedName = embossedName; }
    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
    public String getActiveStatus() { return activeStatus; }
    public void setActiveStatus(String activeStatus) { this.activeStatus = activeStatus; }
}
