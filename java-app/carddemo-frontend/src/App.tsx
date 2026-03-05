import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LoginPage } from './pages/LoginPage';
import { MainMenu } from './pages/MainMenu';
import { AdminMenu } from './pages/AdminMenu';
import { AccountView } from './pages/AccountView';
import { AccountUpdate } from './pages/AccountUpdate';
import { CardList } from './pages/CardList';
import { CardDetail } from './pages/CardDetail';
import { CardUpdate } from './pages/CardUpdate';
import { TransactionList } from './pages/TransactionList';
import { TransactionDetail } from './pages/TransactionDetail';
import { TransactionAdd } from './pages/TransactionAdd';
import { BillPayment } from './pages/BillPayment';
import { ReportRequest } from './pages/ReportRequest';
import { UserList } from './pages/UserList';
import { UserDetail } from './pages/UserDetail';
import { UserAdd } from './pages/UserAdd';
import { UserUpdate } from './pages/UserUpdate';
import './App.css';

/**
 * Main application component — replaces BMS screen map navigation.
 * PF key behaviors mapped to buttons: PF3=Back, PF5=Refresh, PF7=Page Up, PF8=Page Down, Enter=Submit
 */
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/menu" element={<MainMenu />} />
        <Route path="/admin" element={<AdminMenu />} />
        <Route path="/accounts/:acctId" element={<AccountView />} />
        <Route path="/accounts/:acctId/edit" element={<AccountUpdate />} />
        <Route path="/accounts/:acctId/cards" element={<CardList />} />
        <Route path="/cards/:cardNum" element={<CardDetail />} />
        <Route path="/cards/:cardNum/edit" element={<CardUpdate />} />
        <Route path="/accounts/:acctId/transactions" element={<TransactionList />} />
        <Route path="/transactions/:transactionId" element={<TransactionDetail />} />
        <Route path="/transactions/add" element={<TransactionAdd />} />
        <Route path="/accounts/:acctId/bill-payment" element={<BillPayment />} />
        <Route path="/reports" element={<ReportRequest />} />
        <Route path="/admin/users" element={<UserList />} />
        <Route path="/admin/users/:userId" element={<UserDetail />} />
        <Route path="/admin/users/add" element={<UserAdd />} />
        <Route path="/admin/users/:userId/edit" element={<UserUpdate />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
