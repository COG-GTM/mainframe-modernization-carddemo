using CardDemo.Data.Cobol;
using Xunit;

namespace CardDemo.Tests;

public class FixedWidthReaderTests
{
    [Fact]
    public void ReadsFieldsSequentially()
    {
        var reader = new FixedWidthReader("ABC12345  X");
        Assert.Equal("ABC", reader.Read(3));
        Assert.Equal(12345, reader.ReadUnsignedInt(5));
        reader.Skip(2);
        Assert.Equal("X", reader.ReadText(1));
        Assert.Equal(11, reader.Position);
    }

    [Fact]
    public void ReadText_TrimsTrailingSpaces()
    {
        var reader = new FixedWidthReader("hi        ");
        Assert.Equal("hi", reader.ReadText(10));
    }

    [Fact]
    public void Read_PastEnd_Throws()
    {
        var reader = new FixedWidthReader("abc");
        Assert.Throws<FormatException>(() => reader.Read(4));
    }
}
