namespace CardDemo.Batch.Cbact01c.Domain;

/// <summary>
/// Modern representation of the COBOL ACCOUNT-RECORD (copybook CVACT01Y, RECLN 300).
/// Monetary fields use <see cref="decimal"/> to preserve S9(10)V99 precision.
/// </summary>
public sealed record AccountRecord
{
    /// <summary>ACCT-ID PIC 9(11) — primary key.</summary>
    public required long AccountId { get; init; }

    /// <summary>ACCT-ACTIVE-STATUS PIC X(01).</summary>
    public required string ActiveStatus { get; init; }

    /// <summary>ACCT-CURR-BAL PIC S9(10)V99.</summary>
    public required decimal CurrentBalance { get; init; }

    /// <summary>ACCT-CREDIT-LIMIT PIC S9(10)V99.</summary>
    public required decimal CreditLimit { get; init; }

    /// <summary>ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99.</summary>
    public required decimal CashCreditLimit { get; init; }

    /// <summary>ACCT-OPEN-DATE PIC X(10) — YYYY-MM-DD.</summary>
    public required string OpenDate { get; init; }

    /// <summary>ACCT-EXPIRAION-DATE PIC X(10) — YYYY-MM-DD (spelling matches copybook).</summary>
    public required string ExpirationDate { get; init; }

    /// <summary>ACCT-REISSUE-DATE PIC X(10) — YYYY-MM-DD.</summary>
    public required string ReissueDate { get; init; }

    /// <summary>ACCT-CURR-CYC-CREDIT PIC S9(10)V99.</summary>
    public required decimal CurrentCycleCredit { get; init; }

    /// <summary>ACCT-CURR-CYC-DEBIT PIC S9(10)V99.</summary>
    public required decimal CurrentCycleDebit { get; init; }

    /// <summary>ACCT-ADDR-ZIP PIC X(10).</summary>
    public required string AddressZip { get; init; }

    /// <summary>ACCT-GROUP-ID PIC X(10).</summary>
    public required string GroupId { get; init; }
}
