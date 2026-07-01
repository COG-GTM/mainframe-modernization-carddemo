using CardDemo.Data.Cobol;
using Xunit;

namespace CardDemo.Tests;

public class ZonedDecimalTests
{
    [Theory]
    [InlineData("00000001940{", 2, "194.00")]   // { => +0
    [InlineData("00000020200{", 2, "2020.00")]
    [InlineData("00000000000{", 2, "0.00")]
    [InlineData("123A", 0, "1231")]             // A => +1 (units digit)
    [InlineData("0000000019D", 2, "1.94")]      // D => +4
    public void DecodeSigned_PositiveOverpunch(string field, int dp, string expected)
    {
        var value = ZonedDecimal.DecodeSigned(field, dp);
        Assert.Equal(decimal.Parse(expected, System.Globalization.CultureInfo.InvariantCulture), value);
    }

    [Theory]
    [InlineData("123}", 0, "-1230")]            // } => -0
    [InlineData("0000000019M", 2, "-1.94")]     // M => -4
    [InlineData("00000000001R", 2, "-0.19")]    // R => -9 -> ...19
    public void DecodeSigned_NegativeOverpunch(string field, int dp, string expected)
    {
        var value = ZonedDecimal.DecodeSigned(field, dp);
        Assert.Equal(decimal.Parse(expected, System.Globalization.CultureInfo.InvariantCulture), value);
    }

    [Theory]
    [InlineData("00000001940{", 12, 2)]
    [InlineData("00000000000{", 12, 2)]
    [InlineData("0000000019M", 11, 2)]
    public void EncodeSigned_IsInverseOfDecode(string field, int totalDigits, int dp)
    {
        var value = ZonedDecimal.DecodeSigned(field, dp);
        var reencoded = ZonedDecimal.EncodeSigned(value, totalDigits, dp);
        Assert.Equal(field, reencoded);
    }

    [Fact]
    public void EncodeSigned_EncodesNegativeUnitsDigit()
    {
        // -194.00 with 12 digits, 2 dp -> "00000001940" + '}' (negative 0)
        Assert.Equal("00000001940}", ZonedDecimal.EncodeSigned(-194.00m, 12, 2));
    }

    [Fact]
    public void DecodeSigned_InvalidCharacter_Throws()
    {
        Assert.Throws<FormatException>(() => ZonedDecimal.DecodeSigned("12*4", 0));
    }
}
