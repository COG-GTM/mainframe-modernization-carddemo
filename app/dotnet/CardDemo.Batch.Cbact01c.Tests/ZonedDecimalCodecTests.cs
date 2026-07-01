using CardDemo.Batch.Cbact01c.Legacy;

namespace CardDemo.Batch.Cbact01c.Tests;

public class ZonedDecimalCodecTests
{
    [Theory]
    // The final digit is carried by the overpunch char, so "...1940{" = ...19400 => 194.00.
    [InlineData("00000001940{", 2, 194.00)]
    [InlineData("00000020200{", 2, 2020.00)]
    [InlineData("00000000000{", 2, 0.00)]
    [InlineData("0000000194A", 2, 19.41)]    // 'A' => positive 1 in last position
    [InlineData("0000000194J", 2, -19.41)]   // 'J' => negative 1
    [InlineData("00000001940}", 2, -194.00)] // '}' => negative 0
    public void Decode_ParsesSignedOverpunch(string raw, int decimals, double expected)
    {
        var result = ZonedDecimalCodec.Decode(raw, decimals);
        Assert.Equal((decimal)expected, result);
    }

    [Theory]
    [InlineData(194.00, "00000001940{")]
    [InlineData(2020.00, "00000020200{")]
    [InlineData(0.00, "00000000000{")]
    [InlineData(-194.00, "00000001940}")]
    [InlineData(-19.41, "00000000194J")]
    public void Encode_ProducesZonedOverpunch(double value, string expected)
    {
        var result = ZonedDecimalCodec.Encode((decimal)value, 10, 2);
        Assert.Equal(expected, result);
    }

    [Theory]
    [InlineData(19.40)]
    [InlineData(-1234567.89)]
    [InlineData(0.00)]
    [InlineData(9999999999.99)]
    public void EncodeThenDecode_RoundTrips(double value)
    {
        var encoded = ZonedDecimalCodec.Encode((decimal)value, 10, 2);
        var decoded = ZonedDecimalCodec.Decode(encoded, 2);
        Assert.Equal((decimal)value, decoded);
    }

    [Fact]
    public void Encode_Overflow_Throws()
    {
        Assert.Throws<OverflowException>(() => ZonedDecimalCodec.Encode(100000000000m, 10, 2));
    }

    [Fact]
    public void Decode_InvalidChar_Throws()
    {
        Assert.Throws<FormatException>(() => ZonedDecimalCodec.Decode("0000000019*", 2));
    }
}
