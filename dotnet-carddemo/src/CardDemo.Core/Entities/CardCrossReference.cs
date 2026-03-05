namespace CardDemo.Core.Entities;

/// <summary>
/// Represents a card cross-reference record derived from COBOL copybook CVACT03Y.cpy (CARD-XREF-RECORD).
/// Record length: 50 bytes.
/// This is the central lookup table that links cards -> accounts -> customers.
/// </summary>
public class CardCrossReference
{
    /// <summary>
    /// Card number. Maps to XREF-CARD-NUM PIC X(16).
    /// </summary>
    public string CardNumber { get; set; } = string.Empty;

    /// <summary>
    /// Customer identifier. Maps to XREF-CUST-ID PIC 9(09).
    /// </summary>
    public long CustomerId { get; set; }

    /// <summary>
    /// Account identifier. Maps to XREF-ACCT-ID PIC 9(11).
    /// </summary>
    public long AccountId { get; set; }

    // Navigation properties
    public Account Account { get; set; } = null!;
    public Customer Customer { get; set; } = null!;
}
