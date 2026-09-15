/** Client for the Express API in `../../src/online/api.ts`. */

export const SESSION_HEADER = 'x-carddemo-session';

/** A 401 mirrors CICS EIBCALEN = 0: the UI must return to the signon screen. */
export class SessionExpiredError extends Error {
  constructor() {
    super('Session expired');
    this.name = 'SessionExpiredError';
  }
}

/** The `400 {errorMessage}` shape every service uses for BMS ERRMSG text. */
export class ApiError extends Error {
  readonly cursorField: string | undefined;

  constructor(errorMessage: string, cursorField?: string) {
    super(errorMessage);
    this.name = 'ApiError';
    this.cursorField = cursorField;
  }
}

export interface SignonResponse {
  sessionId: string;
  program: string;
  route?: string;
  userType: string;
}

export interface MenuListResponse {
  options: { num: number; name: string; pgmName: string; usrType: string }[];
  labels: string[];
}

export interface MenuSelectResponse {
  program: string;
  route?: string;
  option: { num: number; name: string; pgmName: string; usrType: string };
}

/**
 * Monetary fields arrive as strings (`serializeRecord`) and stay strings —
 * never parse them into JS numbers.
 */
export type SerializedRecord = Record<string, string>;

export interface AccountViewResponse {
  account: SerializedRecord;
  customer: SerializedRecord;
  xref: SerializedRecord;
  infoMessage: string;
}

async function request<T>(path: string, init: RequestInit, sessionId?: string): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body !== undefined) headers.set('content-type', 'application/json');
  if (sessionId !== undefined) headers.set(SESSION_HEADER, sessionId);

  const response = await fetch(path, { ...init, headers });
  if (response.status === 401) throw new SessionExpiredError();

  const payload = (await response.json()) as Partial<{
    errorMessage: string;
    cursorField: string;
  }>;

  if (!response.ok) {
    throw new ApiError(payload.errorMessage ?? 'Unexpected error', payload.cursorField);
  }
  return payload as T;
}

export function signon(userId: string, password: string): Promise<SignonResponse> {
  return request<SignonResponse>('/signon', {
    method: 'POST',
    body: JSON.stringify({ userId, password }),
  });
}

export function signoff(sessionId: string): Promise<unknown> {
  return request<unknown>('/signoff', { method: 'POST' }, sessionId);
}

export function listMenu(sessionId: string): Promise<MenuListResponse> {
  return request<MenuListResponse>('/menu', { method: 'GET' }, sessionId);
}

export function selectMenuOption(sessionId: string, option: string): Promise<MenuSelectResponse> {
  return request<MenuSelectResponse>(
    '/menu',
    { method: 'POST', body: JSON.stringify({ option }) },
    sessionId,
  );
}

export function viewAccount(sessionId: string, acctId: string): Promise<AccountViewResponse> {
  return request<AccountViewResponse>(
    `/accounts/view?acctId=${encodeURIComponent(acctId)}`,
    { method: 'GET' },
    sessionId,
  );
}
