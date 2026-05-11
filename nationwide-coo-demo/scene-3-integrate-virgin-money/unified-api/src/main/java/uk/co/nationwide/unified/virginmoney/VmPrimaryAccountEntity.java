package uk.co.nationwide.unified.virginmoney;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Virgin Money primary (current) account. Adapted from the
 * <code>PrimaryAccount</code> entity in the upstream online-banking project.
 */
@Entity
@Table(name = "vm_primary_account")
public class VmPrimaryAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "account_number")
    private int accountNumber;

    @Column(name = "account_balance", precision = 12, scale = 2)
    private BigDecimal accountBalance;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getAccountNumber() { return accountNumber; }
    public void setAccountNumber(int accountNumber) { this.accountNumber = accountNumber; }
    public BigDecimal getAccountBalance() { return accountBalance; }
    public void setAccountBalance(BigDecimal accountBalance) { this.accountBalance = accountBalance; }
}
