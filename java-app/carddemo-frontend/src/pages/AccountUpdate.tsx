import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getAccount, updateAccount } from '../services/api';
import { Account } from '../types';

/**
 * Account update form — replaces COACTUP.bms screen map.
 * Allows editing account fields with validation.
 */
export function AccountUpdate() {
  const { acctId } = useParams<{ acctId: string }>();
  const navigate = useNavigate();
  const [form, setForm] = useState<Partial<Account>>({});
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    getAccount(Number(acctId)).then(setForm).catch(e => setError(e.message));
  }, [acctId]);

  const handleChange = (field: keyof Account, value: string) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await updateAccount(Number(acctId), form);
      setSuccess('Account updated successfully');
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Account Update</h1>
        <div className="subtitle">COACTUP - Account Update Form</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {success && <div className="message success">{success}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Account ID:</label>
          <input type="text" value={form.acctId || ''} readOnly />
        </div>
        <div className="form-group">
          <label>Status:</label>
          <input type="text" value={form.activeStatus || ''} onChange={e => handleChange('activeStatus', e.target.value)} maxLength={1} />
        </div>
        <div className="form-group">
          <label>Credit Limit:</label>
          <input type="text" value={form.creditLimit || ''} onChange={e => handleChange('creditLimit', e.target.value)} />
        </div>
        <div className="form-group">
          <label>Cash Credit Limit:</label>
          <input type="text" value={form.cashCreditLimit || ''} onChange={e => handleChange('cashCreditLimit', e.target.value)} />
        </div>
        <div className="form-group">
          <label>Expiration Date:</label>
          <input type="date" value={form.expirationDate || ''} onChange={e => handleChange('expirationDate', e.target.value)} />
        </div>
        <div className="form-group">
          <label>Reissue Date:</label>
          <input type="date" value={form.reissueDate || ''} onChange={e => handleChange('reissueDate', e.target.value)} />
        </div>
        <div className="form-group">
          <label>Group ID:</label>
          <input type="text" value={form.groupId || ''} onChange={e => handleChange('groupId', e.target.value)} maxLength={10} />
        </div>
        <div className="button-bar">
          <button type="button" className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
          <button type="submit" className="btn btn-primary">Enter - Submit</button>
        </div>
      </form>
    </div>
  );
}
