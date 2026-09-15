import { randomUUID } from 'node:crypto';
import type { CardDemoCommarea } from '../domain/commarea.js';

/**
 * Pseudo-conversational CICS keeps state by handing the COMMAREA back on
 * `EXEC CICS RETURN TRANSID(...) COMMAREA(...)`. Over HTTP the same state
 * lives here and the client echoes the session id instead.
 */
export class SessionStore {
  private readonly sessions = new Map<string, CardDemoCommarea>();

  create(commarea: CardDemoCommarea): string {
    const id = randomUUID();
    this.sessions.set(id, commarea);
    return id;
  }

  get(id: string | undefined): CardDemoCommarea | undefined {
    return id === undefined ? undefined : this.sessions.get(id);
  }

  update(id: string, commarea: CardDemoCommarea): void {
    this.sessions.set(id, commarea);
  }

  destroy(id: string): void {
    this.sessions.delete(id);
  }
}
