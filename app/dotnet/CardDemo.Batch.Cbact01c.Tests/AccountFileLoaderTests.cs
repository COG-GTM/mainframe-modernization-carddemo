using CardDemo.Batch.Cbact01c.Etl;

namespace CardDemo.Batch.Cbact01c.Tests;

public class AccountFileLoaderTests
{
    [Fact]
    public void ReadRecords_SkipsBlankLines_AndParsesAll()
    {
        const string data =
            "00000000001Y00000001940{00000020200{00000010200{2014-11-202025-05-202025-05-2000000000000{00000000000{A000000000\n" +
            "\n" +
            "00000000002Y00000001580{00000061300{00000054480{2013-06-192024-08-112024-08-1100000000000{00000000000{A000000000\n";

        using var reader = new StringReader(data);
        var records = AccountFileLoader.ReadRecords(reader).ToList();

        Assert.Equal(2, records.Count);
        Assert.Equal(1L, records[0].AccountId);
        Assert.Equal(2L, records[1].AccountId);
    }
}
