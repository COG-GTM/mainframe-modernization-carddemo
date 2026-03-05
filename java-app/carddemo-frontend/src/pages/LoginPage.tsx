import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../services/api';

/**
 * Login page — replaces COSGN0A.bms screen map.
 * Authenticates user and redirects to appropriate menu based on role.
 */
export function LoginPage() {
  const navigate = useNavigate();
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const result = await login({ userId, password });
      sessionStorage.setItem('user', JSON.stringify(result));
      if (result.userType === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/menu');
      }
    } catch (err: any) {
      setError(err.message || 'Login failed');
    }
  };

  return (
    <div className="login-container">
      <h1>CardDemo</h1>
      <p style={{ textAlign: 'center', color: 'var(--text-muted)', marginBottom: 20, fontSize: '0.8rem' }}>
        Credit Card Management System
      </p>
      {error && <div className="message error">{error}</div>}
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>User ID</label>
          <input
            type="text"
            value={userId}
            onChange={e => setUserId(e.target.value)}
            maxLength={8}
            autoFocus
          />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
          />
        </div>
        <div className="button-bar" style={{ borderTop: 'none', justifyContent: 'center' }}>
          <button type="submit" className="btn btn-primary">Enter</button>
        </div>
      </form>
    </div>
  );
}
