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
    /// </summary>
    IEnumerable<AccountRecord> ReadAll();

    /// <summary>Inserts or updates a single account (used by data migration).</summary>
    void Upsert(AccountRecord account);
}
