using CardDemo.Batch.Cbact01c.Domain;

namespace CardDemo.Batch.Cbact01c.Legacy;

/// <summary>
/// Parses a fixed-width 300-byte legacy ACCOUNT-RECORD (copybook CVACT01Y) into an
/// <see cref="AccountRecord"/>. Field offsets and widths mirror the copybook exactly.
/// </summary>
public static class FixedWidthAccountParser
{
    public const int RecordLength = 300;

    // Offset / width of each field within the 300-byte record (0-based).
    private const int AcctIdOffset = 0, AcctIdWidth = 11;
    private const int ActiveStatusOffset = 11, ActiveStatusWidth = 1;
    private const int CurrBalOffset = 12, MoneyWidth = 12;
    private const int CreditLimitOffset = 24;
    private const int CashCreditLimitOffset = 36;
    private const int OpenDateOffset = 48, DateWidth = 10;
    private const int ExpirationDateOffset = 58;
    private const int ReissueDateOffset = 68;
    private const int CurrCycCreditOffset = 78;
    private const int CurrCycDebitOffset = 90;
    private const int AddrZipOffset = 102, AddrZipWidth = 10;
    private const int GroupIdOffset = 112, GroupIdWidth = 10;

    // S9(10)V99 => 10 integer digits, 2 decimal places.
    private const int MoneyIntegerDigits = 10;
    private const int MoneyDecimalPlaces = 2;

    // All numeric/date fields end at this offset; trailing X-fields may be space-stripped.
    private const int MinimumParsableLength = CurrCycDebitOffset + MoneyWidth;

    public static AccountRecord Parse(string record)
    {
        ArgumentNullException.ThrowIfNull(record);
        if (record.Length < MinimumParsableLength)
        {
            throw new FormatException(
                $"Record too short: expected at least {MinimumParsableLength} chars, got {record.Length}.");
        }

        // Fixed-width records are RECLN 300; ASCII exports often strip trailing FILLER/blanks.
        // Right-pad so the trailing X-fields (zip, group id) decode as space-filled.
        if (record.Length < RecordLength)
        {
            record = record.PadRight(RecordLength);
        }

        return new AccountRecord
        {
            AccountId = long.Parse(Field(record, AcctIdOffset, AcctIdWidth)),
            ActiveStatus = Field(record, ActiveStatusOffset, ActiveStatusWidth),
            CurrentBalance = Money(record, CurrBalOffset),
            CreditLimit = Money(record, CreditLimitOffset),
            CashCreditLimit = Money(record, CashCreditLimitOffset),
            OpenDate = Field(record, OpenDateOffset, DateWidth),
            ExpirationDate = Field(record, ExpirationDateOffset, DateWidth),
            ReissueDate = Field(record, ReissueDateOffset, DateWidth),
            CurrentCycleCredit = Money(record, CurrCycCreditOffset),
            CurrentCycleDebit = Money(record, CurrCycDebitOffset),
            AddressZip = Field(record, AddrZipOffset, AddrZipWidth).TrimEnd(),
            GroupId = Field(record, GroupIdOffset, GroupIdWidth).TrimEnd()
        };
    }

    private static string Field(string record, int offset, int width) =>
        record.Substring(offset, width);

    private static decimal Money(string record, int offset) =>
        ZonedDecimalCodec.Decode(Field(record, offset, MoneyWidth), MoneyDecimalPlaces);

    public static (int integerDigits, int decimalPlaces) MoneyFormat =>
        (MoneyIntegerDigits, MoneyDecimalPlaces);
}
