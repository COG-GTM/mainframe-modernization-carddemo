import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getCard } from '../services/api';
import { Card } from '../types';

/**
 * Card detail — replaces COCRDSL.bms screen map.
 * Displays card information with option to edit.
 */
export function CardDetail() {
  const { cardNum } = useParams<{ cardNum: string }>();
  const navigate = useNavigate();
  const [card, setCard] = useState<Card | null>(null);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const data = await getCard(cardNum!);
      setCard(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  useEffect(() => { load(); }, [cardNum]);

  return (
    <div className="page">
      <div className="page-header">
        <h1>Card Detail</h1>
        <div className="subtitle">COCRDSL - Card Information</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {card && (
        <div className="detail-grid">
          <span className="detail-label">Card Number:</span>
          <span className="detail-value">{card.cardNum}</span>
          <span className="detail-label">Account ID:</span>
          <span className="detail-value">{card.acctId}</span>
          <span className="detail-label">Status:</span>
          <span className="detail-value">{card.activeStatus}</span>
          <span className="detail-label">Embossed Name:</span>
          <span className="detail-value">{card.embossedName}</span>
        </div>
      )}
      <div className="button-bar">
        <button className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
        <button className="btn" onClick={load}>PF5 - Refresh</button>
        <Link to={`/cards/${cardNum}/edit`} className="btn btn-primary">Enter - Edit</Link>
      </div>
    </div>
  );
}
