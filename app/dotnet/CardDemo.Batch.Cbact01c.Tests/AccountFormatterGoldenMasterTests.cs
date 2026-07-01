using System.Text;
using CardDemo.Batch.Cbact01c.Etl;
using CardDemo.Batch.Cbact01c.Formatting;

namespace CardDemo.Batch.Cbact01c.Tests;

/// <summary>
/// Golden-master parity test: parse the legacy fixed-width sample and assert the
/// migrated per-record output matches, byte-for-byte, the layout produced by
/// CBACT01C's 1100-DISPLAY-ACCT-RECORD paragraph.
/// </summary>
public class AccountFormatterGoldenMasterTests
{
    private static string FixturePath(string name) =>
        Path.Combine(AppContext.BaseDirectory, "Fixtures", name);

    [Fact]
    public void Format_MatchesLegacyOutput_ByteForByte()
    {
        var accounts = AccountFileLoader.ReadRecords(FixturePath("acctdata_sample.txt")).ToList();

        var actual = new StringBuilder();
        foreach (var account in accounts)
        {
            foreach (var line in AccountFormatter.FormatLines(account))
            {
                actual.Append(line).Append('\n');
            }
        }

        var expected = File.ReadAllText(FixturePath("expected_output.txt")).Replace("\r\n", "\n");
        Assert.Equal(expected, actual.ToString());
    }
}
