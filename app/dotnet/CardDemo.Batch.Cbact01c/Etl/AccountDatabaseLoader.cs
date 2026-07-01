using CardDemo.Batch.Cbact01c.Domain;
using Npgsql;

namespace CardDemo.Batch.Cbact01c.Etl;

/// <summary>
/// Bulk-loads parsed <see cref="AccountRecord"/>s into the PostgreSQL <c>account</c> table
/// using COPY, and provides a simple reconciliation count for post-load validation.
/// </summary>
public sealed class AccountDatabaseLoader
{
    private const string CopyCommand = """
        COPY account (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
                      open_date, expiration_date, reissue_date, curr_cyc_credit,
                      curr_cyc_debit, addr_zip, group_id)
        FROM STDIN (FORMAT BINARY)
        """;

    private readonly string _connectionString;

    public AccountDatabaseLoader(string connectionString)
    {
        _connectionString = connectionString ?? throw new ArgumentNullException(nameof(connectionString));
    }

    /// <summary>Inserts all records and returns the number of rows written.</summary>
    public async Task<long> LoadAsync(
        IEnumerable<AccountRecord> records,
        CancellationToken cancellationToken = default)
    {
        ArgumentNullException.ThrowIfNull(records);

        await using var connection = new NpgsqlConnection(_connectionString);
        await connection.OpenAsync(cancellationToken).ConfigureAwait(false);

        long written = 0;
        await using (var writer = await connection.BeginBinaryImportAsync(CopyCommand, cancellationToken)
                         .ConfigureAwait(false))
        {
            foreach (var r in records)
            {
                await writer.StartRowAsync(cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.AccountId, NpgsqlTypes.NpgsqlDbType.Bigint, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.ActiveStatus, NpgsqlTypes.NpgsqlDbType.Char, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.CurrentBalance, NpgsqlTypes.NpgsqlDbType.Numeric, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.CreditLimit, NpgsqlTypes.NpgsqlDbType.Numeric, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.CashCreditLimit, NpgsqlTypes.NpgsqlDbType.Numeric, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.OpenDate, NpgsqlTypes.NpgsqlDbType.Varchar, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.ExpirationDate, NpgsqlTypes.NpgsqlDbType.Varchar, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.ReissueDate, NpgsqlTypes.NpgsqlDbType.Varchar, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.CurrentCycleCredit, NpgsqlTypes.NpgsqlDbType.Numeric, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.CurrentCycleDebit, NpgsqlTypes.NpgsqlDbType.Numeric, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.AddressZip, NpgsqlTypes.NpgsqlDbType.Varchar, cancellationToken).ConfigureAwait(false);
                await writer.WriteAsync(r.GroupId, NpgsqlTypes.NpgsqlDbType.Varchar, cancellationToken).ConfigureAwait(false);
                written++;
            }

            await writer.CompleteAsync(cancellationToken).ConfigureAwait(false);
        }

        return written;
    }

    /// <summary>Returns the current row count of the account table (reconciliation).</summary>
    public async Task<long> CountAsync(CancellationToken cancellationToken = default)
    {
        await using var connection = new NpgsqlConnection(_connectionString);
        await connection.OpenAsync(cancellationToken).ConfigureAwait(false);
        await using var command = new NpgsqlCommand("SELECT COUNT(*) FROM account", connection);
        var result = await command.ExecuteScalarAsync(cancellationToken).ConfigureAwait(false);
        return Convert.ToInt64(result);
    }
}
