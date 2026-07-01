using CardDemo.Data.Database;
using CardDemo.Domain;
using Dapper;
using Npgsql;

namespace CardDemo.Data.Repositories;

/// <summary>
/// PostgreSQL-backed implementation of <see cref="IAccountRepository"/> using Dapper.
/// </summary>
public sealed class AccountRepository : IAccountRepository
{
    private const string SelectAll = """
        SELECT acct_id              AS AccountId,
               acct_active_status   AS ActiveStatus,
               acct_curr_bal        AS CurrentBalance,
               acct_credit_limit    AS CreditLimit,
               acct_cash_credit_limit AS CashCreditLimit,
               acct_open_date       AS OpenDate,
               acct_expiration_date AS ExpirationDate,
               acct_reissue_date    AS ReissueDate,
               acct_curr_cyc_credit AS CurrentCycleCredit,
               acct_curr_cyc_debit  AS CurrentCycleDebit,
               acct_addr_zip        AS AddressZip,
               acct_group_id        AS GroupId
        FROM account
        ORDER BY acct_id
        """;

    private const string UpsertSql = """
        INSERT INTO account (
            acct_id, acct_active_status, acct_curr_bal, acct_credit_limit,
            acct_cash_credit_limit, acct_open_date, acct_expiration_date,
            acct_reissue_date, acct_curr_cyc_credit, acct_curr_cyc_debit,
            acct_addr_zip, acct_group_id)
        VALUES (
            @AccountId, @ActiveStatus, @CurrentBalance, @CreditLimit,
            @CashCreditLimit, @OpenDate, @ExpirationDate,
            @ReissueDate, @CurrentCycleCredit, @CurrentCycleDebit,
            @AddressZip, @GroupId)
        ON CONFLICT (acct_id) DO UPDATE SET
            acct_active_status     = EXCLUDED.acct_active_status,
            acct_curr_bal          = EXCLUDED.acct_curr_bal,
            acct_credit_limit      = EXCLUDED.acct_credit_limit,
            acct_cash_credit_limit = EXCLUDED.acct_cash_credit_limit,
            acct_open_date         = EXCLUDED.acct_open_date,
            acct_expiration_date   = EXCLUDED.acct_expiration_date,
            acct_reissue_date      = EXCLUDED.acct_reissue_date,
            acct_curr_cyc_credit   = EXCLUDED.acct_curr_cyc_credit,
            acct_curr_cyc_debit    = EXCLUDED.acct_curr_cyc_debit,
            acct_addr_zip          = EXCLUDED.acct_addr_zip,
            acct_group_id          = EXCLUDED.acct_group_id
        """;

    private readonly DatabaseOptions _options;

    public AccountRepository(DatabaseOptions options)
    {
        _options = options ?? throw new ArgumentNullException(nameof(options));
    }

    public IEnumerable<AccountRecord> ReadAll()
    {
        using var connection = new NpgsqlConnection(_options.ConnectionString);
        connection.Open();
        // buffered:false streams rows one at a time, like a sequential file read.
        foreach (var account in connection.Query<AccountRecord>(SelectAll, buffered: false))
        {
            yield return account;
        }
    }

    public void Upsert(AccountRecord account)
    {
        ArgumentNullException.ThrowIfNull(account);
        using var connection = new NpgsqlConnection(_options.ConnectionString);
        connection.Open();
        connection.Execute(UpsertSql, account);
    }
}
