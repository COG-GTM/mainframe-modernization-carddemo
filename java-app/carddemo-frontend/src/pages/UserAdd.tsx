import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { addUser } from '../services/api';

/**
 * Add user form — replaces COUSR02.bms screen map.
 * Creates a new user with role assignment.
 */
export function UserAdd() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    userId: '',
    firstName: '',
    lastName: '',
    password: '',
    userType: 'USER' as 'ADMIN' | 'USER',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await addUser(form);
      setSuccess(`User ${form.userId} created successfully`);
      setForm({ userId: '', firstName: '', lastName: '', password: '', userType: 'USER' });
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Add User</h1>
        <div className="subtitle">COUSR02 - New User Entry</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {success && <div className="message success">{success}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>User ID:</label>
          <input type="text" value={form.userId} onChange={e => setForm(p => ({ ...p, userId: e.target.value }))} maxLength={8} required />
        </div>
        <div className="form-group">
          <label>First Name:</label>
          <input type="text" value={form.firstName} onChange={e => setForm(p => ({ ...p, firstName: e.target.value }))} maxLength={20} required />
        </div>
        <div className="form-group">
          <label>Last Name:</label>
          <input type="text" value={form.lastName} onChange={e => setForm(p => ({ ...p, lastName: e.target.value }))} maxLength={20} required />
        </div>
        <div className="form-group">
          <label>Password:</label>
          <input type="password" value={form.password} onChange={e => setForm(p => ({ ...p, password: e.target.value }))} required />
        </div>
        <div className="form-group">
          <label>User Type:</label>
          <select value={form.userType} onChange={e => setForm(p => ({ ...p, userType: e.target.value as 'ADMIN' | 'USER' }))}>
            <option value="USER">Regular User</option>
            <option value="ADMIN">Administrator</option>
          </select>
        </div>
        <div className="button-bar">
          <button type="button" className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
          <button type="submit" className="btn btn-primary">Enter - Submit</button>
        </div>
      </form>
    </div>
  );
}
