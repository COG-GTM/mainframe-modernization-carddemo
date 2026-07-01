using CardDemo.Batch.Cbact01c.Domain;
using CardDemo.Batch.Cbact01c.Legacy;

namespace CardDemo.Batch.Cbact01c.Etl;

/// <summary>
/// One-time / repeatable ETL helper: reads the legacy fixed-width account flat file
/// (ASCII, overpunch-encoded numerics) and yields parsed <see cref="AccountRecord"/>s
/// ready to be bulk-inserted into the relational store.
/// </summary>
public static class AccountFileLoader
{
    /// <summary>
    /// Parses every non-empty line of the legacy account file. Lines are the 300-byte
    /// fixed-width records (trailing FILLER padding is tolerated / ignored).
    /// </summary>
    public static IEnumerable<AccountRecord> ReadRecords(TextReader reader)
    {
        ArgumentNullException.ThrowIfNull(reader);
        string? line;
        while ((line = reader.ReadLine()) is not null)
        {
            if (line.TrimEnd().Length == 0)
            {
                continue;
            }

            yield return FixedWidthAccountParser.Parse(line);
        }
    }

    /// <summary>Convenience overload that reads records from a file path.</summary>
    public static IEnumerable<AccountRecord> ReadRecords(string path)
    {
        using var reader = new StreamReader(path);
        foreach (var record in ReadRecords(reader))
        {
            yield return record;
        }
    }
}
