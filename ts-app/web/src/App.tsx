import { useCallback, useEffect, useState } from 'react';
import { AccountViewScreen } from './screens/AccountViewScreen.js';
import { MenuScreen } from './screens/MenuScreen.js';
import { SignonScreen } from './screens/SignonScreen.js';

type ScreenName = 'signon' | 'menu' | 'accountView';

interface SessionState {
  sessionId: string;
  userType: string;
}

/** `routeForProgram` values the UI knows how to render. */
function screenForRoute(route: string | undefined): ScreenName {
  if (route === '/accounts/view') return 'accountView';
  return 'menu';
}

export function App(): JSX.Element {
  const [screen, setScreen] = useState<ScreenName>('signon');
  const [session, setSession] = useState<SessionState | undefined>(undefined);
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const timer = window.setInterval(() => setNow(new Date()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  // A 401 means EIBCALEN = 0: drop the session and go back to signon.
  const signOff = useCallback(() => {
    setSession(undefined);
    setScreen('signon');
  }, []);

  if (session === undefined || screen === 'signon') {
    return (
      <SignonScreen
        now={now}
        onSignedOn={(response) => {
          setSession({ sessionId: response.sessionId, userType: response.userType });
          setScreen(screenForRoute(response.route));
        }}
      />
    );
  }

  if (screen === 'accountView') {
    return <AccountViewScreen now={now} sessionId={session.sessionId} onSessionExpired={signOff} />;
  }

  return (
    <MenuScreen
      now={now}
      sessionId={session.sessionId}
      onSelected={(response) => setScreen(screenForRoute(response.route))}
      onSessionExpired={signOff}
    />
  );
}
