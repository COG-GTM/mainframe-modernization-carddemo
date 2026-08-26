using CardDemo.Identity.Cobol;
using CardDemo.Identity.Copybooks;

namespace CardDemo.Identity.Cics;

/// <summary>
/// In-memory stand-in for the USRSEC VSAM KSDS, keyed by SEC-USR-ID.
/// </summary>
public sealed class InMemoryUserSecurityFile : IUserSecurityFile
{
    private readonly Dictionary<string, SecUserData> _records = new();

    /// <summary>RESP code returned instead of NORMAL/NOTFND, to simulate file errors.</summary>
    public int? ForcedRespCode { get; set; }

    /// <summary>Keys the program asked for, in order.</summary>
    public List<string> ReadKeys { get; } = new();

    /// <summary>Dataset names the program read, in order.</summary>
    public List<string> ReadDatasets { get; } = new();

    /// <summary>Stores the record with copybook field lengths (space padded).</summary>
    public void Add(SecUserData record) =>
        _records[CobolString.Move(record.SecUsrId, 8)] = new SecUserData
        {
            SecUsrId = CobolString.Move(record.SecUsrId, 8),
            SecUsrFname = CobolString.Move(record.SecUsrFname, 20),
            SecUsrLname = CobolString.Move(record.SecUsrLname, 20),
            SecUsrPwd = CobolString.Move(record.SecUsrPwd, 8),
            SecUsrType = CobolString.Move(record.SecUsrType, 1),
            SecUsrFiller = CobolString.Move(record.SecUsrFiller, 23)
        };

    public int Read(string dataset, string key, SecUserData into)
    {
        ReadDatasets.Add(dataset);
        ReadKeys.Add(key);

        if (ForcedRespCode is { } forced)
        {
            return forced;
        }

        if (!_records.TryGetValue(CobolString.Move(key, 8), out var record))
        {
            return CicsRespCodes.Notfnd;
        }

        into.SecUsrId = record.SecUsrId;
        into.SecUsrFname = record.SecUsrFname;
        into.SecUsrLname = record.SecUsrLname;
        into.SecUsrPwd = record.SecUsrPwd;
        into.SecUsrType = record.SecUsrType;
        into.SecUsrFiller = record.SecUsrFiller;
        return CicsRespCodes.Normal;
    }
}
