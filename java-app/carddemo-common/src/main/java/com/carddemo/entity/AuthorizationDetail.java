package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for authorization detail (child of AuthorizationSummary).
 * Migrated from IMS HIDAM child segment.
 */
@Entity
@Table(name = "authorization_detail")
public class AuthorizationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_id", nullable = false)
    private AuthorizationSummary authorizationSummary;

    @Column(name = "detail_type", length = 20)
    private String detailType;

    @Column(name = "detail_amount", precision = 11, scale = 2)
    private BigDecimal detailAmount;

    @Column(name = "detail_timestamp")
    private LocalDateTime detailTimestamp;

    @Column(name = "detail_description", length = 100)
    private String detailDescription;

    public AuthorizationDetail() {}

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public AuthorizationSummary getAuthorizationSummary() { return authorizationSummary; }
    public void setAuthorizationSummary(AuthorizationSummary authorizationSummary) { this.authorizationSummary = authorizationSummary; }
    public String getDetailType() { return detailType; }
    public void setDetailType(String detailType) { this.detailType = detailType; }
    public BigDecimal getDetailAmount() { return detailAmount; }
    public void setDetailAmount(BigDecimal detailAmount) { this.detailAmount = detailAmount; }
    public LocalDateTime getDetailTimestamp() { return detailTimestamp; }
    public void setDetailTimestamp(LocalDateTime detailTimestamp) { this.detailTimestamp = detailTimestamp; }
    public String getDetailDescription() { return detailDescription; }
    public void setDetailDescription(String detailDescription) { this.detailDescription = detailDescription; }
}
