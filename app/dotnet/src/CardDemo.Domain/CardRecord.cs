namespace CardDemo.Domain;

/// <summary>
/// Modern representation of the CardDemo card entity.
/// Mirrors the COBOL copybook CVACT02Y (CARD-RECORD, RECLN 150).
/// </summary>
public sealed class CardRecord
{
    /// <summary>CARD-NUM PIC X(16) — primary key.</summary>
    public string CardNumber { get; set; } = string.Empty;

    /// <summary>CARD-ACCT-ID PIC 9(11) — owning account.</summary>
    public long AccountId { get; set; }

    /// <summary>CARD-CVV-CD PIC 9(03).</summary>
    public int CvvCode { get; set; }

    /// <summary>CARD-EMBOSSED-NAME PIC X(50).</summary>
    public string EmbossedName { get; set; } = string.Empty;

    /// <summary>CARD-EXPIRAION-DATE PIC X(10) (spelling preserved from copybook).</summary>
    public string ExpirationDate { get; set; } = string.Empty;

    /// <summary>CARD-ACTIVE-STATUS PIC X(01).</summary>
    public string ActiveStatus { get; set; } = string.Empty;
}
