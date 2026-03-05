import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

/**
 * Admin menu — replaces COADM01.bms screen map.
 * Provides navigation to all screens plus user administration.
 */
export function AdminMenu() {
  const navigate = useNavigate();
  const user = JSON.parse(sessionStorage.getItem('user') || '{}');

  const handleLogout = () => {
    sessionStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Admin Menu</h1>
        <div className="subtitle">Welcome, {user.firstName} {user.lastName} (Administrator)</div>
      </div>
      <ul className="menu-list">
        <li><Link to="/accounts/1">01. View Account</Link></li>
        <li><Link to="/accounts/1/edit">02. Update Account</Link></li>
        <li><Link to="/accounts/1/cards">03. View Cards</Link></li>
        <li><Link to="/accounts/1/transactions">04. View Transactions</Link></li>
        <li><Link to="/transactions/add">05. Add Transaction</Link></li>
        <li><Link to="/accounts/1/bill-payment">06. Bill Payment</Link></li>
        <li><Link to="/reports">07. Transaction Report</Link></li>
        <li><Link to="/admin/users">08. User Administration</Link></li>
      </ul>
      <div className="button-bar">
        <button className="btn btn-danger" onClick={handleLogout}>PF3 - Logout</button>
      </div>
    </div>
  );
}
