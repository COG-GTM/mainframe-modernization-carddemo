using CardDemo.Domain;

namespace CardDemo.Data.Repositories;

/// <summary>
/// Data access for card records, replacing sequential VSAM KSDS reads.
/// </summary>
public interface ICardRepository
{
    /// <summary>
    /// Streams every card in ascending card-number order, mirroring the
    /// sequential key order of the original CARDFILE KSDS.
    /// </summary>
    IEnumerable<CardRecord> ReadAll();

    /// <summary>Inserts or updates a single card (used by data migration).</summary>
    void Upsert(CardRecord card);
}
