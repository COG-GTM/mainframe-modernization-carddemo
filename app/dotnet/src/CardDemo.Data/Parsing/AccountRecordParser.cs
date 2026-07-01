using CardDemo.Data.Cobol;
using CardDemo.Domain;

namespace CardDemo.Data.Parsing;

/// <summary>
/// Parses a 300-byte CardDemo account flat-file record (copybook CVACT01Y)
/// into an <see cref="AccountRecord"/>.
/// </summary>
public static class AccountRecordParser
{
    public const int RecordLength = 300;

    public static AccountRecord Parse(string record)
    {
        ArgumentNullException.ThrowIfNull(record);

        // Tolerate records padded shorter/longer than the fixed length; pad to
        // the declared width so FILLER-only trailing bytes are optional.
        if (record.Length < RecordLength)
        {
            record = record.PadRight(RecordLength);
        }

        var reader = new FixedWidthReader(record);

        return new AccountRecord
        {
            AccountId = reader.ReadUnsignedLong(11),          // ACCT-ID          PIC 9(11)
            ActiveStatus = reader.ReadText(1),                // ACCT-ACTIVE-STATUS PIC X(01)
            CurrentBalance = reader.ReadSignedDecimal(12, 2), // ACCT-CURR-BAL    PIC S9(10)V99
            CreditLimit = reader.ReadSignedDecimal(12, 2),    // ACCT-CREDIT-LIMIT
            CashCreditLimit = reader.ReadSignedDecimal(12, 2),// ACCT-CASH-CREDIT-LIMIT
            OpenDate = reader.ReadText(10),                   // ACCT-OPEN-DATE   PIC X(10)
            ExpirationDate = reader.ReadText(10),             // ACCT-EXPIRAION-DATE
            ReissueDate = reader.ReadText(10),                // ACCT-REISSUE-DATE
            CurrentCycleCredit = reader.ReadSignedDecimal(12, 2), // ACCT-CURR-CYC-CREDIT
            CurrentCycleDebit = reader.ReadSignedDecimal(12, 2),  // ACCT-CURR-CYC-DEBIT
            AddressZip = reader.ReadText(10),                 // ACCT-ADDR-ZIP    PIC X(10)
            GroupId = reader.ReadText(10),                    // ACCT-GROUP-ID    PIC X(10)
            // remaining FILLER PIC X(178) is ignored
        };
    }
}
