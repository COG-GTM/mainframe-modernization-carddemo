package com.carddemo.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity mapped from COBOL copybook CVACT02Y.cpy — CARD-RECORD (150 bytes).
 */
@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @Column(name = "card_number", length = 16)
    private String cardNumber;

    @Column(name = "acct_id")
    private Long accountId;

    @Column(name = "cvv_code")
    private Integer cvvCode;

    @Column(name = "embossed_name", length = 50)
    private String embossedName;

    @Column(name = "expiration_date", length = 10)
    private String expirationDate;

    @Column(name = "active_status", length = 1)
    private String activeStatus;
}
