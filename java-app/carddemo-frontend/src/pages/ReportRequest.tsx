import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { submitReport } from '../services/api';

/**
 * Report request — replaces CORPT00.bms screen map.
 * Submits a transaction report request for async batch generation.
 */
export function ReportRequest() {
  const navigate = useNavigate();
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [acctId, setAcctId] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const result = await submitReport({
        startDate,
        endDate,
        acctId: acctId ? Number(acctId) : undefined,
      });
      setSuccess(`Report submitted successfully. Report ID: ${result.reportId}`);
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Transaction Report</h1>
        <div className="subtitle">CORPT00 - Request Transaction Report</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {success && <div className="message success">{success}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Start Date:</label>
          <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>End Date:</label>
          <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>Account ID (optional):</label>
          <input type="number" value={acctId} onChange={e => setAcctId(e.target.value)} />
        </div>
        <div className="button-bar">
          <button type="button" className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
          <button type="submit" className="btn btn-primary">Enter - Submit</button>
        </div>
      </form>
    </div>
  );
}
