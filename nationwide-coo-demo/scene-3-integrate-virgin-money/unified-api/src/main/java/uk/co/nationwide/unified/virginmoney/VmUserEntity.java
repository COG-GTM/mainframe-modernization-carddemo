package uk.co.nationwide.unified.virginmoney;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Virgin Money online-banking user record. Adapted from the
 * <code>User</code> entity in the upstream open-source online-banking
 * project. Mapped to a separate table prefix (<code>vm_*</code>) so both
 * source systems can coexist in the same Postgres instance for the demo.
 */
@Entity
@Table(name = "vm_user")
public class VmUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    private String username;
    private String firstName;
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;
    private String phone;
    private boolean enabled = true;

    @OneToOne
    @JoinColumn(name = "primary_account_id")
    private VmPrimaryAccountEntity primaryAccount;

    @OneToOne
    @JoinColumn(name = "savings_account_id")
    private VmSavingsAccountEntity savingsAccount;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public VmPrimaryAccountEntity getPrimaryAccount() { return primaryAccount; }
    public void setPrimaryAccount(VmPrimaryAccountEntity primaryAccount) { this.primaryAccount = primaryAccount; }
    public VmSavingsAccountEntity getSavingsAccount() { return savingsAccount; }
    public void setSavingsAccount(VmSavingsAccountEntity savingsAccount) { this.savingsAccount = savingsAccount; }
}
