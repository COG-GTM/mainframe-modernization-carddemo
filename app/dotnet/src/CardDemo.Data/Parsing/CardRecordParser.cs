using CardDemo.Data.Cobol;
using CardDemo.Domain;

namespace CardDemo.Data.Parsing;

/// <summary>
/// Parses a 150-byte CardDemo card flat-file record (copybook CVACT02Y)
/// into a <see cref="CardRecord"/>.
/// </summary>
public static class CardRecordParser
{
    public const int RecordLength = 150;

    public static CardRecord Parse(string record)
    {
        ArgumentNullException.ThrowIfNull(record);

        if (record.Length < RecordLength)
        {
            record = record.PadRight(RecordLength);
        }

        var reader = new FixedWidthReader(record);

        return new CardRecord
        {
            CardNumber = reader.ReadText(16),      // CARD-NUM           PIC X(16)
            AccountId = reader.ReadUnsignedLong(11),// CARD-ACCT-ID      PIC 9(11)
            CvvCode = reader.ReadUnsignedInt(3),    // CARD-CVV-CD        PIC 9(03)
            EmbossedName = reader.ReadText(50),     // CARD-EMBOSSED-NAME PIC X(50)
            ExpirationDate = reader.ReadText(10),   // CARD-EXPIRAION-DATE PIC X(10)
            ActiveStatus = reader.ReadText(1),      // CARD-ACTIVE-STATUS PIC X(01)
            // remaining FILLER PIC X(59) is ignored
        };
    }
}
