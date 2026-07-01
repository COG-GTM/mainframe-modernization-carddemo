using CardDemo.Data.Database;
using CardDemo.Domain;
using Dapper;
using Npgsql;

namespace CardDemo.Data.Repositories;

/// <summary>
/// PostgreSQL-backed implementation of <see cref="ICardRepository"/> using Dapper.
/// </summary>
public sealed class CardRepository : ICardRepository
{
    private const string SelectAll = """
        SELECT card_num            AS CardNumber,
               card_acct_id        AS AccountId,
               card_cvv_cd         AS CvvCode,
               card_embossed_name  AS EmbossedName,
               card_expiration_date AS ExpirationDate,
               card_active_status  AS ActiveStatus
        FROM card
        ORDER BY card_num
        """;

    private const string UpsertSql = """
        INSERT INTO card (
            card_num, card_acct_id, card_cvv_cd, card_embossed_name,
            card_expiration_date, card_active_status)
        VALUES (
            @CardNumber, @AccountId, @CvvCode, @EmbossedName,
            @ExpirationDate, @ActiveStatus)
        ON CONFLICT (card_num) DO UPDATE SET
            card_acct_id         = EXCLUDED.card_acct_id,
            card_cvv_cd          = EXCLUDED.card_cvv_cd,
            card_embossed_name   = EXCLUDED.card_embossed_name,
            card_expiration_date = EXCLUDED.card_expiration_date,
            card_active_status   = EXCLUDED.card_active_status
        """;

    private readonly DatabaseOptions _options;

    public CardRepository(DatabaseOptions options)
    {
        _options = options ?? throw new ArgumentNullException(nameof(options));
    }

    public IEnumerable<CardRecord> ReadAll()
    {
        using var connection = new NpgsqlConnection(_options.ConnectionString);
        connection.Open();
        foreach (var card in connection.Query<CardRecord>(SelectAll, buffered: false))
        {
            yield return card;
        }
    }

    public void Upsert(CardRecord card)
    {
        ArgumentNullException.ThrowIfNull(card);
        using var connection = new NpgsqlConnection(_options.ConnectionString);
        connection.Open();
        connection.Execute(UpsertSql, card);
    }
}
