import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getAccount, payBill } from '../services/api';
import { Account } from '../types';

/**
 * Bill payment — replaces COBIL00.bms screen map.
 * Pays the full account balance and creates a payment transaction.
 */
export function BillPayment() {
  const { acctId } = useParams<{ acctId: string }>();
  const navigate = useNavigate();
  const [account, setAccount] = useState<Account | null>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    getAccount(Number(acctId)).then(setAccount).catch(e => setError(e.message));
  }, [acctId]);

  const fmt = (n: number) => n?.toLocaleString('en-US', { style: 'currency', currency: 'USD' });

  const handlePay = async () => {
    setError('');
    setSuccess('');
    setProcessing(true);
    try {
      await payBill(Number(acctId));
      setSuccess('Bill payment processed successfully. Balance is now $0.00.');
      const updated = await getAccount(Number(acctId));
      setAccount(updated);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Bill Payment</h1>
        <div className="subtitle">COBIL00 - Pay Account Balance</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {success && <div className="message success">{success}</div>}
      {account && (
        <div className="detail-grid">
          <span className="detail-label">Account ID:</span>
          <span className="detail-value">{account.acctId}</span>
          <span className="detail-label">Current Balance:</span>
          <span className="detail-value currency">{fmt(account.currentBalance)}</span>
          <span className="detail-label">Credit Limit:</span>
          <span className="detail-value currency">{fmt(account.creditLimit)}</span>
          <span className="detail-label">Payment Amount:</span>
          <span className="detail-value currency" style={{ fontWeight: 'bold' }}>{fmt(account.currentBalance)}</span>
        </div>
      )}
      <div style={{ padding: '15px 0', color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
        This will pay the full current balance for the account.
      </div>
      <div className="button-bar">
        <button className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
        <button className="btn btn-primary" onClick={handlePay} disabled={processing}>
          {processing ? 'Processing...' : 'Enter - Pay Bill'}
        </button>
      </div>
    </div>
  );
}
