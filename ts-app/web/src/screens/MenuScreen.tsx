import { useEffect, useState } from 'react';
import { ApiError, listMenu, selectMenuOption, type MenuSelectResponse } from '../api.js';
import { Footer, Header } from '../screen/Header.js';
import { Input, Screen, Text } from '../screen/Screen.js';

export function MenuScreen({
  now,
  sessionId,
  onSelected,
  onSessionExpired,
}: {
  now: Date;
  sessionId: string;
  onSelected: (response: MenuSelectResponse) => void;
  onSessionExpired: () => void;
}): JSX.Element {
  const [labels, setLabels] = useState<string[]>([]);
  const [option, setOption] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    let cancelled = false;
    listMenu(sessionId)
      .then((menu) => {
        if (!cancelled) setLabels(menu.labels);
      })
      .catch((error: unknown) => {
        if (cancelled) return;
        if (error instanceof ApiError) setErrorMessage(error.message);
        else onSessionExpired();
      });
    return () => {
      cancelled = true;
    };
  }, [sessionId, onSessionExpired]);

  async function submit(): Promise<void> {
    setErrorMessage('');
    try {
      onSelected(await selectMenuOption(sessionId, option));
    } catch (error) {
      if (error instanceof ApiError) setErrorMessage(error.message);
      else onSessionExpired();
    }
  }

  return (
    <Screen onEnter={() => void submit()}>
      <Header tranId="CM00" program="COMEN01C" now={now} />
      <Text row={4} col={35} len={9} color="neutral" bright>
        Main Menu
      </Text>

      {labels.map((label, index) => (
        <Text key={label} row={6 + index} col={20} len={40}>
          {label}
        </Text>
      ))}

      <Text row={20} col={15} len={25} color="turquoise" bright>
        Please select an option :
      </Text>
      <Input
        id="option"
        label="Option"
        row={20}
        col={41}
        len={2}
        value={option}
        onChange={setOption}
        autoFocus
        numeric
      />

      <Footer errorMessage={errorMessage} keys="ENTER=Continue  F3=Exit" />
    </Screen>
  );
}
