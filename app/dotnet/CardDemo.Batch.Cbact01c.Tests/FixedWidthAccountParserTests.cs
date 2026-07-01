using CardDemo.Batch.Cbact01c.Legacy;

namespace CardDemo.Batch.Cbact01c.Tests;

public class FixedWidthAccountParserTests
{
    private const string Record1 =
        "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000";

    [Fact]
    public void Parse_MapsAllFields()
    {
        var account = FixedWidthAccountParser.Parse(Record1);

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
    public void Parse_ShortRecord_Throws()
    {
        Assert.Throws<FormatException>(() => FixedWidthAccountParser.Parse("00000000001Y"));
    }
}
