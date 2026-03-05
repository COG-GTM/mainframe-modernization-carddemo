import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getUser } from '../services/api';
import { User } from '../types';

/**
 * User detail — replaces COUSR01.bms screen map.
 * Displays user information.
 */
export function UserDetail() {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const data = await getUser(userId!);
      setUser(data);
    } catch (err: any) {
      setError(err.message);
    }
  };

  useEffect(() => { load(); }, [userId]);

  return (
    <div className="page">
      <div className="page-header">
        <h1>User Detail</h1>
        <div className="subtitle">COUSR01 - User Information</div>
      </div>
      {error && <div className="message error">{error}</div>}
      {user && (
        <div className="detail-grid">
          <span className="detail-label">User ID:</span>
          <span className="detail-value">{user.userId}</span>
          <span className="detail-label">First Name:</span>
          <span className="detail-value">{user.firstName}</span>
          <span className="detail-label">Last Name:</span>
          <span className="detail-value">{user.lastName}</span>
          <span className="detail-label">User Type:</span>
          <span className="detail-value">{user.userType}</span>
        </div>
      )}
      <div className="button-bar">
        <button className="btn" onClick={() => navigate(-1)}>PF3 - Back</button>
        <button className="btn" onClick={load}>PF5 - Refresh</button>
        <Link to={`/admin/users/${userId}/edit`} className="btn btn-primary">Enter - Edit</Link>
      </div>
    </div>
  );
}
