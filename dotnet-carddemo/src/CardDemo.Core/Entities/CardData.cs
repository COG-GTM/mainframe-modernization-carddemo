namespace CardDemo.Core.Entities;

/// <summary>
/// Represents a card record derived from COBOL copybook CVACT02Y.cpy (CARD-RECORD).
/// Record length: 150 bytes.
/// </summary>
public class CardData
{
    /// <summary>
    /// Card number. Maps to CARD-NUM PIC X(16).
    /// </summary>
    public string CardNumber { get; set; } = string.Empty;

    /// <summary>
    /// Associated account identifier. Maps to CARD-ACCT-ID PIC 9(11).
    /// </summary>
    public long AccountId { get; set; }

    /// <summary>
    /// Card verification value code. Maps to CARD-CVV-CD PIC 9(03).
    /// </summary>
    public int CvvCode { get; set; }

    /// <summary>
    /// Embossed name on the card. Maps to CARD-EMBOSSED-NAME PIC X(50).
    /// </summary>
    public string EmbossedName { get; set; } = string.Empty;

    /// <summary>
    /// Card expiration date. Maps to CARD-EXPIRAION-DATE PIC X(10).
    /// </summary>
    public string ExpirationDate { get; set; } = string.Empty;

    /// <summary>
    /// Card active status. Maps to CARD-ACTIVE-STATUS PIC X(01).
    /// </summary>
    public string CardStatus { get; set; } = string.Empty;

    // Navigation property
    public Account Account { get; set; } = null!;
}
