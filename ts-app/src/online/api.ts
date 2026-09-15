import express, { type Express, type Request, type Response } from 'express';
import type { CardDemoRepositories } from '../data/repositories.js';
import type { CardDemoCommarea } from '../domain/commarea.js';
import { viewAccount } from './accountViewService.js';
import { menuOptionLabels, selectMenuOption } from './menuService.js';
import { MAIN_MENU_OPTIONS, PROGRAMS, routeForProgram } from './routes.js';
import { serializeRecord } from './serialize.js';
import { SessionStore } from './session.js';
import { signon } from './signonService.js';

const SESSION_HEADER = 'x-carddemo-session';

export interface ApiOptions {
  repositories: CardDemoRepositories;
  sessions?: SessionStore;
}

function requireSession(
  request: Request,
  response: Response,
  sessions: SessionStore,
): { id: string; commarea: CardDemoCommarea } | undefined {
  const id = request.header(SESSION_HEADER) ?? undefined;
  const commarea = sessions.get(id);
  if (id === undefined || commarea === undefined) {
    // CICS returns to the signon screen when EIBCALEN = 0.
    response
      .status(401)
      .json({ program: PROGRAMS.signon, route: routeForProgram(PROGRAMS.signon) });
    return undefined;
  }
  return { id, commarea };
}

export function createApi({ repositories, sessions = new SessionStore() }: ApiOptions): Express {
  const app = express();
  app.use(express.json());

  app.post('/signon', (request, response) => {
    const body = request.body as Partial<{ userId: string; password: string }>;
    const result = signon(
      { userId: body.userId ?? '', password: body.password ?? '' },
      repositories.users,
    );

    if (!result.ok) {
      response
        .status(400)
        .json({ errorMessage: result.errorMessage, cursorField: result.cursorField });
      return;
    }

    const sessionId = sessions.create(result.commarea);
    response.json({
      sessionId,
      program: result.program,
      route: routeForProgram(result.program),
      userType: result.commarea.userType,
    });
  });

  app.get('/menu', (request, response) => {
    const session = requireSession(request, response, sessions);
    if (session === undefined) return;
    response.json({ options: MAIN_MENU_OPTIONS, labels: menuOptionLabels() });
  });

  app.post('/menu', (request, response) => {
    const session = requireSession(request, response, sessions);
    if (session === undefined) return;

    const body = request.body as Partial<{ option: string }>;
    const result = selectMenuOption(String(body.option ?? ''), session.commarea);

    if (!result.ok) {
      response.status(400).json({ errorMessage: result.errorMessage });
      return;
    }

    sessions.update(session.id, result.commarea);
    response.json({
      program: result.program,
      route: routeForProgram(result.program),
      option: result.option,
    });
  });

  app.get('/accounts/view', (request, response) => {
    const session = requireSession(request, response, sessions);
    if (session === undefined) return;

    const acctId = typeof request.query.acctId === 'string' ? request.query.acctId : '';
    const result = viewAccount(acctId, session.commarea, repositories);

    if (!result.ok) {
      sessions.update(session.id, result.commarea);
      response.status(400).json({ errorMessage: result.errorMessage });
      return;
    }

    sessions.update(session.id, result.commarea);
    response.json({
      account: serializeRecord(result.account),
      customer: serializeRecord(result.customer),
      xref: serializeRecord(result.xref),
      infoMessage: result.infoMessage,
    });
  });

  app.post('/signoff', (request, response) => {
    const id = request.header(SESSION_HEADER);
    if (id !== undefined) sessions.destroy(id);
    response.json({ program: PROGRAMS.signon, route: routeForProgram(PROGRAMS.signon) });
  });

  return app;
}

export { SESSION_HEADER };
