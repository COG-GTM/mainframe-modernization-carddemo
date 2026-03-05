import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { listCards } from '../services/api';
import { Card, Page } from '../types';

/**
 * Card list — replaces COCRDLI.bms screen map.
 * Displays paginated table of cards for an account.
 * PF7=Page Up, PF8=Page Down.
 */
export function CardList() {
  const { acctId } = useParams<{ acctId: string }>();
  const navigate = useNavigate();
  const [page, setPage] = useState<Page<Card> | null>(null);
  const [pageNum, setPageNum] = useState(0);
  const [error, setError] = useState('');

  const load = async (p: number) => {
    try {
      const data = await listCards(Number(acctId), p);
      setPage(data);
      setPageNum(p);
    } catch (err: any) {
      setError(err.message);
    }
  };

  useEffect(() => { load(0); }, [acctId]);

  return (
    <div className="page">
      <div className="page-header">
        <h1>Card List</h1>
        <div className="subtitle">COCRDLI - Cards for Account {acctId}</div>
      </div>
      {error && <div className="message error">{error}</div>}
      <table className="data-table">
        <thead>
          <tr>
            <th>Card Number</th>
            <th>Account ID</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {page?.content.map(card => (
            <tr key={card.cardNum}>
              <td><Link to={`/cards/${card.cardNum}`}>{card.cardNum}</Link></td>
              <td>{card.acctId}</td>
              <td>{card.activeStatus}</td>
              <td><Link to={`/cards/${card.cardNum}/edit`} className="btn" style={{ padding: '2px 8px', fontSize: '0.75rem' }}>Edit</Link></td>
            </tr>
          ))}
          {page?.content.length === 0 && (
            <tr><td colSpan={4} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No cards found</td></tr>
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
      </div>
    </div>
  );
}
