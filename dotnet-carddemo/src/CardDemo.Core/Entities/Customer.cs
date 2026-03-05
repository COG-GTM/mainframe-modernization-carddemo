namespace CardDemo.Core.Entities;

/// <summary>
/// Represents a customer record derived from COBOL copybook CVCUS01Y.cpy (CUSTOMER-RECORD).
/// Record length: 500 bytes.
/// </summary>
public class Customer
{
    /// <summary>
    /// Customer identifier. Maps to CUST-ID PIC 9(09).
    /// </summary>
    public long CustomerId { get; set; }

    /// <summary>
    /// Customer first name. Maps to CUST-FIRST-NAME PIC X(25).
    /// </summary>
    public string FirstName { get; set; } = string.Empty;

    /// <summary>
    /// Customer middle name. Maps to CUST-MIDDLE-NAME PIC X(25).
    /// </summary>
    public string MiddleName { get; set; } = string.Empty;

    /// <summary>
    /// Customer last name. Maps to CUST-LAST-NAME PIC X(25).
    /// </summary>
    public string LastName { get; set; } = string.Empty;

    /// <summary>
    /// Address line 1. Maps to CUST-ADDR-LINE-1 PIC X(50).
    /// </summary>
    public string AddressLine1 { get; set; } = string.Empty;

    /// <summary>
    /// Address line 2. Maps to CUST-ADDR-LINE-2 PIC X(50).
    /// </summary>
    public string AddressLine2 { get; set; } = string.Empty;

    /// <summary>
    /// Address line 3. Maps to CUST-ADDR-LINE-3 PIC X(50).
    /// </summary>
    public string AddressLine3 { get; set; } = string.Empty;

    /// <summary>
    /// State code. Maps to CUST-ADDR-STATE-CD PIC X(02).
    /// </summary>
    public string State { get; set; } = string.Empty;

    /// <summary>
    /// Country code. Maps to CUST-ADDR-COUNTRY-CD PIC X(03).
    /// </summary>
    public string CountryCode { get; set; } = string.Empty;

    /// <summary>
    /// ZIP code. Maps to CUST-ADDR-ZIP PIC X(10).
    /// </summary>
    public string ZipCode { get; set; } = string.Empty;

    /// <summary>
    /// Phone number 1. Maps to CUST-PHONE-NUM-1 PIC X(15).
    /// </summary>
    public string PhoneNumber1 { get; set; } = string.Empty;

    /// <summary>
    /// Phone number 2. Maps to CUST-PHONE-NUM-2 PIC X(15).
    /// </summary>
    public string PhoneNumber2 { get; set; } = string.Empty;

    /// <summary>
    /// Social Security Number. Maps to CUST-SSN PIC 9(09).
    /// </summary>
    public string Ssn { get; set; } = string.Empty;

    /// <summary>
    /// Government-issued ID. Maps to CUST-GOVT-ISSUED-ID PIC X(20).
    /// </summary>
    public string GovtIssuedId { get; set; } = string.Empty;

    /// <summary>
    /// Date of birth. Maps to CUST-DOB-YYYY-MM-DD PIC X(10).
    /// </summary>
    public string DateOfBirth { get; set; } = string.Empty;

    /// <summary>
    /// EFT account identifier. Maps to CUST-EFT-ACCOUNT-ID PIC X(10).
    /// </summary>
    public string EftAccountId { get; set; } = string.Empty;

    /// <summary>
    /// Primary card holder indicator. Maps to CUST-PRI-CARD-HOLDER-IND PIC X(01).
    /// </summary>
    public string PrimaryCardHolderIndicator { get; set; } = string.Empty;

    /// <summary>
    /// FICO credit score. Maps to CUST-FICO-CREDIT-SCORE PIC 9(03).
    /// </summary>
    public int FicoScore { get; set; }

    // Navigation properties
    public ICollection<CardCrossReference> CrossReferences { get; set; } = new List<CardCrossReference>();
}
