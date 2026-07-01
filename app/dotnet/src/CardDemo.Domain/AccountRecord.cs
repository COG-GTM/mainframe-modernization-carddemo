namespace CardDemo.Domain;

/// <summary>
/// Modern representation of the CardDemo account entity.
/// Mirrors the COBOL copybook CVACT01Y (ACCOUNT-RECORD, RECLN 300).
/// </summary>
public sealed class AccountRecord
{
    /// <summary>ACCT-ID PIC 9(11) — primary key.</summary>
    public long AccountId { get; set; }

    /// <summary>ACCT-ACTIVE-STATUS PIC X(01).</summary>
    public string ActiveStatus { get; set; } = string.Empty;

    /// <summary>ACCT-CURR-BAL PIC S9(10)V99.</summary>
    public decimal CurrentBalance { get; set; }

    /// <summary>ACCT-CREDIT-LIMIT PIC S9(10)V99.</summary>
    public decimal CreditLimit { get; set; }

    /// <summary>ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99.</summary>
    public decimal CashCreditLimit { get; set; }

    /// <summary>ACCT-OPEN-DATE PIC X(10).</summary>
    public string OpenDate { get; set; } = string.Empty;

    /// <summary>ACCT-EXPIRAION-DATE PIC X(10) (spelling preserved from copybook).</summary>
    public string ExpirationDate { get; set; } = string.Empty;

    /// <summary>ACCT-REISSUE-DATE PIC X(10).</summary>
    public string ReissueDate { get; set; } = string.Empty;

    /// <summary>ACCT-CURR-CYC-CREDIT PIC S9(10)V99.</summary>
    public decimal CurrentCycleCredit { get; set; }

    /// <summary>ACCT-CURR-CYC-DEBIT PIC S9(10)V99.</summary>
    public decimal CurrentCycleDebit { get; set; }

    /// <summary>ACCT-ADDR-ZIP PIC X(10).</summary>
    public string AddressZip { get; set; } = string.Empty;

    /// <summary>ACCT-GROUP-ID PIC X(10).</summary>
    public string GroupId { get; set; } = string.Empty;
}
