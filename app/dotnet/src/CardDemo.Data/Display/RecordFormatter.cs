using System.Globalization;
using System.Text;
using CardDemo.Domain;

namespace CardDemo.Data.Display;

/// <summary>
/// Produces human-readable, field-by-field renderings of records that mirror
/// the COBOL DISPLAY output of CBACT01C (paragraph 1100-DISPLAY-ACCT-RECORD)
/// and an equivalent layout for cards (CBACT02C).
///
/// Note: the legacy programs DISPLAY signed numerics as raw zoned-decimal
/// bytes (e.g. "00000019400{"). The modern reader intentionally renders the
/// decimal value (e.g. "194.00"), which carries the same business meaning.
/// </summary>
public static class RecordFormatter
{
    private const int LabelWidth = 24;
    private const string Separator = "-------------------------------------------------";

    public static string FormatAccount(AccountRecord a)
    {
        ArgumentNullException.ThrowIfNull(a);

        var sb = new StringBuilder();
        Line(sb, "ACCT-ID", a.AccountId.ToString("D11", CultureInfo.InvariantCulture));
        Line(sb, "ACCT-ACTIVE-STATUS", a.ActiveStatus);
        Line(sb, "ACCT-CURR-BAL", Money(a.CurrentBalance));
        Line(sb, "ACCT-CREDIT-LIMIT", Money(a.CreditLimit));
        Line(sb, "ACCT-CASH-CREDIT-LIMIT", Money(a.CashCreditLimit));
        Line(sb, "ACCT-OPEN-DATE", a.OpenDate);
        Line(sb, "ACCT-EXPIRAION-DATE", a.ExpirationDate);
        Line(sb, "ACCT-REISSUE-DATE", a.ReissueDate);
        Line(sb, "ACCT-CURR-CYC-CREDIT", Money(a.CurrentCycleCredit));
        Line(sb, "ACCT-CURR-CYC-DEBIT", Money(a.CurrentCycleDebit));
        Line(sb, "ACCT-GROUP-ID", a.GroupId);
        sb.Append(Separator);
        return sb.ToString();
    }

    public static string FormatCard(CardRecord c)
    {
        ArgumentNullException.ThrowIfNull(c);

        var sb = new StringBuilder();
        Line(sb, "CARD-NUM", c.CardNumber);
        Line(sb, "CARD-ACCT-ID", c.AccountId.ToString("D11", CultureInfo.InvariantCulture));
        Line(sb, "CARD-CVV-CD", c.CvvCode.ToString("D3", CultureInfo.InvariantCulture));
        Line(sb, "CARD-EMBOSSED-NAME", c.EmbossedName);
        Line(sb, "CARD-EXPIRAION-DATE", c.ExpirationDate);
        Line(sb, "CARD-ACTIVE-STATUS", c.ActiveStatus);
        sb.Append(Separator);
        return sb.ToString();
    }

    private static void Line(StringBuilder sb, string label, string value)
    {
        sb.Append(label.PadRight(LabelWidth));
        sb.Append(':');
        sb.AppendLine(value);
    }

    private static string Money(decimal value) => value.ToString("F2", CultureInfo.InvariantCulture);
}
