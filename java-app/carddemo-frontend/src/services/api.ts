/**
 * API service module — centralizes all REST calls to the Spring Boot backend.
 * The proxy in package.json forwards /api/* to http://localhost:8080.
 */
import {
  Account, Authorization, Card, Customer, LoginRequest, LoginResponse,
  Page, ReportRequest, Transaction, TransactionType, User
} from '../types';

const BASE = '/api';

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${url}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const body = await res.text();
    throw new Error(body || `HTTP ${res.status}`);
  }
  return res.json();
}

// Auth
export const login = (data: LoginRequest) =>
  request<LoginResponse>('/auth/login', { method: 'POST', body: JSON.stringify(data) });

export const logout = () =>
  request<void>('/auth/logout', { method: 'POST' });

// Accounts
export const getAccount = (acctId: number) =>
  request<Account>(`/accounts/${acctId}`);

export const updateAccount = (acctId: number, data: Partial<Account>) =>
  request<Account>(`/accounts/${acctId}`, { method: 'PUT', body: JSON.stringify(data) });

// Cards
export const listCards = (acctId: number, page = 0, size = 10) =>
  request<Page<Card>>(`/accounts/${acctId}/cards?page=${page}&size=${size}`);

export const getCard = (cardNum: string) =>
  request<Card>(`/cards/${cardNum}`);

export const updateCard = (cardNum: string, data: Partial<Card>) =>
  request<Card>(`/cards/${cardNum}`, { method: 'PUT', body: JSON.stringify(data) });

// Transactions
export const listTransactions = (acctId: number, page = 0, size = 10) =>
  request<Page<Transaction>>(`/accounts/${acctId}/transactions?page=${page}&size=${size}`);

export const getTransaction = (transactionId: string) =>
  request<Transaction>(`/transactions/${transactionId}`);

export const addTransaction = (data: Partial<Transaction>) =>
  request<Transaction>('/transactions', { method: 'POST', body: JSON.stringify(data) });

// Bill Payment
export const payBill = (acctId: number) =>
  request<void>(`/accounts/${acctId}/bill-payment`, { method: 'POST' });

// Reports
export const submitReport = (data: ReportRequest) =>
  request<{ reportId: string }>('/reports/transaction-report', { method: 'POST', body: JSON.stringify(data) });

export const getReport = (reportId: string) =>
  request<{ reportId: string; status: string }>(`/reports/${reportId}`);

// User Admin
export const listUsers = (page = 0, size = 10) =>
  request<Page<User>>(`/admin/users?page=${page}&size=${size}`);

export const getUser = (userId: string) =>
  request<User>(`/admin/users/${userId}`);

export const addUser = (data: Partial<User>) =>
  request<User>('/admin/users', { method: 'POST', body: JSON.stringify(data) });

export const updateUser = (userId: string, data: Partial<User>) =>
  request<User>(`/admin/users/${userId}`, { method: 'PUT', body: JSON.stringify(data) });

export const deleteUser = (userId: string) =>
  request<void>(`/admin/users/${userId}`, { method: 'DELETE' });

// Transaction Types
export const listTransactionTypes = (page = 0, size = 10) =>
  request<Page<TransactionType>>(`/admin/transaction-types?page=${page}&size=${size}`);

export const addTransactionType = (data: Partial<TransactionType>) =>
  request<TransactionType>('/admin/transaction-types', { method: 'POST', body: JSON.stringify(data) });

export const updateTransactionType = (typeCode: string, data: Partial<TransactionType>) =>
  request<TransactionType>(`/admin/transaction-types/${typeCode}`, { method: 'PUT', body: JSON.stringify(data) });

export const deleteTransactionType = (typeCode: string) =>
  request<void>(`/admin/transaction-types/${typeCode}`, { method: 'DELETE' });

// Authorizations
export const listAuthorizations = (cardNum: string, page = 0, size = 10) =>
  request<Page<Authorization>>(`/authorizations?cardNum=${cardNum}&page=${page}&size=${size}`);

export const getAuthorization = (authId: number) =>
  request<Authorization>(`/authorizations/${authId}`);

export const markFraud = (authId: number) =>
  request<void>(`/authorizations/${authId}/fraud`, { method: 'PUT' });

export const processAuthorization = (data: { cardNum: string; amount: number; merchantId: string; authType: string }) =>
  request<Authorization>('/authorizations', { method: 'POST', body: JSON.stringify(data) });
