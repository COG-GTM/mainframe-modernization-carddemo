using CardDemo.Data.Parsing;
using Xunit;

namespace CardDemo.Tests;

public class AccountRecordParserTests
{
    // First record of app/data/ASCII/acctdata.txt, composed field-by-field.
    private const string SampleRecord =
        "00000000001" + // ACCT-ID
        "Y" +           // ACCT-ACTIVE-STATUS
        "00000001940{" +// ACCT-CURR-BAL           194.00
        "00000020200{" +// ACCT-CREDIT-LIMIT      2020.00
        "00000010200{" +// ACCT-CASH-CREDIT-LIMIT 1020.00
        "2014-11-20" +  // ACCT-OPEN-DATE
        "2025-05-20" +  // ACCT-EXPIRAION-DATE
        "2025-05-20" +  // ACCT-REISSUE-DATE
        "00000000000{" +// ACCT-CURR-CYC-CREDIT     0.00
        "00000000000{" +// ACCT-CURR-CYC-DEBIT      0.00
        "A000000000" +  // ACCT-ADDR-ZIP
        "          ";   // ACCT-GROUP-ID (blank); parser pads remaining FILLER

    [Fact]
    public void Parse_MapsAllFields()
    {
        var account = AccountRecordParser.Parse(SampleRecord);

        Assert.Equal(1L, account.AccountId);
        Assert.Equal("Y", account.ActiveStatus);
        Assert.Equal(194.00m, account.CurrentBalance);
        Assert.Equal(2020.00m, account.CreditLimit);
        Assert.Equal(1020.00m, account.CashCreditLimit);
        Assert.Equal("2014-11-20", account.OpenDate);
        Assert.Equal("2025-05-20", account.ExpirationDate);
        Assert.Equal("2025-05-20", account.ReissueDate);
        Assert.Equal(0.00m, account.CurrentCycleCredit);
        Assert.Equal(0.00m, account.CurrentCycleDebit);
        Assert.Equal("A000000000", account.AddressZip);
        Assert.Equal(string.Empty, account.GroupId);
    }

    [Fact]
    public void Parse_ShortRecordIsPadded()
    {
        // Only the leading fields provided; parser pads to the fixed 300 length.
        var account = AccountRecordParser.Parse("00000000042" + "N");
        Assert.Equal(42L, account.AccountId);
        Assert.Equal("N", account.ActiveStatus);
        Assert.Equal(0.00m, account.CurrentBalance);
    }
}
