using System.Runtime.CompilerServices;
using CardDemo.Batch.Cbact01c.Domain;
using Npgsql;

namespace CardDemo.Batch.Cbact01c.Data;

/// <summary>
/// PostgreSQL-backed <see cref="IAccountRepository"/>. Uses a forward-only reader so
/// arbitrarily large tables stream without being buffered in memory.
/// </summary>
public sealed class PostgresAccountRepository : IAccountRepository
{
    private const string SelectAllOrdered = """
        SELECT acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
               open_date, expiration_date, reissue_date, curr_cyc_credit,
               curr_cyc_debit, addr_zip, group_id
        FROM account
        ORDER BY acct_id ASC
        """;

    private readonly string _connectionString;

    public PostgresAccountRepository(string connectionString)
    {
        _connectionString = connectionString ?? throw new ArgumentNullException(nameof(connectionString));
    }

    public async IAsyncEnumerable<AccountRecord> StreamAllOrderedByIdAsync(
        [EnumeratorCancellation] CancellationToken cancellationToken = default)
    {
        await using var connection = new NpgsqlConnection(_connectionString);
        await connection.OpenAsync(cancellationToken).ConfigureAwait(false);

        await using var command = new NpgsqlCommand(SelectAllOrdered, connection);
        await using var reader = await command
            .ExecuteReaderAsync(System.Data.CommandBehavior.SingleResult, cancellationToken)
            .ConfigureAwait(false);

        while (await reader.ReadAsync(cancellationToken).ConfigureAwait(false))
        {
            yield return new AccountRecord
            {
                AccountId = reader.GetInt64(0),
                ActiveStatus = reader.GetString(1),
                CurrentBalance = reader.GetDecimal(2),
                CreditLimit = reader.GetDecimal(3),
                CashCreditLimit = reader.GetDecimal(4),
                OpenDate = reader.GetString(5),
                ExpirationDate = reader.GetString(6),
                ReissueDate = reader.GetString(7),
                CurrentCycleCredit = reader.GetDecimal(8),
                CurrentCycleDebit = reader.GetDecimal(9),
                AddressZip = reader.GetString(10),
                GroupId = reader.GetString(11)
            };
        }
    }
}
