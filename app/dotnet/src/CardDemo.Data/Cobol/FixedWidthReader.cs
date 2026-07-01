namespace CardDemo.Data.Cobol;

/// <summary>
/// Sequentially extracts fixed-width fields from a mainframe flat-file record,
/// tracking the current byte offset so parsers read fields in copybook order.
/// </summary>
public sealed class FixedWidthReader
{
    private readonly string _record;
    private int _position;

    public FixedWidthReader(string record)
    {
        _record = record ?? throw new ArgumentNullException(nameof(record));
    }

    public int Position => _position;

    /// <summary>Reads the next <paramref name="length"/> characters verbatim.</summary>
    public string Read(int length)
    {
        if (length < 0)
        {
            throw new ArgumentOutOfRangeException(nameof(length));
        }

        if (_position + length > _record.Length)
        {
            throw new FormatException(
                $"Record too short: needed {length} chars at offset {_position} but only " +
                $"{_record.Length - _position} remain (record length {_record.Length}).");
        }

        var value = _record.Substring(_position, length);
        _position += length;
        return value;
    }

    /// <summary>Reads a fixed-width text field, trimming trailing spaces.</summary>
    public string ReadText(int length) => Read(length).TrimEnd();

    /// <summary>Reads an unsigned numeric field (PIC 9) as a long.</summary>
    public long ReadUnsignedLong(int length)
    {
        var raw = Read(length).Trim();
        return raw.Length == 0 ? 0L : long.Parse(raw);
    }

    /// <summary>Reads an unsigned numeric field (PIC 9) as an int.</summary>
    public int ReadUnsignedInt(int length)
    {
        var raw = Read(length).Trim();
        return raw.Length == 0 ? 0 : int.Parse(raw);
    }

    /// <summary>Reads a signed zoned-decimal field (PIC S9..V..).</summary>
    public decimal ReadSignedDecimal(int length, int decimalPlaces)
        => ZonedDecimal.DecodeSigned(Read(length), decimalPlaces);

    /// <summary>Skips <paramref name="length"/> characters (e.g. FILLER).</summary>
    public void Skip(int length) => Read(length);
}
