using CardDemo.Data.Parsing;
using Xunit;

namespace CardDemo.Tests;

public class CardRecordParserTests
{
    // First record of app/data/ASCII/carddata.txt, composed field-by-field.
    private static readonly string SampleRecord =
        "0500024453765740" +          // CARD-NUM
        "00000000050" +               // CARD-ACCT-ID
        "747" +                       // CARD-CVV-CD
        "Aniya Von".PadRight(50) +    // CARD-EMBOSSED-NAME (50)
        "2023-03-09" +                // CARD-EXPIRAION-DATE
        "Y";                          // CARD-ACTIVE-STATUS

    [Fact]
    public void Parse_MapsAllFields()
    {
        var card = CardRecordParser.Parse(SampleRecord);

        Assert.Equal("0500024453765740", card.CardNumber);
        Assert.Equal(50L, card.AccountId);
        Assert.Equal(747, card.CvvCode);
        Assert.Equal("Aniya Von", card.EmbossedName);
        Assert.Equal("2023-03-09", card.ExpirationDate);
        Assert.Equal("Y", card.ActiveStatus);
    }
}
