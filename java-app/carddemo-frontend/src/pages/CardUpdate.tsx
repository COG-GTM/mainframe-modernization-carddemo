import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getCard, updateCard } from '../services/api';
import { Card } from '../types';

/**
 * Card update form — replaces COCRDUP.bms screen map.
 * Allows updating card status and embossed name.
 */
export function CardUpdate() {
  const { cardNum } = useParams<{ cardNum: string }>();
  const navigate = useNavigate();
  const [form, setForm] = useState<Partial<Card>>({});
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    getCard(cardNum!).then(setForm).catch(e => setError(e.message));
  }, [cardNum]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await updateCard(cardNum!, form);
      setSuccess('Card updated successfully');
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Card Update</h1>
        <div className="subtitle">COCRDUP - Update Card Information</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {success && <div className="message success">{success}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Card Number:</label>
          <input type="text" value={form.cardNum || ''} readOnly />
        </div>
        <div className="form-group">
          <label>Account ID:</label>
          <input type="text" value={form.acctId || ''} readOnly />
        </div>
        <div className="form-group">
          <label>Status:</label>
          <select value={form.activeStatus || ''} onChange={e => setForm(p => ({ ...p, activeStatus: e.target.value }))}>
            <option value="Y">Active</option>
            <option value="N">Inactive</option>
          </select>
        </div>
        <div className="form-group">
          <label>Embossed Name:</label>
          <input type="text" value={form.embossedName || ''} onChange={e => setForm(p => ({ ...p, embossedName: e.target.value }))} maxLength={50} />
        </div>
        <div className="button-bar">
          <button type="button" className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
          <button type="submit" className="btn btn-primary">Enter - Submit</button>
        </div>
      </form>
    </div>
  );
}
