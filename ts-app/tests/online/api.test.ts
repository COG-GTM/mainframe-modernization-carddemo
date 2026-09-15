import request from 'supertest';
import { describe, expect, it } from 'vitest';
import { SESSION_HEADER, createApi } from '../../src/online/api.js';
import { ACCOUNT, CUSTOMER, XREF, testRepositories } from './fixtures.js';

function api() {
  return createApi({ repositories: testRepositories() });
}

async function signonAs(app: ReturnType<typeof api>, userId: string): Promise<string> {
  const response = await request(app).post('/signon').send({ userId, password: 'PASSWORD' });
  expect(response.status).toBe(200);
  return response.body.sessionId as string;
}

describe('online API', () => {
  it('walks signon -> menu -> account view', async () => {
    const app = api();

    const signonResponse = await request(app)
      .post('/signon')
      .send({ userId: 'user0001', password: 'password' });
    expect(signonResponse.status).toBe(200);
    expect(signonResponse.body).toMatchObject({
      program: 'COMEN01C',
      route: '/menu',
      userType: 'U',
    });
    const sessionId = signonResponse.body.sessionId as string;

    const menuResponse = await request(app).get('/menu').set(SESSION_HEADER, sessionId);
    expect(menuResponse.status).toBe(200);
    expect(menuResponse.body.labels[0]).toBe('01. Account View');

    const selectResponse = await request(app)
      .post('/menu')
      .set(SESSION_HEADER, sessionId)
      .send({ option: '1' });
    expect(selectResponse.status).toBe(200);
    expect(selectResponse.body).toMatchObject({ program: 'COACTVWC', route: '/accounts/view' });

    const viewResponse = await request(app)
      .get('/accounts/view')
      .query({ acctId: ACCOUNT.acctId })
      .set(SESSION_HEADER, sessionId);
    expect(viewResponse.status).toBe(200);
    expect(viewResponse.body.account).toMatchObject({
      acctId: ACCOUNT.acctId,
      acctCurrBal: '1234.56',
      acctCurrCycDebit: '-25.75',
    });
    expect(viewResponse.body.customer).toMatchObject({ custId: CUSTOMER.custId });
    expect(viewResponse.body.xref).toMatchObject({ xrefCardNum: XREF.xrefCardNum });
    expect(viewResponse.body.infoMessage).toBe('Displaying details of given Account');
  });

  it('routes an admin signon to COADM01C', async () => {
    const response = await request(api())
      .post('/signon')
      .send({ userId: 'ADMIN001', password: 'PASSWORD' });

    expect(response.body).toMatchObject({
      program: 'COADM01C',
      route: '/admin-menu',
      userType: 'A',
    });
  });

  it('returns 400 with the COBOL message on a bad signon', async () => {
    const response = await request(api()).post('/signon').send({ userId: '', password: '' });

    expect(response.status).toBe(400);
    expect(response.body).toEqual({
      errorMessage: 'Please enter User ID ...',
      cursorField: 'userId',
    });
  });

  it('returns 400 with the account filter message', async () => {
    const app = api();
    const sessionId = await signonAs(app, 'USER0001');

    const response = await request(app)
      .get('/accounts/view')
      .query({ acctId: '0' })
      .set(SESSION_HEADER, sessionId);

    expect(response.status).toBe(400);
    expect(response.body.errorMessage).toBe('Account Filter must  be a non-zero 11 digit number');
  });

  it.each([
    ['get', '/menu'],
    ['post', '/menu'],
    ['get', '/accounts/view'],
  ] as const)(
    'sends an unauthenticated %s %s back to the signon screen (EIBCALEN = 0)',
    async (method, path) => {
      const response = await request(api())[method](path);

      expect(response.status).toBe(401);
      expect(response.body).toEqual({ program: 'COSGN00C', route: '/signon' });
    },
  );

  it('rejects a stale session id after signoff', async () => {
    const app = api();
    const sessionId = await signonAs(app, 'USER0001');

    await request(app).post('/signoff').set(SESSION_HEADER, sessionId).expect(200);

    const response = await request(app).get('/menu').set(SESSION_HEADER, sessionId);
    expect(response.status).toBe(401);
  });
});
