import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getUser, updateUser } from '../services/api';

/**
 * Update user form — replaces COUSR03.bms screen map.
 * Allows updating user details and role.
 */
export function UserUpdate() {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    password: '',
    userType: 'USER' as string,
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    getUser(userId!).then(user => {
      setForm({
        firstName: user.firstName,
        lastName: user.lastName,
        password: '',
        userType: user.userType,
      });
    }).catch(e => setError(e.message));
  }, [userId]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      const data: any = {
        firstName: form.firstName,
        lastName: form.lastName,
        userType: form.userType,
      };
      if (form.password) data.password = form.password;
      await updateUser(userId!, data);
      setSuccess('User updated successfully');
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Update User</h1>
        <div className="subtitle">COUSR03 - Edit User: {userId}</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {success && <div className="message success">{success}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>User ID:</label>
          <input type="text" value={userId} readOnly />
        </div>
        <div className="form-group">
          <label>First Name:</label>
          <input type="text" value={form.firstName} onChange={e => setForm(p => ({ ...p, firstName: e.target.value }))} maxLength={20} />
        </div>
        <div className="form-group">
          <label>Last Name:</label>
          <input type="text" value={form.lastName} onChange={e => setForm(p => ({ ...p, lastName: e.target.value }))} maxLength={20} />
        </div>
        <div className="form-group">
          <label>Password (leave blank to keep):</label>
          <input type="password" value={form.password} onChange={e => setForm(p => ({ ...p, password: e.target.value }))} />
        </div>
        <div className="form-group">
          <label>User Type:</label>
          <select value={form.userType} onChange={e => setForm(p => ({ ...p, userType: e.target.value }))}>
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
