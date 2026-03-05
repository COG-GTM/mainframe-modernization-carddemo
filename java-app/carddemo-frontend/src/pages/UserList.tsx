import React, { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { listUsers, deleteUser } from '../services/api';
import { User, Page } from '../types';

/**
 * User list — replaces COUSR00.bms screen map.
 * Displays paginated table of users (admin only).
 */
export function UserList() {
  const navigate = useNavigate();
  const [page, setPage] = useState<Page<User> | null>(null);
  const [pageNum, setPageNum] = useState(0);
  const [error, setError] = useState('');

  const load = async (p: number) => {
    try {
      const data = await listUsers(p);
      setPage(data);
      setPageNum(p);
    } catch (err: any) {
      setError(err.message);
    }
  };

  useEffect(() => { load(0); }, []);

  const handleDelete = async (userId: string) => {
    if (!window.confirm(`Delete user ${userId}?`)) return;
    try {
      await deleteUser(userId);
      load(pageNum);
    } catch (err: any) {
      setError(err.message);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>User Administration</h1>
        <div className="subtitle">COUSR00 - User List</div>
      </div>
      {error && <div className="message error">{error}</div>}
      <table className="data-table">
        <thead>
          <tr>
            <th>User ID</th>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Type</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {page?.content.map(user => (
            <tr key={user.userId}>
              <td><Link to={`/admin/users/${user.userId}`}>{user.userId}</Link></td>
              <td>{user.firstName}</td>
              <td>{user.lastName}</td>
              <td>{user.userType}</td>
              <td>
                <Link to={`/admin/users/${user.userId}/edit`} className="btn" style={{ padding: '2px 8px', fontSize: '0.75rem', marginRight: 5 }}>Edit</Link>
                <button className="btn btn-danger" style={{ padding: '2px 8px', fontSize: '0.75rem' }} onClick={() => handleDelete(user.userId)}>Del</button>
              </td>
            </tr>
          ))}
          {page?.content.length === 0 && (
            <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No users found</td></tr>
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
        <Link to="/admin/users/add" className="btn btn-primary">Add User</Link>
      </div>
    </div>
  );
}
