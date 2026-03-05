import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { listTransactions } from '../services/api';
import { Transaction, Page } from '../types';

/**
 * Transaction list — replaces COTRN00.bms screen map.
 * Displays paginated table of transactions for an account.
 */
export function TransactionList() {
  const { acctId } = useParams<{ acctId: string }>();
  const navigate = useNavigate();
  const [page, setPage] = useState<Page<Transaction> | null>(null);
  const [pageNum, setPageNum] = useState(0);
  const [error, setError] = useState('');

  const load = async (p: number) => {
    try {
      const data = await listTransactions(Number(acctId), p);
      setPage(data);
      setPageNum(p);
    } catch (err: any) {
      setError(err.message);
    }
  };

  useEffect(() => { load(0); }, [acctId]);

  const fmt = (n: number) => n?.toLocaleString('en-US', { style: 'currency', currency: 'USD' });

  return (
    <div className="page">
      <div className="page-header">
        <h1>Transaction List</h1>
        <div className="subtitle">COTRN00 - Transactions for Account {acctId}</div>
      </div>
      {error && <div className="message error">{error}</div>}
      <table className="data-table">
        <thead>
          <tr>
            <th>Transaction ID</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Timestamp</th>
            <th>Description</th>
          </tr>
        </thead>
        <tbody>
          {page?.content.map(txn => (
            <tr key={txn.transactionId}>
              <td><Link to={`/transactions/${txn.transactionId}`}>{txn.transactionId}</Link></td>
              <td>{txn.transactionType}</td>
              <td style={{ color: 'var(--success)' }}>{fmt(txn.transactionAmount)}</td>
              <td>{txn.transactionTimestamp}</td>
              <td>{txn.transactionDescription}</td>
            </tr>
          ))}
          {page?.content.length === 0 && (
            <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No transactions found</td></tr>
          )}
        </tbody>
      </table>
      <div className="pagination">
        <button className="btn" disabled={page?.first} onClick={() => load(pageNum - 1)}>PF7 - Prev</button>
        <span>Page {(page?.number ?? 0) + 1} of {page?.totalPages ?? 0}</span>
        <button className="btn" disabled={page?.last} onClick={() => load(pageNum + 1)}>PF8 - Next</button>
      </div>
      <div className="button-bar">
        <button className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
        <button className="btn" onClick={() => load(pageNum)}>PF5 - Refresh</button>
        <Link to="/transactions/add" className="btn btn-primary">Add Transaction</Link>
      </div>
    </div>
  );
}
