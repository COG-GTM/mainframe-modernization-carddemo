using CardDemo.Domain;

namespace CardDemo.Data.Repositories;

/// <summary>
/// Data access for account records, replacing sequential VSAM KSDS reads.
/// </summary>
public interface IAccountRepository
{
    /// <summary>
    /// Streams every account in ascending account-id order, mirroring the
    /// sequential key order of the original ACCTFILE KSDS.
    /// The result is lazily evaluated and holds an open database connection for
    /// the duration of iteration, so callers must fully enumerate or dispose it
    /// (a <c>foreach</c> does this automatically).
    /// </summary>
    IEnumerable<AccountRecord> ReadAll();

    /// <summary>Inserts or updates a single account (used by data migration).</summary>
    void Upsert(AccountRecord account);
}
