package uk.co.nationwide.unified.nationwide;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Nationwide card-platform customer record, mapped from the CardDemo VSAM
 * copybook <code>CVCUS01Y.cpy</code> (CUSTOMER-RECORD, 500-byte fixed).
 */
@Entity
@Table(name = "nw_customer")
public class NwCustomerEntity {

    @Id
    @Column(name = "cust_id")
    private Long custId;

    @Column(name = "cust_first_name", length = 25)
    private String custFirstName;

    @Column(name = "cust_middle_name", length = 25)
    private String custMiddleName;

    @Column(name = "cust_last_name", length = 25)
    private String custLastName;

    @Column(name = "cust_addr_line_1", length = 50)
    private String custAddrLine1;

    @Column(name = "cust_addr_line_2", length = 50)
    private String custAddrLine2;

    @Column(name = "cust_addr_line_3", length = 50)
    private String custAddrLine3;

    @Column(name = "cust_addr_state_cd", length = 2)
    private String custAddrStateCd;

    @Column(name = "cust_addr_country_cd", length = 3)
    private String custAddrCountryCd;

    @Column(name = "cust_addr_zip", length = 10)
    private String custAddrZip;

    @Column(name = "cust_phone_num_1", length = 15)
    private String custPhoneNum1;

    @Column(name = "cust_phone_num_2", length = 15)
    private String custPhoneNum2;

    @Column(name = "cust_dob")
    private LocalDate custDob;

    @Column(name = "cust_fico_credit_score")
    private Integer custFicoCreditScore;

    public Long getCustId() { return custId; }
    public void setCustId(Long custId) { this.custId = custId; }
    public String getCustFirstName() { return custFirstName; }
    public void setCustFirstName(String custFirstName) { this.custFirstName = custFirstName; }
    public String getCustMiddleName() { return custMiddleName; }
    public void setCustMiddleName(String custMiddleName) { this.custMiddleName = custMiddleName; }
    public String getCustLastName() { return custLastName; }
    public void setCustLastName(String custLastName) { this.custLastName = custLastName; }
    public String getCustAddrLine1() { return custAddrLine1; }
    public void setCustAddrLine1(String custAddrLine1) { this.custAddrLine1 = custAddrLine1; }
    public String getCustAddrLine2() { return custAddrLine2; }
    public void setCustAddrLine2(String custAddrLine2) { this.custAddrLine2 = custAddrLine2; }
    public String getCustAddrLine3() { return custAddrLine3; }
    public void setCustAddrLine3(String custAddrLine3) { this.custAddrLine3 = custAddrLine3; }
    public String getCustAddrStateCd() { return custAddrStateCd; }
    public void setCustAddrStateCd(String custAddrStateCd) { this.custAddrStateCd = custAddrStateCd; }
    public String getCustAddrCountryCd() { return custAddrCountryCd; }
    public void setCustAddrCountryCd(String custAddrCountryCd) { this.custAddrCountryCd = custAddrCountryCd; }
    public String getCustAddrZip() { return custAddrZip; }
    public void setCustAddrZip(String custAddrZip) { this.custAddrZip = custAddrZip; }
    public String getCustPhoneNum1() { return custPhoneNum1; }
    public void setCustPhoneNum1(String custPhoneNum1) { this.custPhoneNum1 = custPhoneNum1; }
    public String getCustPhoneNum2() { return custPhoneNum2; }
    public void setCustPhoneNum2(String custPhoneNum2) { this.custPhoneNum2 = custPhoneNum2; }
    public LocalDate getCustDob() { return custDob; }
    public void setCustDob(LocalDate custDob) { this.custDob = custDob; }
    public Integer getCustFicoCreditScore() { return custFicoCreditScore; }
    public void setCustFicoCreditScore(Integer custFicoCreditScore) { this.custFicoCreditScore = custFicoCreditScore; }
}
