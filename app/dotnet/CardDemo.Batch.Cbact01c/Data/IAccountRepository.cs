using CardDemo.Batch.Cbact01c.Domain;

namespace CardDemo.Batch.Cbact01c.Data;

/// <summary>
/// Provides access to account records, replacing sequential reads of the VSAM KSDS.
/// </summary>
public interface IAccountRepository
{
    /// <summary>
    /// Streams all accounts ordered by account id (ascending), mirroring the key order
    /// of the original VSAM KSDS sequential read. Implementations must stream rows
    /// (forward-only) rather than materializing the full table in memory.
    /// </summary>
    IAsyncEnumerable<AccountRecord> StreamAllOrderedByIdAsync(CancellationToken cancellationToken = default);
}
