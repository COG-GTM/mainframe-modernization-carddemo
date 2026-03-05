import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { addTransaction } from '../services/api';

/**
 * Add transaction form — replaces COTRN02.bms screen map.
 * Creates a new daily transaction for batch processing.
 */
export function TransactionAdd() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    cardNum: '',
    transactionType: '',
    transactionCategoryCode: '',
    transactionSource: '',
    transactionDescription: '',
    transactionAmount: '',
    merchantId: '',
    merchantName: '',
    merchantCity: '',
    merchantZip: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (field: string, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await addTransaction({
        ...form,
        transactionAmount: parseFloat(form.transactionAmount),
      });
      setSuccess('Transaction added successfully. It will be processed in the next batch run.');
      setForm({
        cardNum: '', transactionType: '', transactionCategoryCode: '',
        transactionSource: '', transactionDescription: '', transactionAmount: '',
        merchantId: '', merchantName: '', merchantCity: '', merchantZip: '',
      });
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Add Transaction</h1>
        <div className="subtitle">COTRN02 - New Transaction Entry</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {success && <div className="message success">{success}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Card Number:</label>
          <input type="text" value={form.cardNum} onChange={e => handleChange('cardNum', e.target.value)} maxLength={16} required />
        </div>
        <div className="form-group">
          <label>Transaction Type:</label>
          <input type="text" value={form.transactionType} onChange={e => handleChange('transactionType', e.target.value)} maxLength={2} required />
        </div>
        <div className="form-group">
          <label>Category Code:</label>
          <input type="text" value={form.transactionCategoryCode} onChange={e => handleChange('transactionCategoryCode', e.target.value)} maxLength={4} required />
        </div>
        <div className="form-group">
          <label>Source:</label>
          <input type="text" value={form.transactionSource} onChange={e => handleChange('transactionSource', e.target.value)} maxLength={10} />
        </div>
        <div className="form-group">
          <label>Description:</label>
          <input type="text" value={form.transactionDescription} onChange={e => handleChange('transactionDescription', e.target.value)} maxLength={100} />
        </div>
        <div className="form-group">
          <label>Amount:</label>
          <input type="number" step="0.01" value={form.transactionAmount} onChange={e => handleChange('transactionAmount', e.target.value)} required />
        </div>
        <div className="form-group">
          <label>Merchant ID:</label>
          <input type="text" value={form.merchantId} onChange={e => handleChange('merchantId', e.target.value)} maxLength={9} />
        </div>
        <div className="form-group">
          <label>Merchant Name:</label>
          <input type="text" value={form.merchantName} onChange={e => handleChange('merchantName', e.target.value)} maxLength={30} />
        </div>
        <div className="form-group">
          <label>Merchant City:</label>
          <input type="text" value={form.merchantCity} onChange={e => handleChange('merchantCity', e.target.value)} maxLength={20} />
        </div>
        <div className="form-group">
          <label>Merchant Zip:</label>
          <input type="text" value={form.merchantZip} onChange={e => handleChange('merchantZip', e.target.value)} maxLength={10} />
        </div>
        <div className="button-bar">
          <button type="button" className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
          <button type="submit" className="btn btn-primary">Enter - Submit</button>
        </div>
      </form>
    </div>
  );
}
