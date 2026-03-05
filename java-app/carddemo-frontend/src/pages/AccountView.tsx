import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getAccount } from '../services/api';
import { Account } from '../types';

/**
 * Account view — replaces COACTVW.bms screen map.
 * Displays account details including balances and dates.
 */
export function AccountView() {
  const { acctId } = useParams<{ acctId: string }>();
  const navigate = useNavigate();
  const [account, setAccount] = useState<Account | null>(null);
  const [error, setError] = useState('');

  const loadAccount = async () => {
    try {
      const data = await getAccount(Number(acctId));
      setAccount(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  useEffect(() => { loadAccount(); }, [acctId]);

  const fmt = (n: number) => n?.toLocaleString('en-US', { style: 'currency', currency: 'USD' });

  return (
    <div className="page">
      <div className="page-header">
        <h1>Account View</h1>
        <div className="subtitle">COACTVW - Account Detail Display</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {account && (
        <div className="detail-grid">
          <span className="detail-label">Account ID:</span>
          <span className="detail-value">{account.acctId}</span>
          <span className="detail-label">Status:</span>
          <span className="detail-value">{account.activeStatus}</span>
          <span className="detail-label">Current Balance:</span>
          <span className="detail-value currency">{fmt(account.currentBalance)}</span>
          <span className="detail-label">Credit Limit:</span>
          <span className="detail-value currency">{fmt(account.creditLimit)}</span>
          <span className="detail-label">Cash Credit Limit:</span>
          <span className="detail-value currency">{fmt(account.cashCreditLimit)}</span>
          <span className="detail-label">Open Date:</span>
          <span className="detail-value">{account.openDate}</span>
          <span className="detail-label">Expiration Date:</span>
          <span className="detail-value">{account.expirationDate}</span>
          <span className="detail-label">Reissue Date:</span>
          <span className="detail-value">{account.reissueDate}</span>
          <span className="detail-label">Current Cycle Credit:</span>
          <span className="detail-value currency">{fmt(account.currentCycleCredit)}</span>
          <span className="detail-label">Current Cycle Debit:</span>
          <span className="detail-value currency">{fmt(account.currentCycleDebit)}</span>
          <span className="detail-label">Group ID:</span>
          <span className="detail-value">{account.groupId}</span>
        </div>
      )}
      <div className="button-bar">
        <button className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
        <button className="btn" onClick={loadAccount}>PF5 - Refresh</button>
        <Link to={`/accounts/${acctId}/edit`} className="btn btn-primary">Enter - Edit</Link>
      </div>
    </div>
  );
}
