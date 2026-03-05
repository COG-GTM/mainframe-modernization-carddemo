namespace CardDemo.Core.Entities;

/// <summary>
/// Represents an account record derived from COBOL copybook CVACT01Y.cpy (ACCOUNT-RECORD).
/// Record length: 300 bytes.
/// </summary>
public class Account
{
    /// <summary>
    /// Account identifier. Maps to ACCT-ID PIC 9(11).
    /// </summary>
    public long AccountId { get; set; }

    /// <summary>
    /// Account active status. Maps to ACCT-ACTIVE-STATUS PIC X(01).
    /// </summary>
    public string AccountStatus { get; set; } = string.Empty;

    /// <summary>
    /// Current balance. Maps to ACCT-CURR-BAL PIC S9(10)V99.
    /// </summary>
    public decimal CurrentBalance { get; set; }

    /// <summary>
    /// Credit limit. Maps to ACCT-CREDIT-LIMIT PIC S9(10)V99.
    /// </summary>
    public decimal CreditLimit { get; set; }

    /// <summary>
    /// Cash credit limit. Maps to ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99.
    /// </summary>
    public decimal CashCreditLimit { get; set; }

    /// <summary>
    /// Account open date. Maps to ACCT-OPEN-DATE PIC X(10).
    /// </summary>
    public string OpenDate { get; set; } = string.Empty;

    /// <summary>
    /// Account expiration date. Maps to ACCT-EXPIRAION-DATE PIC X(10).
    /// </summary>
    public string ExpirationDate { get; set; } = string.Empty;

    /// <summary>
    /// Account reissue date. Maps to ACCT-REISSUE-DATE PIC X(10).
    /// </summary>
    public string ReissueDate { get; set; } = string.Empty;

    /// <summary>
    /// Current cycle credit total. Maps to ACCT-CURR-CYC-CREDIT PIC S9(10)V99.
    /// </summary>
    public decimal CurrentCycleCredit { get; set; }

    /// <summary>
    /// Current cycle debit total. Maps to ACCT-CURR-CYC-DEBIT PIC S9(10)V99.
    /// </summary>
    public decimal CurrentCycleDebit { get; set; }

    /// <summary>
    /// Address ZIP code. Maps to ACCT-ADDR-ZIP PIC X(10).
    /// </summary>
    public string AddressZip { get; set; } = string.Empty;

    /// <summary>
    /// Group identifier. Maps to ACCT-GROUP-ID PIC X(10).
    /// </summary>
    public string GroupId { get; set; } = string.Empty;

    // Navigation properties
    public ICollection<CardData> Cards { get; set; } = new List<CardData>();
    public ICollection<CardCrossReference> CrossReferences { get; set; } = new List<CardCrossReference>();
}
