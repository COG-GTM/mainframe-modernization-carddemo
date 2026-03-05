import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getTransaction } from '../services/api';
import { Transaction } from '../types';

/**
 * Transaction detail — replaces COTRN01.bms screen map.
 * Displays full transaction information.
 */
export function TransactionDetail() {
  const { transactionId } = useParams<{ transactionId: string }>();
  const navigate = useNavigate();
  const [txn, setTxn] = useState<Transaction | null>(null);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const data = await getTransaction(transactionId!);
      setTxn(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  useEffect(() => { load(); }, [transactionId]);

  const fmt = (n: number) => n?.toLocaleString('en-US', { style: 'currency', currency: 'USD' });

  return (
    <div className="page">
      <div className="page-header">
        <h1>Transaction Detail</h1>
        <div className="subtitle">COTRN01 - Transaction Information</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {txn && (
        <div className="detail-grid">
          <span className="detail-label">Transaction ID:</span>
          <span className="detail-value">{txn.transactionId}</span>
          <span className="detail-label">Card Number:</span>
          <span className="detail-value">{txn.cardNum}</span>
          <span className="detail-label">Type:</span>
          <span className="detail-value">{txn.transactionType}</span>
          <span className="detail-label">Category:</span>
          <span className="detail-value">{txn.transactionCategoryCode}</span>
          <span className="detail-label">Source:</span>
          <span className="detail-value">{txn.transactionSource}</span>
          <span className="detail-label">Description:</span>
          <span className="detail-value">{txn.transactionDescription}</span>
          <span className="detail-label">Amount:</span>
          <span className="detail-value currency">{fmt(txn.transactionAmount)}</span>
          <span className="detail-label">Timestamp:</span>
          <span className="detail-value">{txn.transactionTimestamp}</span>
          <span className="detail-label">Merchant ID:</span>
          <span className="detail-value">{txn.merchantId}</span>
          <span className="detail-label">Merchant Name:</span>
          <span className="detail-value">{txn.merchantName}</span>
          <span className="detail-label">Merchant City:</span>
          <span className="detail-value">{txn.merchantCity}</span>
          <span className="detail-label">Merchant Zip:</span>
          <span className="detail-value">{txn.merchantZip}</span>
        </div>
      )}
      <div className="button-bar">
        <button className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
        <button className="btn" onClick={load}>PF5 - Refresh</button>
      </div>
    </div>
  );
}
