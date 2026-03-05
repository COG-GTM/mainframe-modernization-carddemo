import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

/**
 * Main menu for regular users — replaces COMEN01.bms screen map.
 * Provides navigation to account, card, transaction, and payment screens.
 */
export function MainMenu() {
  const navigate = useNavigate();
  const user = JSON.parse(sessionStorage.getItem('user') || '{}');

  const handleLogout = () => {
    sessionStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Main Menu</h1>
        <div className="subtitle">Welcome, {user.firstName} {user.lastName}</div>
      </div>
      <ul className="menu-list">
        <li><Link to="/accounts/1">01. View Account</Link></li>
        <li><Link to="/accounts/1/edit">02. Update Account</Link></li>
        <li><Link to="/accounts/1/cards">03. View Cards</Link></li>
        <li><Link to="/accounts/1/transactions">04. View Transactions</Link></li>
        <li><Link to="/transactions/add">05. Add Transaction</Link></li>
        <li><Link to="/accounts/1/bill-payment">06. Bill Payment</Link></li>
        <li><Link to="/reports">07. Transaction Report</Link></li>
      </ul>
      <div className="button-bar">
        <button className="btn btn-danger" onClick={handleLogout}>PF3 - Logout</button>
      </div>
    </div>
  );
}
