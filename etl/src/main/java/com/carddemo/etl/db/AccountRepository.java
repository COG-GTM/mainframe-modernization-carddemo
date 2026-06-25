package com.carddemo.etl.db;

import com.carddemo.etl.model.Account;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

/**
 * Loads {@link Account} rows into the {@code accounts} table using JDBC batch upserts.
 *
 * <p>Inserts are idempotent: a conflict on {@code acct_id} updates the existing row and refreshes
 * {@code updated_at}, so re-running the ETL over the same dataset converges to the same state.
 */
public final class AccountRepository {

    private static final String UPSERT_SQL = """
            INSERT INTO accounts (
                acct_id, active_status, curr_bal, credit_limit, cash_credit_limit,
                open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit,
                addr_zip, group_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (acct_id) DO UPDATE SET
                active_status     = EXCLUDED.active_status,
                curr_bal          = EXCLUDED.curr_bal,
                credit_limit      = EXCLUDED.credit_limit,
                cash_credit_limit = EXCLUDED.cash_credit_limit,
                open_date         = EXCLUDED.open_date,
                expiration_date   = EXCLUDED.expiration_date,
                reissue_date      = EXCLUDED.reissue_date,
                curr_cyc_credit   = EXCLUDED.curr_cyc_credit,
                curr_cyc_debit    = EXCLUDED.curr_cyc_debit,
                addr_zip          = EXCLUDED.addr_zip,
                group_id          = EXCLUDED.group_id,
                updated_at        = now()
            """;

    private final Connection connection;

    public AccountRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Upserts a batch of accounts in a single transaction.
     *
     * @return the number of rows affected (inserted or updated)
     */
    public int upsertBatch(List<Account> accounts) throws SQLException {
        if (accounts.isEmpty()) {
            return 0;
        }
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement ps = connection.prepareStatement(UPSERT_SQL)) {
            for (Account account : accounts) {
                bind(ps, account);
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            connection.commit();
            return countAffected(results);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private static void bind(PreparedStatement ps, Account account) throws SQLException {
        ps.setLong(1, account.acctId());
        ps.setString(2, account.activeStatus());
        ps.setBigDecimal(3, account.currBal());
        ps.setBigDecimal(4, account.creditLimit());
        ps.setBigDecimal(5, account.cashCreditLimit());
        setDate(ps, 6, account.openDate());
        setDate(ps, 7, account.expirationDate());
        setDate(ps, 8, account.reissueDate());
        ps.setBigDecimal(9, account.currCycCredit());
        ps.setBigDecimal(10, account.currCycDebit());
        setString(ps, 11, account.addrZip());
        setString(ps, 12, account.groupId());
    }

    private static void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }

    private static void setString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private static int countAffected(int[] results) {
        int total = 0;
        for (int result : results) {
            if (result == java.sql.Statement.SUCCESS_NO_INFO) {
                total += 1;
            } else if (result >= 0) {
                total += result;
            }
        }
        return total;
    }
}
