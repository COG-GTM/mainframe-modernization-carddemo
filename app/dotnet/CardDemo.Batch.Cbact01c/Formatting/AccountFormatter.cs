using System.Globalization;
using System.Text;
using CardDemo.Batch.Cbact01c.Domain;
using CardDemo.Batch.Cbact01c.Legacy;

namespace CardDemo.Batch.Cbact01c.Formatting;

/// <summary>
/// Reproduces, byte-for-byte, the console output that CBACT01C's
/// 1100-DISPLAY-ACCT-RECORD paragraph produces for a single account, so the
/// migrated program can be validated against the mainframe via a golden master.
/// Signed numeric fields are re-encoded to their COBOL zoned/overpunch display form.
/// </summary>
public static class AccountFormatter
{
    private const string Separator = "-------------------------------------------------";
    private const int GroupIdWidth = 10;

    // Labels copied verbatim from the COBOL DISPLAY literals (including trailing spaces).
    private const string LblAcctId = "ACCT-ID                 :";
    private const string LblActiveStatus = "ACCT-ACTIVE-STATUS      :";
    private const string LblCurrBal = "ACCT-CURR-BAL           :";
    private const string LblCreditLimit = "ACCT-CREDIT-LIMIT       :";
    private const string LblCashCreditLimit = "ACCT-CASH-CREDIT-LIMIT  :";
    private const string LblOpenDate = "ACCT-OPEN-DATE          :";
    private const string LblExpirationDate = "ACCT-EXPIRAION-DATE     :";
    private const string LblReissueDate = "ACCT-REISSUE-DATE       :";
    private const string LblCurrCycCredit = "ACCT-CURR-CYC-CREDIT    :";
    private const string LblCurrCycDebit = "ACCT-CURR-CYC-DEBIT     :";
    private const string LblGroupId = "ACCT-GROUP-ID           :";

    public static IEnumerable<string> FormatLines(AccountRecord account)
    {
        ArgumentNullException.ThrowIfNull(account);
        yield return LblAcctId + account.AccountId.ToString("D11", CultureInfo.InvariantCulture);
        yield return LblActiveStatus + account.ActiveStatus;
        yield return LblCurrBal + Money(account.CurrentBalance);
        yield return LblCreditLimit + Money(account.CreditLimit);
        yield return LblCashCreditLimit + Money(account.CashCreditLimit);
        yield return LblOpenDate + account.OpenDate;
        yield return LblExpirationDate + account.ExpirationDate;
        yield return LblReissueDate + account.ReissueDate;
        yield return LblCurrCycCredit + Money(account.CurrentCycleCredit);
        yield return LblCurrCycDebit + Money(account.CurrentCycleDebit);
        // ACCT-GROUP-ID is PIC X(10); COBOL DISPLAY emits the full space-padded width.
        yield return LblGroupId + account.GroupId.PadRight(GroupIdWidth);
        yield return Separator;
    }

    public static string Format(AccountRecord account)
    {
        var sb = new StringBuilder();
        foreach (var line in FormatLines(account))
        {
            sb.AppendLine(line);
        }

        return sb.ToString();
    }

    private static string Money(decimal value)
    {
        var (integerDigits, decimalPlaces) = FixedWidthAccountParser.MoneyFormat;
        return ZonedDecimalCodec.Encode(value, integerDigits, decimalPlaces);
    }
}
